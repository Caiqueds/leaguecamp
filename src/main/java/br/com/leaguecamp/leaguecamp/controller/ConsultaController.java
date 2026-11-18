package br.com.leaguecamp.leaguecamp.controller;

import br.com.leaguecamp.leaguecamp.model.Consulta;
import br.com.leaguecamp.leaguecamp.model.ResultadoConsulta;
import br.com.leaguecamp.leaguecamp.repository.CampeonatoRepository;
import br.com.leaguecamp.leaguecamp.repository.ClubeRepository;
import br.com.leaguecamp.leaguecamp.repository.ConsultaRepository;
import br.com.leaguecamp.leaguecamp.repository.PartidaRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Página de Consultas: só leitura, nenhuma rota altera dados. */
@Controller
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaRepository consultaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final ClubeRepository clubeRepository;
    private final PartidaRepository partidaRepository;

    public ConsultaController(ConsultaRepository consultaRepository,
                              CampeonatoRepository campeonatoRepository,
                              ClubeRepository clubeRepository,
                              PartidaRepository partidaRepository) {
        this.consultaRepository = consultaRepository;
        this.campeonatoRepository = campeonatoRepository;
        this.clubeRepository = clubeRepository;
        this.partidaRepository = partidaRepository;
    }

    @GetMapping
    public String indice(Model model) {
        carregarListas(model);
        model.addAttribute("consulta", null);
        model.addAttribute("filtros", new HashMap<String, String>());
        return "consultas/index";
    }

    @GetMapping("/{numero}")
    public String abrir(@PathVariable int numero,
                        @RequestParam Map<String, String> filtros,
                        Model model) {
        carregarListas(model);
        model.addAttribute("filtros", filtros);

        Optional<Consulta> encontrada = consultaRepository.buscar(numero);
        if (encontrada.isEmpty()) {
            model.addAttribute("consulta", null);
            model.addAttribute("erro", "Consulta não encontrada.");
            return "consultas/index";
        }

        Consulta consulta = encontrada.get();
        model.addAttribute("consulta", consulta);

        // Só executa quando o formulário foi enviado (botão Executar)
        if (!filtros.containsKey("executar")) {
            return "consultas/index";
        }

        Map<String, Object> parametros = new HashMap<>();
        for (String nome : consulta.parametros()) {
            String texto = filtros.get(nome);
            if (texto == null || texto.isBlank()) {
                model.addAttribute("erro", "Preencha o filtro: " + nome + ".");
                return "consultas/index";
            }
            try {
                parametros.put(nome, Integer.valueOf(texto.trim()));
            } catch (NumberFormatException e) {
                model.addAttribute("erro", "O filtro " + nome + " precisa ser um número.");
                return "consultas/index";
            }
        }

        try {
            ResultadoConsulta resultado = consultaRepository.executar(consulta, parametros);
            model.addAttribute("resultado", resultado);
        } catch (DataAccessException e) {
            model.addAttribute("erro", "Erro ao executar a consulta: " + e.getMostSpecificCause().getMessage());
        }
        return "consultas/index";
    }

    // Listas usadas nos campos de filtro (selects) e no menu lateral
    private void carregarListas(Model model) {
        model.addAttribute("consultas", consultaRepository.catalogo());
        model.addAttribute("campeonatos", campeonatoRepository.listar());
        model.addAttribute("clubes", clubeRepository.listar());
        model.addAttribute("partidas", partidaRepository.listar(null, null, null));
    }
}
