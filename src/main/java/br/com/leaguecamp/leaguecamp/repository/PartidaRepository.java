package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Partida;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PartidaRepository {

    private final JdbcTemplate jdbc;

    public PartidaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE = """
            SELECT p.ID_PARTIDA, p.RODADA, p.DATA_HORA, p.GOLS_CASA, p.GOLS_FORA, p.STATUS,
                   p.ID_CAMP, camp.NOME || ' ' || camp.EDICAO AS NOME_CAMP,
                   p.ID_CLUBE_CASA, casa.NOME AS NOME_CASA,
                   p.ID_CLUBE_FORA, fora.NOME AS NOME_FORA,
                   p.ID_ESTADIO, est.NOME AS NOME_ESTADIO
            FROM PARTIDA p
            JOIN CAMPEONATO camp ON camp.ID_CAMP = p.ID_CAMP
            JOIN CLUBE casa ON casa.ID_CLUBE = p.ID_CLUBE_CASA
            JOIN CLUBE fora ON fora.ID_CLUBE = p.ID_CLUBE_FORA
            JOIN ESTADIO est ON est.ID_ESTADIO = p.ID_ESTADIO
            """;

    private final RowMapper<Partida> mapper = (rs, numeroLinha) -> new Partida(
            rs.getInt("ID_PARTIDA"),
            rs.getInt("RODADA"),
            rs.getTimestamp("DATA_HORA").toLocalDateTime(),
            rs.getInt("GOLS_CASA"),
            rs.getInt("GOLS_FORA"),
            rs.getString("STATUS"),
            rs.getInt("ID_CAMP"),
            rs.getString("NOME_CAMP"),
            rs.getInt("ID_CLUBE_CASA"),
            rs.getString("NOME_CASA"),
            rs.getInt("ID_CLUBE_FORA"),
            rs.getString("NOME_FORA"),
            rs.getInt("ID_ESTADIO"),
            rs.getString("NOME_ESTADIO")
    );

    // Os filtros são opcionais: só entram no WHERE os que foram informados.
    // Os valores sempre vão como parâmetros (?), nunca dentro do texto do SQL.
    public List<Partida> listar(Integer idCamp, Integer rodada, String status) {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1 = 1 ");
        List<Object> argumentos = new ArrayList<>();

        if (idCamp != null) {
            sql.append("AND p.ID_CAMP = ? ");
            argumentos.add(idCamp);
        }
        if (rodada != null) {
            sql.append("AND p.RODADA = ? ");
            argumentos.add(rodada);
        }
        if (status != null) {
            sql.append("AND p.STATUS = ? ");
            argumentos.add(status);
        }
        sql.append("ORDER BY camp.EDICAO DESC, p.ID_CAMP, p.RODADA, p.DATA_HORA");

        return jdbc.query(sql.toString(), mapper, argumentos.toArray());
    }

    public Optional<Partida> buscarPorId(int idPartida) {
        String sql = SELECT_BASE + """
                WHERE p.ID_PARTIDA = ?
                """;
        return jdbc.query(sql, mapper, idPartida).stream().findFirst();
    }

    public void inserir(int rodada, LocalDateTime dataHora, int golsCasa, int golsFora, String status,
                        int idCamp, int idClubeCasa, int idClubeFora, int idEstadio) {
        String sql = """
                INSERT INTO PARTIDA (RODADA, DATA_HORA, GOLS_CASA, GOLS_FORA, STATUS,
                                     ID_CAMP, ID_CLUBE_CASA, ID_CLUBE_FORA, ID_ESTADIO)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sql, rodada, Timestamp.valueOf(dataHora), golsCasa, golsFora, status,
                idCamp, idClubeCasa, idClubeFora, idEstadio);
    }

    public void atualizar(int idPartida, int rodada, LocalDateTime dataHora, int golsCasa, int golsFora,
                          String status, int idCamp, int idClubeCasa, int idClubeFora, int idEstadio) {
        String sql = """
                UPDATE PARTIDA
                SET RODADA = ?, DATA_HORA = ?, GOLS_CASA = ?, GOLS_FORA = ?, STATUS = ?,
                    ID_CAMP = ?, ID_CLUBE_CASA = ?, ID_CLUBE_FORA = ?, ID_ESTADIO = ?
                WHERE ID_PARTIDA = ?
                """;
        jdbc.update(sql, rodada, Timestamp.valueOf(dataHora), golsCasa, golsFora, status,
                idCamp, idClubeCasa, idClubeFora, idEstadio, idPartida);
    }

    // Apaga primeiro a súmula e a escalação de árbitros, que dependem da partida
    @Transactional
    public void excluir(int idPartida) {
        String sqlSumula = """
                DELETE FROM SUMULA
                WHERE ID_PARTIDA = ?
                """;
        jdbc.update(sqlSumula, idPartida);

        String sqlApita = """
                DELETE FROM APITA
                WHERE ID_PARTIDA = ?
                """;
        jdbc.update(sqlApita, idPartida);

        String sqlPartida = """
                DELETE FROM PARTIDA
                WHERE ID_PARTIDA = ?
                """;
        jdbc.update(sqlPartida, idPartida);
    }

    // Regra: um clube não pode jogar duas partidas no mesmo horário.
    // idPartida = -1 quando a partida ainda não existe (cadastro novo).
    public boolean existeConflitoDeHorario(LocalDateTime dataHora, int idClubeCasa, int idClubeFora,
                                           int idPartida) {
        String sql = """
                SELECT COUNT(*)
                FROM PARTIDA p
                WHERE p.DATA_HORA = ?
                  AND p.ID_PARTIDA <> ?
                  AND p.STATUS <> 'ADIADA'
                  AND (p.ID_CLUBE_CASA IN (?, ?) OR p.ID_CLUBE_FORA IN (?, ?))
                """;
        Integer total = jdbc.queryForObject(sql, Integer.class,
                Timestamp.valueOf(dataHora), idPartida,
                idClubeCasa, idClubeFora, idClubeCasa, idClubeFora);
        return total != null && total > 0;
    }
}
