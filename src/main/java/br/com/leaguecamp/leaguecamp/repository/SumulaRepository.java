package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.EventoSumula;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SumulaRepository {

    private final JdbcTemplate jdbc;

    public SumulaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<EventoSumula> mapper = (rs, numeroLinha) -> new EventoSumula(
            rs.getInt("ID_SUMULA"),
            rs.getString("TIPO_EVENTO"),
            rs.getInt("MIN"),
            rs.getInt("ID_PARTIDA"),
            rs.getInt("ID_MEMBRO"),
            rs.getString("NOME_MEMBRO"),
            rs.getString("NOME_CLUBE")
    );

    public List<EventoSumula> listarPorPartida(int idPartida) {
        String sql = """
                SELECT s.ID_SUMULA, s.TIPO_EVENTO, s.MIN, s.ID_PARTIDA, s.ID_MEMBRO,
                       e.PNOME || ' ' || e.UNOME AS NOME_MEMBRO,
                       c.NOME AS NOME_CLUBE
                FROM SUMULA s
                JOIN EQUIPE e ON e.ID_EQ = s.ID_MEMBRO
                JOIN CLUBE c ON c.ID_CLUBE = e.ID_CLUBE
                WHERE s.ID_PARTIDA = ?
                ORDER BY s.MIN, s.ID_SUMULA
                """;
        return jdbc.query(sql, mapper, idPartida);
    }

    public Optional<EventoSumula> buscarPorId(int idSumula) {
        String sql = """
                SELECT s.ID_SUMULA, s.TIPO_EVENTO, s.MIN, s.ID_PARTIDA, s.ID_MEMBRO,
                       e.PNOME || ' ' || e.UNOME AS NOME_MEMBRO,
                       c.NOME AS NOME_CLUBE
                FROM SUMULA s
                JOIN EQUIPE e ON e.ID_EQ = s.ID_MEMBRO
                JOIN CLUBE c ON c.ID_CLUBE = e.ID_CLUBE
                WHERE s.ID_SUMULA = ?
                """;
        return jdbc.query(sql, mapper, idSumula).stream().findFirst();
    }

    public void inserir(String tipoEvento, int min, int idPartida, int idMembro) {
        String sql = """
                INSERT INTO SUMULA (TIPO_EVENTO, MIN, ID_PARTIDA, ID_MEMBRO)
                VALUES (?, ?, ?, ?)
                """;
        jdbc.update(sql, tipoEvento, min, idPartida, idMembro);
    }

    public void atualizar(int idSumula, String tipoEvento, int min, int idMembro) {
        String sql = """
                UPDATE SUMULA
                SET TIPO_EVENTO = ?, MIN = ?, ID_MEMBRO = ?
                WHERE ID_SUMULA = ?
                """;
        jdbc.update(sql, tipoEvento, min, idMembro, idSumula);
    }

    public void excluir(int idSumula) {
        String sql = """
                DELETE FROM SUMULA
                WHERE ID_SUMULA = ?
                """;
        jdbc.update(sql, idSumula);
    }

    // O membro precisa ser de um dos dois clubes da partida
    public boolean membroPertenceAPartida(int idPartida, int idMembro) {
        String sql = """
                SELECT COUNT(*)
                FROM EQUIPE e
                JOIN PARTIDA p ON p.ID_PARTIDA = ?
                WHERE e.ID_EQ = ?
                  AND e.ID_CLUBE IN (p.ID_CLUBE_CASA, p.ID_CLUBE_FORA)
                """;
        Integer total = jdbc.queryForObject(sql, Integer.class, idPartida, idMembro);
        return total != null && total > 0;
    }

    // Refaz o placar contando os eventos da súmula.
    // GOL vale para o clube do jogador. GOL_CONTRA vale para o clube adversário.
    public void recalcularPlacar(int idPartida) {
        String sql = """
                UPDATE PARTIDA p
                SET GOLS_CASA = (
                        SELECT COUNT(*)
                        FROM SUMULA s
                        JOIN EQUIPE e ON e.ID_EQ = s.ID_MEMBRO
                        WHERE s.ID_PARTIDA = p.ID_PARTIDA
                          AND ((s.TIPO_EVENTO = 'GOL' AND e.ID_CLUBE = p.ID_CLUBE_CASA)
                            OR (s.TIPO_EVENTO = 'GOL_CONTRA' AND e.ID_CLUBE = p.ID_CLUBE_FORA))
                    ),
                    GOLS_FORA = (
                        SELECT COUNT(*)
                        FROM SUMULA s
                        JOIN EQUIPE e ON e.ID_EQ = s.ID_MEMBRO
                        WHERE s.ID_PARTIDA = p.ID_PARTIDA
                          AND ((s.TIPO_EVENTO = 'GOL' AND e.ID_CLUBE = p.ID_CLUBE_FORA)
                            OR (s.TIPO_EVENTO = 'GOL_CONTRA' AND e.ID_CLUBE = p.ID_CLUBE_CASA))
                    )
                WHERE p.ID_PARTIDA = ?
                """;
        jdbc.update(sql, idPartida);
    }
}
