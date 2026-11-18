package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Escalacao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Tabela APITA: árbitros escalados em cada partida. */
@Repository
public class ApitaRepository {

    private final JdbcTemplate jdbc;

    public ApitaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Escalacao> mapper = (rs, numeroLinha) -> new Escalacao(
            rs.getInt("ID_ARB"),
            rs.getString("NOME_ARBITRO"),
            rs.getString("FUNCAO")
    );

    public List<Escalacao> listarPorPartida(int idPartida) {
        String sql = """
                SELECT ap.ID_ARB, arb.PNOME || ' ' || arb.UNOME AS NOME_ARBITRO, ap.FUNCAO
                FROM APITA ap
                JOIN ARBITRO arb ON arb.ID_ARB = ap.ID_ARB
                WHERE ap.ID_PARTIDA = ?
                ORDER BY CASE ap.FUNCAO WHEN 'PRINCIPAL' THEN 1 ELSE 2 END, ap.FUNCAO
                """;
        return jdbc.query(sql, mapper, idPartida);
    }

    // Se o árbitro já estiver escalado na partida, apenas troca a função dele
    public void escalar(int idArb, int idPartida, String funcao) {
        String sql = """
                INSERT INTO APITA (ID_ARB, ID_PARTIDA, FUNCAO)
                VALUES (?, ?, ?)
                ON CONFLICT (ID_ARB, ID_PARTIDA) DO UPDATE SET FUNCAO = EXCLUDED.FUNCAO
                """;
        jdbc.update(sql, idArb, idPartida, funcao);
    }

    public void remover(int idArb, int idPartida) {
        String sql = """
                DELETE FROM APITA
                WHERE ID_ARB = ? AND ID_PARTIDA = ?
                """;
        jdbc.update(sql, idArb, idPartida);
    }
}
