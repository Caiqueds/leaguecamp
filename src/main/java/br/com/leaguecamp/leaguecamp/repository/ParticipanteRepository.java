package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Clube;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Tabela PARTICIPANTES: clubes inscritos em cada campeonato. */
@Repository
public class ParticipanteRepository {

    private final JdbcTemplate jdbc;

    public ParticipanteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Clube> mapper = (rs, numeroLinha) -> new Clube(
            rs.getInt("ID_CLUBE"),
            rs.getString("NOME"),
            rs.getString("CIDADE")
    );

    public List<Clube> listarClubesInscritos(int idCamp) {
        String sql = """
                SELECT c.ID_CLUBE, c.NOME, c.CIDADE
                FROM PARTICIPANTES pa
                JOIN CLUBE c ON c.ID_CLUBE = pa.ID_CLUBE
                WHERE pa.ID_CAMP = ?
                ORDER BY c.NOME
                """;
        return jdbc.query(sql, mapper, idCamp);
    }

    public List<Clube> listarClubesDisponiveis(int idCamp) {
        String sql = """
                SELECT c.ID_CLUBE, c.NOME, c.CIDADE
                FROM CLUBE c
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM PARTICIPANTES pa
                    WHERE pa.ID_CLUBE = c.ID_CLUBE
                      AND pa.ID_CAMP = ?
                )
                ORDER BY c.NOME
                """;
        return jdbc.query(sql, mapper, idCamp);
    }

    public void inscrever(int idClube, int idCamp) {
        String sql = """
                INSERT INTO PARTICIPANTES (ID_CLUBE, ID_CAMP)
                VALUES (?, ?)
                """;
        jdbc.update(sql, idClube, idCamp);
    }

    public void remover(int idClube, int idCamp) {
        String sql = """
                DELETE FROM PARTICIPANTES
                WHERE ID_CLUBE = ? AND ID_CAMP = ?
                """;
        jdbc.update(sql, idClube, idCamp);
    }

    public boolean clubeInscrito(int idCamp, int idClube) {
        String sql = """
                SELECT COUNT(*)
                FROM PARTICIPANTES
                WHERE ID_CAMP = ? AND ID_CLUBE = ?
                """;
        Integer total = jdbc.queryForObject(sql, Integer.class, idCamp, idClube);
        return total != null && total > 0;
    }

    public boolean clubeTemPartidas(int idCamp, int idClube) {
        String sql = """
                SELECT COUNT(*)
                FROM PARTIDA
                WHERE ID_CAMP = ?
                  AND (ID_CLUBE_CASA = ? OR ID_CLUBE_FORA = ?)
                """;
        Integer total = jdbc.queryForObject(sql, Integer.class, idCamp, idClube, idClube);
        return total != null && total > 0;
    }
}
