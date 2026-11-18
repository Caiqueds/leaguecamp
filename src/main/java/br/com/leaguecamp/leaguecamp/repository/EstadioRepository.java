package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Estadio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EstadioRepository {

    private final JdbcTemplate jdbc;

    public EstadioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Estadio> mapper = (rs, numeroLinha) -> new Estadio(
            rs.getInt("ID_ESTADIO"),
            rs.getString("NOME"),
            rs.getString("CIDADE"),
            rs.getInt("CAPACIDADE")
    );

    public List<Estadio> listar() {
        String sql = """
                SELECT ID_ESTADIO, NOME, CIDADE, CAPACIDADE
                FROM ESTADIO
                ORDER BY NOME
                """;
        return jdbc.query(sql, mapper);
    }

    public Optional<Estadio> buscarPorId(int idEstadio) {
        String sql = """
                SELECT ID_ESTADIO, NOME, CIDADE, CAPACIDADE
                FROM ESTADIO
                WHERE ID_ESTADIO = ?
                """;
        return jdbc.query(sql, mapper, idEstadio).stream().findFirst();
    }

    public void inserir(String nome, String cidade, int capacidade) {
        String sql = """
                INSERT INTO ESTADIO (NOME, CIDADE, CAPACIDADE)
                VALUES (?, ?, ?)
                """;
        jdbc.update(sql, nome, cidade, capacidade);
    }

    public void atualizar(int idEstadio, String nome, String cidade, int capacidade) {
        String sql = """
                UPDATE ESTADIO
                SET NOME = ?, CIDADE = ?, CAPACIDADE = ?
                WHERE ID_ESTADIO = ?
                """;
        jdbc.update(sql, nome, cidade, capacidade, idEstadio);
    }

    public void excluir(int idEstadio) {
        String sql = """
                DELETE FROM ESTADIO
                WHERE ID_ESTADIO = ?
                """;
        jdbc.update(sql, idEstadio);
    }
}
