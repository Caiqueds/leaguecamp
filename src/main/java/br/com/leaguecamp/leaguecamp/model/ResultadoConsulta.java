package br.com.leaguecamp.leaguecamp.model;

import java.util.List;

public record ResultadoConsulta(List<String> colunas, List<List<String>> linhas) {
}
