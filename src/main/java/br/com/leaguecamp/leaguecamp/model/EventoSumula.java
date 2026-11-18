package br.com.leaguecamp.leaguecamp.model;

public record EventoSumula(Integer idSumula, String tipoEvento, Integer min,
                           Integer idPartida, Integer idMembro,
                           String nomeMembro, String nomeClube) {

    public String tipoTexto() {
        return switch (tipoEvento) {
            case "GOL" -> "Gol";
            case "GOL_CONTRA" -> "Gol contra";
            case "CARTAO_AMARELO" -> "Cartão amarelo";
            case "CARTAO_VERMELHO" -> "Cartão vermelho";
            default -> tipoEvento;
        };
    }
}
