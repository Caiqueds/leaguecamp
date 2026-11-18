package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Clube;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClubeRepository {

    private final JdbcTemplate jdbc;

    public ClubeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Clube> mapper = (rs, numeroLinha) -> new Clube(
            rs.getInt("ID_CLUBE"),
            rs.getString("NOME"),
            rs.getString("CIDADE")
    );

    public List<Clube> listar() {
        String sql = """
                SELECT ID_CLUBE, NOME, CIDADE
                FROM CLUBE
                ORDER BY NOME
                """;
        return jdbc.query(sql, mapper);
    }

    public Optional<Clube> buscarPorId(int idClube) {
        String sql = """
                SELECT ID_CLUBE, NOME, CIDADE
                FROM CLUBE
                WHERE ID_CLUBE = ?
                """;
        return jdbc.query(sql, mapper, idClube).stream().findFirst();
    }

    public void inserir(String nome, String cidade) {
        String sql = """
                INSERT INTO CLUBE (NOME, CIDADE)
                VALUES (?, ?)
                """;
        jdbc.update(sql, nome, cidade);
    }

    public void atualizar(int idClube, String nome, String cidade) {
        String sql = """
                UPDATE CLUBE
                SET NOME = ?, CIDADE = ?
                WHERE ID_CLUBE = ?
                """;
        jdbc.update(sql, nome, cidade, idClube);
    }

    public void excluir(int idClube) {
        String sql = """
                DELETE FROM CLUBE
                WHERE ID_CLUBE = ?
                """;
        jdbc.update(sql, idClube);
    }
}
