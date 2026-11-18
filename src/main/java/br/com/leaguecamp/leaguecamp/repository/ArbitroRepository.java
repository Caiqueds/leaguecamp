package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Arbitro;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ArbitroRepository {

    private final JdbcTemplate jdbc;

    public ArbitroRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Arbitro> mapper = (rs, numeroLinha) -> new Arbitro(
            rs.getInt("ID_ARB"),
            rs.getString("PNOME"),
            rs.getString("UNOME"),
            rs.getDate("DATA_NASC").toLocalDate()
    );

    public List<Arbitro> listar() {
        String sql = """
                SELECT ID_ARB, PNOME, UNOME, DATA_NASC
                FROM ARBITRO
                ORDER BY PNOME, UNOME
                """;
        return jdbc.query(sql, mapper);
    }

    public Optional<Arbitro> buscarPorId(int idArb) {
        String sql = """
                SELECT ID_ARB, PNOME, UNOME, DATA_NASC
                FROM ARBITRO
                WHERE ID_ARB = ?
                """;
        return jdbc.query(sql, mapper, idArb).stream().findFirst();
    }

    public void inserir(String pnome, String unome, LocalDate dataNasc) {
        String sql = """
                INSERT INTO ARBITRO (PNOME, UNOME, DATA_NASC)
                VALUES (?, ?, ?)
                """;
        jdbc.update(sql, pnome, unome, Date.valueOf(dataNasc));
    }

    public void atualizar(int idArb, String pnome, String unome, LocalDate dataNasc) {
        String sql = """
                UPDATE ARBITRO
                SET PNOME = ?, UNOME = ?, DATA_NASC = ?
                WHERE ID_ARB = ?
                """;
        jdbc.update(sql, pnome, unome, Date.valueOf(dataNasc), idArb);
    }

    public void excluir(int idArb) {
        String sql = """
                DELETE FROM ARBITRO
                WHERE ID_ARB = ?
                """;
        jdbc.update(sql, idArb);
    }
}
