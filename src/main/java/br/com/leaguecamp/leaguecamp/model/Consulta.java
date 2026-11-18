package br.com.leaguecamp.leaguecamp.model;

import java.util.List;

/**
 * Uma consulta pronta da página de Consultas.
 * parametros guarda os nomes dos filtros (campeonato, clube, partida, rodada)
 * e sql é exatamente o texto que o banco executa.
 */
public record Consulta(int numero, String titulo, String descricao,
                       List<String> parametros, String sql) {
}
