package br.com.leaguecamp.leaguecamp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record Arbitro(Integer idArb, String pnome, String unome, LocalDate dataNasc) {

    public String nomeCompleto() {
        return pnome + " " + unome;
    }

    public String dataNascTexto() {
        return dataNasc == null ? "" : dataNasc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String dataNascInput() {
        return dataNasc == null ? "" : dataNasc.toString();
    }
}
