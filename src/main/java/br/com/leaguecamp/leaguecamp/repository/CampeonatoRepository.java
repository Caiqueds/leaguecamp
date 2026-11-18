package br.com.leaguecamp.leaguecamp.repository;

import br.com.leaguecamp.leaguecamp.model.Campeonato;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class CampeonatoRepository {

    private final JdbcTemplate jdbc;

    public CampeonatoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Campeonato> mapper = (rs, numeroLinha) -> new Campeonato(
            rs.getInt("ID_CAMP"),
            rs.getString("NOME"),
            rs.getObject("EDICAO", Integer.class)
    );

    public List<Campeonato> listar() {
        String sql = """
                SELECT ID_CAMP, NOME, EDICAO
                FROM CAMPEONATO
                ORDER BY EDICAO DESC, NOME
                """;
        return jdbc.query(sql, mapper);
    }

    public Optional<Campeonato> buscarPorId(int idCamp) {
        String sql = """
                SELECT ID_CAMP, NOME, EDICAO
                FROM CAMPEONATO
                WHERE ID_CAMP = ?
                """;
        return jdbc.query(sql, mapper, idCamp).stream().findFirst();
    }

    public void inserir(String nome, Integer edicao) {
        String sql = """
                INSERT INTO CAMPEONATO (NOME, EDICAO)
                VALUES (?, ?)
                """;
        jdbc.update(sql, nome, edicao);
    }

    public void atualizar(int idCamp, String nome, Integer edicao) {
        String sql = """
                UPDATE CAMPEONATO
                SET NOME = ?, EDICAO = ?
                WHERE ID_CAMP = ?
                """;
        jdbc.update(sql, nome, edicao, idCamp);
    }

    // Remove as inscrições e depois o campeonato.
    // Se existir partida, o banco recusa o DELETE e a transação desfaz tudo.
    @Transactional
    public void excluir(int idCamp) {
        String sqlInscricoes = """
                DELETE FROM PARTICIPANTES
                WHERE ID_CAMP = ?
                """;
        jdbc.update(sqlInscricoes, idCamp);

        String sqlCampeonato = """
                DELETE FROM CAMPEONATO
                WHERE ID_CAMP = ?
                """;
        jdbc.update(sqlCampeonato, idCamp);
    }
}
