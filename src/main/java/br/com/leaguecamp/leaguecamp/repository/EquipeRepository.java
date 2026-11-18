package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Equipe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * EQUIPE é a tabela "pai": toda pessoa entra nela e depois em ATLETA ou COMISSAO_TECNICA.
 * Por isso inserir, atualizar e excluir usam transação.
 */
@Repository
public class EquipeRepository {

    private final JdbcTemplate jdbc;

    public EquipeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE = """
            SELECT e.ID_EQ, e.PNOME, e.UNOME, e.DATA_NASC, e.ID_CLUBE,
                   c.NOME AS NOME_CLUBE,
                   CASE WHEN a.ID_EQ IS NOT NULL THEN 'ATLETA' ELSE 'COMISSAO_TECNICA' END AS TIPO,
                   a.POSICAO, a.NUMERO_CAMISA, ct.FUNCAO
            FROM EQUIPE e
            JOIN CLUBE c ON c.ID_CLUBE = e.ID_CLUBE
            LEFT JOIN ATLETA a ON a.ID_EQ = e.ID_EQ
            LEFT JOIN COMISSAO_TECNICA ct ON ct.ID_EQ = e.ID_EQ
            """;

    private final RowMapper<Equipe> mapper = (rs, numeroLinha) -> new Equipe(
            rs.getInt("ID_EQ"),
            rs.getString("PNOME"),
            rs.getString("UNOME"),
            rs.getDate("DATA_NASC").toLocalDate(),
            rs.getInt("ID_CLUBE"),
            rs.getString("NOME_CLUBE"),
            rs.getString("TIPO"),
            rs.getString("POSICAO"),
            rs.getObject("NUMERO_CAMISA", Integer.class),
            rs.getString("FUNCAO")
    );

    public List<Equipe> listarTodos() {
        String sql = SELECT_BASE + """
                ORDER BY c.NOME, TIPO, e.PNOME
                """;
        return jdbc.query(sql, mapper);
    }

    public List<Equipe> listarPorClube(int idClube) {
        String sql = SELECT_BASE + """
                WHERE e.ID_CLUBE = ?
                ORDER BY TIPO, e.PNOME
                """;
        return jdbc.query(sql, mapper, idClube);
    }

    // Todas as pessoas dos dois clubes da partida (usado na súmula)
    public List<Equipe> listarDaPartida(int idPartida) {
        String sql = SELECT_BASE + """
                WHERE e.ID_CLUBE IN (
                    SELECT p.ID_CLUBE_CASA FROM PARTIDA p WHERE p.ID_PARTIDA = ?
                    UNION
                    SELECT p.ID_CLUBE_FORA FROM PARTIDA p WHERE p.ID_PARTIDA = ?
                )
                ORDER BY c.NOME, TIPO, e.PNOME
                """;
        return jdbc.query(sql, mapper, idPartida, idPartida);
    }

    public Optional<Equipe> buscarPorId(int idEq) {
        String sql = SELECT_BASE + """
                WHERE e.ID_EQ = ?
                """;
        return jdbc.query(sql, mapper, idEq).stream().findFirst();
    }

    @Transactional
    public void inserirAtleta(String pnome, String unome, LocalDate dataNasc, int idClube,
                              String posicao, int numeroCamisa) {
        int idEq = inserirEquipe(pnome, unome, dataNasc, idClube);

        String sql = """
                INSERT INTO ATLETA (ID_EQ, POSICAO, NUMERO_CAMISA)
                VALUES (?, ?, ?)
                """;
        jdbc.update(sql, idEq, posicao, numeroCamisa);
    }

    @Transactional
    public void inserirComissao(String pnome, String unome, LocalDate dataNasc, int idClube,
                                String funcao) {
        int idEq = inserirEquipe(pnome, unome, dataNasc, idClube);

        String sql = """
                INSERT INTO COMISSAO_TECNICA (ID_EQ, FUNCAO)
                VALUES (?, ?)
                """;
        jdbc.update(sql, idEq, funcao);
    }

    // O banco gera o ID_EQ (identity) e o RETURNING devolve o valor gerado
    private int inserirEquipe(String pnome, String unome, LocalDate dataNasc, int idClube) {
        String sql = """
                INSERT INTO EQUIPE (PNOME, UNOME, DATA_NASC, ID_CLUBE)
                VALUES (?, ?, ?, ?)
                RETURNING ID_EQ
                """;
        Integer idEq = jdbc.queryForObject(sql, Integer.class,
                pnome, unome, Date.valueOf(dataNasc), idClube);
        return idEq;
    }

    @Transactional
    public void atualizarAtleta(int idEq, String pnome, String unome, LocalDate dataNasc,
                                int idClube, String posicao, int numeroCamisa) {
        atualizarEquipe(idEq, pnome, unome, dataNasc, idClube);

        String sql = """
                UPDATE ATLETA
                SET POSICAO = ?, NUMERO_CAMISA = ?
                WHERE ID_EQ = ?
                """;
        jdbc.update(sql, posicao, numeroCamisa, idEq);
    }

    @Transactional
    public void atualizarComissao(int idEq, String pnome, String unome, LocalDate dataNasc,
                                  int idClube, String funcao) {
        atualizarEquipe(idEq, pnome, unome, dataNasc, idClube);

        String sql = """
                UPDATE COMISSAO_TECNICA
                SET FUNCAO = ?
                WHERE ID_EQ = ?
                """;
        jdbc.update(sql, funcao, idEq);
    }

    private void atualizarEquipe(int idEq, String pnome, String unome, LocalDate dataNasc, int idClube) {
        String sql = """
                UPDATE EQUIPE
                SET PNOME = ?, UNOME = ?, DATA_NASC = ?, ID_CLUBE = ?
                WHERE ID_EQ = ?
                """;
        jdbc.update(sql, pnome, unome, Date.valueOf(dataNasc), idClube, idEq);
    }

    // Se a pessoa tiver eventos na súmula, o último DELETE falha e tudo é desfeito
    @Transactional
    public void excluir(int idEq) {
        String sqlAtleta = """
                DELETE FROM ATLETA
                WHERE ID_EQ = ?
                """;
        jdbc.update(sqlAtleta, idEq);

        String sqlComissao = """
                DELETE FROM COMISSAO_TECNICA
                WHERE ID_EQ = ?
                """;
        jdbc.update(sqlComissao, idEq);

        String sqlEquipe = """
                DELETE FROM EQUIPE
                WHERE ID_EQ = ?
                """;
        jdbc.update(sqlEquipe, idEq);
    }
}
