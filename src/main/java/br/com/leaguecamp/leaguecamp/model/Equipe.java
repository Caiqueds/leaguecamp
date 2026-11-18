package br.com.leaguecamp.leaguecamp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Pessoa da tabela EQUIPE já unida com ATLETA ou COMISSAO_TECNICA.
 * tipo vale "ATLETA" ou "COMISSAO_TECNICA".
 * posicao e numeroCamisa só existem para atletas, funcao só para comissão técnica.
 */
public record Equipe(Integer idEq, String pnome, String unome, LocalDate dataNasc,
                     Integer idClube, String nomeClube, String tipo,
                     String posicao, Integer numeroCamisa, String funcao) {

    public String nomeCompleto() {
        return pnome + " " + unome;
    }

    public boolean ehAtleta() {
        return "ATLETA".equals(tipo);
    }

    public String tipoTexto() {
        return ehAtleta() ? "Atleta" : "Comissão técnica";
    }

    public String detalhe() {
        if (ehAtleta()) {
            return posicao + " · camisa " + numeroCamisa;
        }
        return funcao;
    }

    public String dataNascTexto() {
        return dataNasc == null ? "" : dataNasc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String dataNascInput() {
        return dataNasc == null ? "" : dataNasc.toString();
    }
}
