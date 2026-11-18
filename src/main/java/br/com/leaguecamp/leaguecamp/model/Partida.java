package br.com.leaguecamp.leaguecamp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Partida(Integer idPartida, Integer rodada, LocalDateTime dataHora,
                      Integer golsCasa, Integer golsFora, String status,
                      Integer idCamp, String nomeCamp,
                      Integer idClubeCasa, String nomeCasa,
                      Integer idClubeFora, String nomeFora,
                      Integer idEstadio, String nomeEstadio) {

    public String dataHoraTexto() {
        return dataHora == null ? "" : dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String dataHoraInput() {
        return dataHora == null ? "" : dataHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public String placar() {
        return golsCasa + " x " + golsFora;
    }

    public String titulo() {
        return "Rodada " + rodada + ": " + nomeCasa + " x " + nomeFora;
    }

    public String statusTexto() {
        return switch (status) {
            case "AGENDADA" -> "Agendada";
            case "EM_ANDAMENTO" -> "Em andamento";
            case "FINALIZADA" -> "Finalizada";
            case "ADIADA" -> "Adiada";
            default -> status;
        };
    }
}
