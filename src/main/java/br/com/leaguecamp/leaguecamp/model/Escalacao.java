package br.com.leaguecamp.leaguecamp.model;

/** Árbitro escalado em uma partida (linha da tabela APITA). */
public record Escalacao(Integer idArb, String nomeArbitro, String funcao) {

    public String funcaoTexto() {
        return switch (funcao) {
            case "PRINCIPAL" -> "Árbitro principal";
            case "ASSISTENTE_1" -> "Assistente 1";
            case "ASSISTENTE_2" -> "Assistente 2";
            case "QUARTO_ARBITRO" -> "Quarto árbitro";
            default -> funcao;
        };
    }
}
