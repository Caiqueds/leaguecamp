package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Consulta;
import br.com.leaguecamp.leaguecamp.model.ResultadoConsulta;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Todas as consultas da página de Consultas.
 * Cada SQL está escrito por extenso (sem SELECT *, sem ORM) e é o mesmo texto
 * que aparece na tela. Os filtros entram como parâmetros nomeados (:campeonato, :clube...).
 */
@Repository
public class ConsultaRepository {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final List<Consulta> catalogo = List.of(

            new Consulta(1, "Classificação do campeonato",
                    "Pontos, jogos, vitórias, empates, derrotas, gols e saldo de cada clube. "
                            + "Só conta partidas finalizadas (vitória 3 pontos, empate 1).",
                    List.of("campeonato"),
                    """
                    SELECT c.NOME AS clube,
                           COUNT(*) AS jogos,
                           SUM(CASE WHEN r.GOLS_PRO > r.GOLS_CONTRA THEN 1 ELSE 0 END) AS vitorias,
                           SUM(CASE WHEN r.GOLS_PRO = r.GOLS_CONTRA THEN 1 ELSE 0 END) AS empates,
                           SUM(CASE WHEN r.GOLS_PRO < r.GOLS_CONTRA THEN 1 ELSE 0 END) AS derrotas,
                           SUM(r.GOLS_PRO) AS gols_pro,
                           SUM(r.GOLS_CONTRA) AS gols_contra,
                           SUM(r.GOLS_PRO) - SUM(r.GOLS_CONTRA) AS saldo,
                           SUM(CASE WHEN r.GOLS_PRO > r.GOLS_CONTRA THEN 3
                                    WHEN r.GOLS_PRO = r.GOLS_CONTRA THEN 1
                                    ELSE 0 END) AS pontos
                    FROM (
                        SELECT p.ID_CLUBE_CASA AS ID_CLUBE,
                               p.GOLS_CASA AS GOLS_PRO,
                               p.GOLS_FORA AS GOLS_CONTRA
                        FROM PARTIDA p
                        WHERE p.ID_CAMP = :campeonato
                          AND p.STATUS = 'FINALIZADA'
                        UNION ALL
                        SELECT p.ID_CLUBE_FORA AS ID_CLUBE,
                               p.GOLS_FORA AS GOLS_PRO,
                               p.GOLS_CASA AS GOLS_CONTRA
                        FROM PARTIDA p
                        WHERE p.ID_CAMP = :campeonato
                          AND p.STATUS = 'FINALIZADA'
                    ) r
                    JOIN CLUBE c ON c.ID_CLUBE = r.ID_CLUBE
                    GROUP BY c.ID_CLUBE, c.NOME
                    ORDER BY pontos DESC, vitorias DESC, saldo DESC, gols_pro DESC, c.NOME
                    """),

            new Consulta(2, "Artilheiros do campeonato",
                    "Jogadores com mais gols. Gols contra não entram na contagem.",
                    List.of("campeonato"),
                    """
                    SELECT e.PNOME || ' ' || e.UNOME AS jogador,
                           cl.NOME AS clube,
                           COUNT(s.ID_SUMULA) AS gols
                    FROM SUMULA s
                    JOIN PARTIDA p ON p.ID_PARTIDA = s.ID_PARTIDA
                    JOIN EQUIPE e ON e.ID_EQ = s.ID_MEMBRO
                    JOIN CLUBE cl ON cl.ID_CLUBE = e.ID_CLUBE
                    WHERE p.ID_CAMP = :campeonato
                      AND s.TIPO_EVENTO = 'GOL'
                    GROUP BY e.ID_EQ, e.PNOME, e.UNOME, cl.NOME
                    ORDER BY gols DESC, e.PNOME
                    """),

            new Consulta(3, "Partidas de uma rodada",
                    "Todos os jogos de uma rodada do campeonato, com placar e estádio.",
                    List.of("campeonato", "rodada"),
                    """
                    SELECT p.DATA_HORA AS data_hora,
                           casa.NOME AS mandante,
                           p.GOLS_CASA AS gols_mandante,
                           p.GOLS_FORA AS gols_visitante,
                           fora.NOME AS visitante,
                           est.NOME AS estadio,
                           p.STATUS AS status
                    FROM PARTIDA p
                    JOIN CLUBE casa ON casa.ID_CLUBE = p.ID_CLUBE_CASA
                    JOIN CLUBE fora ON fora.ID_CLUBE = p.ID_CLUBE_FORA
                    JOIN ESTADIO est ON est.ID_ESTADIO = p.ID_ESTADIO
                    WHERE p.ID_CAMP = :campeonato
                      AND p.RODADA = :rodada
                    ORDER BY p.DATA_HORA
                    """),

            new Consulta(4, "Histórico de partidas de um clube",
                    "Todos os jogos do clube, como mandante ou visitante, com o resultado do ponto de vista dele.",
                    List.of("clube"),
                    """
                    SELECT p.DATA_HORA AS data_hora,
                           camp.NOME || ' ' || camp.EDICAO AS campeonato,
                           casa.NOME AS mandante,
                           p.GOLS_CASA AS gols_mandante,
                           p.GOLS_FORA AS gols_visitante,
                           fora.NOME AS visitante,
                           CASE
                               WHEN p.STATUS <> 'FINALIZADA' THEN 'Sem resultado'
                               WHEN p.GOLS_CASA = p.GOLS_FORA THEN 'Empate'
                               WHEN (p.ID_CLUBE_CASA = :clube AND p.GOLS_CASA > p.GOLS_FORA)
                                 OR (p.ID_CLUBE_FORA = :clube AND p.GOLS_FORA > p.GOLS_CASA) THEN 'Vitória'
                               ELSE 'Derrota'
                           END AS resultado
                    FROM PARTIDA p
                    JOIN CAMPEONATO camp ON camp.ID_CAMP = p.ID_CAMP
                    JOIN CLUBE casa ON casa.ID_CLUBE = p.ID_CLUBE_CASA
                    JOIN CLUBE fora ON fora.ID_CLUBE = p.ID_CLUBE_FORA
                    WHERE p.ID_CLUBE_CASA = :clube
                       OR p.ID_CLUBE_FORA = :clube
                    ORDER BY p.DATA_HORA
                    """),

            new Consulta(5, "Elenco de atletas de um clube",
                    "Atletas do clube com número da camisa, posição e data de nascimento.",
                    List.of("clube"),
                    """
                    SELECT a.NUMERO_CAMISA AS camisa,
                           e.PNOME || ' ' || e.UNOME AS atleta,
                           a.POSICAO AS posicao,
                           e.DATA_NASC AS nascimento
                    FROM ATLETA a
                    JOIN EQUIPE e ON e.ID_EQ = a.ID_EQ
                    WHERE e.ID_CLUBE = :clube
                    ORDER BY a.NUMERO_CAMISA
                    """),

            new Consulta(6, "Comissão técnica de um clube",
                    "Técnicos e demais membros da comissão técnica do clube.",
                    List.of("clube"),
                    """
                    SELECT e.PNOME || ' ' || e.UNOME AS membro,
                           ct.FUNCAO AS funcao,
                           e.DATA_NASC AS nascimento
                    FROM COMISSAO_TECNICA ct
                    JOIN EQUIPE e ON e.ID_EQ = ct.ID_EQ
                    WHERE e.ID_CLUBE = :clube
                    ORDER BY ct.FUNCAO, e.PNOME
                    """),

            new Consulta(7, "Cartões por clube",
                    "Quantidade de cartões amarelos e vermelhos de cada clube no campeonato.",
                    List.of("campeonato"),
                    """
                    SELECT cl.NOME AS clube,
                           COUNT(*) FILTER (WHERE s.TIPO_EVENTO = 'CARTAO_AMARELO') AS amarelos,
                           COUNT(*) FILTER (WHERE s.TIPO_EVENTO = 'CARTAO_VERMELHO') AS vermelhos
                    FROM SUMULA s
                    JOIN PARTIDA p ON p.ID_PARTIDA = s.ID_PARTIDA
                    JOIN EQUIPE e ON e.ID_EQ = s.ID_MEMBRO
                    JOIN CLUBE cl ON cl.ID_CLUBE = e.ID_CLUBE
                    WHERE p.ID_CAMP = :campeonato
                      AND s.TIPO_EVENTO IN ('CARTAO_AMARELO', 'CARTAO_VERMELHO')
                    GROUP BY cl.ID_CLUBE, cl.NOME
                    ORDER BY vermelhos DESC, amarelos DESC, cl.NOME
                    """),

            new Consulta(8, "Árbitros escalados em cada partida",
                    "Equipe de arbitragem de todas as partidas do campeonato.",
                    List.of("campeonato"),
                    """
                    SELECT p.RODADA AS rodada,
                           p.DATA_HORA AS data_hora,
                           casa.NOME || ' x ' || fora.NOME AS partida,
                           arb.PNOME || ' ' || arb.UNOME AS arbitro,
                           ap.FUNCAO AS funcao
                    FROM APITA ap
                    JOIN PARTIDA p ON p.ID_PARTIDA = ap.ID_PARTIDA
                    JOIN ARBITRO arb ON arb.ID_ARB = ap.ID_ARB
                    JOIN CLUBE casa ON casa.ID_CLUBE = p.ID_CLUBE_CASA
                    JOIN CLUBE fora ON fora.ID_CLUBE = p.ID_CLUBE_FORA
                    WHERE p.ID_CAMP = :campeonato
                    ORDER BY p.DATA_HORA, p.ID_PARTIDA,
                             CASE ap.FUNCAO WHEN 'PRINCIPAL' THEN 1 ELSE 2 END, ap.FUNCAO
                    """),

            new Consulta(9, "Ranking de árbitros por jogos apitados",
                    "Quantas partidas cada árbitro já apitou e quantas como árbitro principal. "
                            + "Árbitros sem jogos aparecem com zero (LEFT JOIN).",
                    List.of(),
                    """
                    SELECT arb.PNOME || ' ' || arb.UNOME AS arbitro,
                           COUNT(ap.ID_PARTIDA) AS jogos,
                           COUNT(*) FILTER (WHERE ap.FUNCAO = 'PRINCIPAL') AS como_principal
                    FROM ARBITRO arb
                    LEFT JOIN APITA ap ON ap.ID_ARB = arb.ID_ARB
                    GROUP BY arb.ID_ARB, arb.PNOME, arb.UNOME
                    ORDER BY jogos DESC, arbitro
                    """),

            new Consulta(10, "Média de gols por estádio",
                    "Média de gols por partida finalizada em cada estádio. "
                            + "O HAVING mostra só estádios com pelo menos 2 jogos.",
                    List.of(),
                    """
                    SELECT est.NOME AS estadio,
                           est.CIDADE AS cidade,
                           COUNT(p.ID_PARTIDA) AS partidas,
                           ROUND(AVG(p.GOLS_CASA + p.GOLS_FORA), 2) AS media_gols
                    FROM ESTADIO est
                    JOIN PARTIDA p ON p.ID_ESTADIO = est.ID_ESTADIO
                    WHERE p.STATUS = 'FINALIZADA'
                    GROUP BY est.ID_ESTADIO, est.NOME, est.CIDADE
                    HAVING COUNT(p.ID_PARTIDA) >= 2
                    ORDER BY media_gols DESC, est.NOME
                    """),

            new Consulta(11, "Maiores goleadas do campeonato",
                    "As 5 partidas finalizadas com maior diferença de gols.",
                    List.of("campeonato"),
                    """
                    SELECT p.RODADA AS rodada,
                           casa.NOME AS mandante,
                           p.GOLS_CASA AS gols_mandante,
                           p.GOLS_FORA AS gols_visitante,
                           fora.NOME AS visitante,
                           ABS(p.GOLS_CASA - p.GOLS_FORA) AS diferenca
                    FROM PARTIDA p
                    JOIN CLUBE casa ON casa.ID_CLUBE = p.ID_CLUBE_CASA
                    JOIN CLUBE fora ON fora.ID_CLUBE = p.ID_CLUBE_FORA
                    WHERE p.ID_CAMP = :campeonato
                      AND p.STATUS = 'FINALIZADA'
                    ORDER BY diferenca DESC, (p.GOLS_CASA + p.GOLS_FORA) DESC, p.DATA_HORA
                    LIMIT 5
                    """),

            new Consulta(12, "Clubes inscritos que ainda não jogaram",
                    "Subconsulta com NOT EXISTS: clubes inscritos no campeonato sem nenhuma partida finalizada.",
                    List.of("campeonato"),
                    """
                    SELECT c.NOME AS clube,
                           c.CIDADE AS cidade
                    FROM PARTICIPANTES pa
                    JOIN CLUBE c ON c.ID_CLUBE = pa.ID_CLUBE
                    WHERE pa.ID_CAMP = :campeonato
                      AND NOT EXISTS (
                          SELECT 1
                          FROM PARTIDA p
                          WHERE p.ID_CAMP = pa.ID_CAMP
                            AND p.STATUS = 'FINALIZADA'
                            AND (p.ID_CLUBE_CASA = c.ID_CLUBE OR p.ID_CLUBE_FORA = c.ID_CLUBE)
                      )
                    ORDER BY c.NOME
                    """),

            new Consulta(13, "Atletas que nunca marcaram gol",
                    "LEFT JOIN com IS NULL: atletas do clube sem nenhum gol registrado na súmula.",
                    List.of("clube"),
                    """
                    SELECT a.NUMERO_CAMISA AS camisa,
                           e.PNOME || ' ' || e.UNOME AS atleta,
                           a.POSICAO AS posicao
                    FROM ATLETA a
                    JOIN EQUIPE e ON e.ID_EQ = a.ID_EQ
                    LEFT JOIN SUMULA s ON s.ID_MEMBRO = e.ID_EQ
                                      AND s.TIPO_EVENTO = 'GOL'
                    WHERE e.ID_CLUBE = :clube
                      AND s.ID_SUMULA IS NULL
                    ORDER BY a.NUMERO_CAMISA
                    """),

            new Consulta(14, "Clubes com mais gols que a média do campeonato",
                    "Subconsulta com AVG: clubes cujo total de gols marcados fica acima da média dos clubes.",
                    List.of("campeonato"),
                    """
                    WITH gols_por_clube AS (
                        SELECT c.NOME AS clube,
                               SUM(r.GOLS) AS gols
                        FROM (
                            SELECT p.ID_CLUBE_CASA AS ID_CLUBE, p.GOLS_CASA AS GOLS
                            FROM PARTIDA p
                            WHERE p.ID_CAMP = :campeonato
                              AND p.STATUS = 'FINALIZADA'
                            UNION ALL
                            SELECT p.ID_CLUBE_FORA AS ID_CLUBE, p.GOLS_FORA AS GOLS
                            FROM PARTIDA p
                            WHERE p.ID_CAMP = :campeonato
                              AND p.STATUS = 'FINALIZADA'
                        ) r
                        JOIN CLUBE c ON c.ID_CLUBE = r.ID_CLUBE
                        GROUP BY c.ID_CLUBE, c.NOME
                    )
                    SELECT g.clube,
                           g.gols,
                           (SELECT ROUND(AVG(m.gols), 2) FROM gols_por_clube m) AS media_do_campeonato
                    FROM gols_por_clube g
                    WHERE g.gols > (SELECT AVG(m.gols) FROM gols_por_clube m)
                    ORDER BY g.gols DESC, g.clube
                    """),

            new Consulta(15, "Linha do tempo de uma partida (súmula)",
                    "Eventos da partida em ordem de minuto: gols, gols contra e cartões.",
                    List.of("partida"),
                    """
                    SELECT s.MIN AS minuto,
                           s.TIPO_EVENTO AS evento,
                           e.PNOME || ' ' || e.UNOME AS jogador,
                           cl.NOME AS clube
                    FROM SUMULA s
                    JOIN EQUIPE e ON e.ID_EQ = s.ID_MEMBRO
                    JOIN CLUBE cl ON cl.ID_CLUBE = e.ID_CLUBE
                    WHERE s.ID_PARTIDA = :partida
                    ORDER BY s.MIN, s.ID_SUMULA
                    """)
    );

    public List<Consulta> catalogo() {
        return catalogo;
    }

    public Optional<Consulta> buscar(int numero) {
        return catalogo.stream().filter(consulta -> consulta.numero() == numero).findFirst();
    }

    // Executa o SQL da consulta com os parâmetros informados e devolve colunas e linhas já em texto
    public ResultadoConsulta executar(Consulta consulta, Map<String, Object> parametros) {
        ResultSetExtractor<ResultadoConsulta> extrator = rs -> {
            ResultSetMetaData meta = rs.getMetaData();
            int quantidadeColunas = meta.getColumnCount();

            List<String> colunas = new ArrayList<>();
            for (int i = 1; i <= quantidadeColunas; i++) {
                colunas.add(meta.getColumnLabel(i));
            }

            List<List<String>> linhas = new ArrayList<>();
            while (rs.next()) {
                List<String> linha = new ArrayList<>();
                for (int i = 1; i <= quantidadeColunas; i++) {
                    linha.add(formatar(rs.getObject(i)));
                }
                linhas.add(linha);
            }
            return new ResultadoConsulta(colunas, linhas);
        };

        return jdbc.query(consulta.sql(), parametros, extrator);
    }

    private String formatar(Object valor) {
        if (valor == null) {
            return "-";
        }
        if (valor instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().format(FORMATO_DATA_HORA);
        }
        if (valor instanceof java.sql.Date data) {
            return data.toLocalDate().format(FORMATO_DATA);
        }
        return valor.toString();
    }
}
