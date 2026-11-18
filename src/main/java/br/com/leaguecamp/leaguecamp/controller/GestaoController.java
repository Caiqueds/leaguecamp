package br.com.leaguecamp.leaguecamp.controller;

import br.com.leaguecamp.leaguecamp.model.Campeonato;
import br.com.leaguecamp.leaguecamp.model.Partida;
import br.com.leaguecamp.leaguecamp.repository.ApitaRepository;
import br.com.leaguecamp.leaguecamp.repository.ArbitroRepository;
import br.com.leaguecamp.leaguecamp.repository.CampeonatoRepository;
import br.com.leaguecamp.leaguecamp.repository.ClubeRepository;
import br.com.leaguecamp.leaguecamp.repository.EquipeRepository;
import br.com.leaguecamp.leaguecamp.repository.EstadioRepository;
import br.com.leaguecamp.leaguecamp.repository.ParticipanteRepository;
import br.com.leaguecamp.leaguecamp.repository.PartidaRepository;
import br.com.leaguecamp.leaguecamp.repository.SumulaRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Página de Gestão: criar, editar e excluir tudo o que acontece no campeonato.
 * Padrão usado em todas as áreas:
 *   GET  /gestao/area                lista (e formulário, se vier ?editar=id)
 *   POST /gestao/area/salvar         cadastra (sem id) ou atualiza (com id)
 *   POST /gestao/area/{id}/excluir   exclui
 */
@Controller
@RequestMapping("/gestao")
public class GestaoController {

    private static final List<String> STATUS_PARTIDA =
            List.of("AGENDADA", "EM_ANDAMENTO", "FINALIZADA", "ADIADA");
    private static final List<String> FUNCOES_ARBITRO =
            List.of("PRINCIPAL", "ASSISTENTE_1", "ASSISTENTE_2", "QUARTO_ARBITRO");
    private static final List<String> TIPOS_EVENTO =
            List.of("GOL", "GOL_CONTRA", "CARTAO_AMARELO", "CARTAO_VERMELHO");

    private final CampeonatoRepository campeonatoRepository;
    private final ClubeRepository clubeRepository;
    private final EstadioRepository estadioRepository;
    private final ArbitroRepository arbitroRepository;
    private final EquipeRepository equipeRepository;
    private final ParticipanteRepository participanteRepository;
    private final PartidaRepository partidaRepository;
    private final ApitaRepository apitaRepository;
    private final SumulaRepository sumulaRepository;

    public GestaoController(CampeonatoRepository campeonatoRepository,
                            ClubeRepository clubeRepository,
                            EstadioRepository estadioRepository,
                            ArbitroRepository arbitroRepository,
                            EquipeRepository equipeRepository,
                            ParticipanteRepository participanteRepository,
                            PartidaRepository partidaRepository,
                            ApitaRepository apitaRepository,
                            SumulaRepository sumulaRepository) {
        this.campeonatoRepository = campeonatoRepository;
        this.clubeRepository = clubeRepository;
        this.estadioRepository = estadioRepository;
        this.arbitroRepository = arbitroRepository;
        this.equipeRepository = equipeRepository;
        this.participanteRepository = participanteRepository;
        this.partidaRepository = partidaRepository;
        this.apitaRepository = apitaRepository;
        this.sumulaRepository = sumulaRepository;
    }

    // ---------------------------------------------------------------
    // Métodos de apoio
    // ---------------------------------------------------------------

    // Executa uma ação no banco e guarda a mensagem de sucesso ou de erro para a próxima tela
    private String executar(Runnable acao, String sucesso, String erroDeVinculo,
                            RedirectAttributes mensagens, String destino) {
        try {
            acao.run();
            mensagens.addFlashAttribute("sucesso", sucesso);
        } catch (DataIntegrityViolationException e) {
            mensagens.addFlashAttribute("erro", erroDeVinculo);
        } catch (DataAccessException e) {
            mensagens.addFlashAttribute("erro",
                    "Erro no banco de dados: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:" + destino;
    }

    private String recusar(String mensagem, RedirectAttributes mensagens, String destino) {
        mensagens.addFlashAttribute("erro", mensagem);
        return "redirect:" + destino;
    }

    private boolean tamanhoInvalido(String texto, int maximo) {
        return texto.isEmpty() || texto.length() > maximo;
    }

    // ---------------------------------------------------------------
    // Painel
    // ---------------------------------------------------------------

    @GetMapping
    public String painel() {
        return "gestao/index";
    }

    // ---------------------------------------------------------------
    // Campeonatos e clubes inscritos (tabelas CAMPEONATO e PARTICIPANTES)
    // ---------------------------------------------------------------

    @GetMapping("/campeonatos")
    public String campeonatos(@RequestParam(required = false) Integer editar,
                              @RequestParam(required = false) Integer camp,
                              Model model) {
        model.addAttribute("campeonatos", campeonatoRepository.listar());
        model.addAttribute("campeonatoEdicao",
                editar == null ? null : campeonatoRepository.buscarPorId(editar).orElse(null));

        Campeonato selecionado = camp == null ? null : campeonatoRepository.buscarPorId(camp).orElse(null);
        model.addAttribute("campSelecionado", selecionado);
        if (selecionado != null) {
            model.addAttribute("inscritos", participanteRepository.listarClubesInscritos(camp));
            model.addAttribute("disponiveis", participanteRepository.listarClubesDisponiveis(camp));
        }
        return "gestao/campeonatos";
    }

    @PostMapping("/campeonatos/salvar")
    public String salvarCampeonato(@RequestParam(required = false) Integer idCamp,
                                   @RequestParam String nome,
                                   @RequestParam Integer edicao,
                                   RedirectAttributes mensagens) {
        String destino = "/gestao/campeonatos";
        String nomeLimpo = nome.trim();

        if (tamanhoInvalido(nomeLimpo, 50)) {
            return recusar("O nome deve ter entre 1 e 50 caracteres.", mensagens, destino);
        }
        if (edicao < 1900 || edicao > 2100) {
            return recusar("A edição deve ser um ano entre 1900 e 2100.", mensagens, destino);
        }

        if (idCamp == null) {
            return executar(() -> campeonatoRepository.inserir(nomeLimpo, edicao),
                    "Campeonato cadastrado.", "Não foi possível cadastrar o campeonato.",
                    mensagens, destino);
        }
        return executar(() -> campeonatoRepository.atualizar(idCamp, nomeLimpo, edicao),
                "Campeonato atualizado.", "Não foi possível atualizar o campeonato.",
                mensagens, destino);
    }

    @PostMapping("/campeonatos/{id}/excluir")
    public String excluirCampeonato(@PathVariable int id, RedirectAttributes mensagens) {
        return executar(() -> campeonatoRepository.excluir(id),
                "Campeonato excluído.",
                "Este campeonato possui partidas e não pode ser excluído.",
                mensagens, "/gestao/campeonatos");
    }

    @PostMapping("/campeonatos/{id}/clubes")
    public String inscreverClube(@PathVariable int id,
                                 @RequestParam int idClube,
                                 RedirectAttributes mensagens) {
        return executar(() -> participanteRepository.inscrever(idClube, id),
                "Clube inscrito no campeonato.",
                "Este clube já está inscrito no campeonato.",
                mensagens, "/gestao/campeonatos?camp=" + id);
    }

    @PostMapping("/campeonatos/{id}/clubes/{idClube}/remover")
    public String removerClubeDoCampeonato(@PathVariable int id,
                                           @PathVariable int idClube,
                                           RedirectAttributes mensagens) {
        String destino = "/gestao/campeonatos?camp=" + id;
        if (participanteRepository.clubeTemPartidas(id, idClube)) {
            return recusar("Este clube já tem partidas no campeonato e não pode ser removido.",
                    mensagens, destino);
        }
        return executar(() -> participanteRepository.remover(idClube, id),
                "Clube removido do campeonato.", "Não foi possível remover o clube.",
                mensagens, destino);
    }

    // ---------------------------------------------------------------
    // Clubes (tabela CLUBE)
    // ---------------------------------------------------------------

    @GetMapping("/clubes")
    public String clubes(@RequestParam(required = false) Integer editar, Model model) {
        model.addAttribute("clubes", clubeRepository.listar());
        model.addAttribute("clubeEdicao",
                editar == null ? null : clubeRepository.buscarPorId(editar).orElse(null));
        return "gestao/clubes";
    }

    @PostMapping("/clubes/salvar")
    public String salvarClube(@RequestParam(required = false) Integer idClube,
                              @RequestParam String nome,
                              @RequestParam String cidade,
                              RedirectAttributes mensagens) {
        String destino = "/gestao/clubes";
        String nomeLimpo = nome.trim();
        String cidadeLimpa = cidade.trim();

        if (tamanhoInvalido(nomeLimpo, 50) || tamanhoInvalido(cidadeLimpa, 50)) {
            return recusar("Nome e cidade devem ter entre 1 e 50 caracteres.", mensagens, destino);
        }

        if (idClube == null) {
            return executar(() -> clubeRepository.inserir(nomeLimpo, cidadeLimpa),
                    "Clube cadastrado.", "Não foi possível cadastrar o clube.", mensagens, destino);
        }
        return executar(() -> clubeRepository.atualizar(idClube, nomeLimpo, cidadeLimpa),
                "Clube atualizado.", "Não foi possível atualizar o clube.", mensagens, destino);
    }

    @PostMapping("/clubes/{id}/excluir")
    public String excluirClube(@PathVariable int id, RedirectAttributes mensagens) {
        return executar(() -> clubeRepository.excluir(id),
                "Clube excluído.",
                "Este clube tem elenco, inscrições ou partidas e não pode ser excluído.",
                mensagens, "/gestao/clubes");
    }

    // ---------------------------------------------------------------
    // Estádios (tabela ESTADIO)
    // ---------------------------------------------------------------

    @GetMapping("/estadios")
    public String estadios(@RequestParam(required = false) Integer editar, Model model) {
        model.addAttribute("estadios", estadioRepository.listar());
        model.addAttribute("estadioEdicao",
                editar == null ? null : estadioRepository.buscarPorId(editar).orElse(null));
        return "gestao/estadios";
    }

    @PostMapping("/estadios/salvar")
    public String salvarEstadio(@RequestParam(required = false) Integer idEstadio,
                                @RequestParam String nome,
                                @RequestParam String cidade,
                                @RequestParam int capacidade,
                                RedirectAttributes mensagens) {
        String destino = "/gestao/estadios";
        String nomeLimpo = nome.trim();
        String cidadeLimpa = cidade.trim();

        if (tamanhoInvalido(nomeLimpo, 50) || tamanhoInvalido(cidadeLimpa, 50)) {
            return recusar("Nome e cidade devem ter entre 1 e 50 caracteres.", mensagens, destino);
        }
        if (capacidade < 0) {
            return recusar("A capacidade não pode ser negativa.", mensagens, destino);
        }

        if (idEstadio == null) {
            return executar(() -> estadioRepository.inserir(nomeLimpo, cidadeLimpa, capacidade),
                    "Estádio cadastrado.", "Não foi possível cadastrar o estádio.", mensagens, destino);
        }
        return executar(() -> estadioRepository.atualizar(idEstadio, nomeLimpo, cidadeLimpa, capacidade),
                "Estádio atualizado.", "Não foi possível atualizar o estádio.", mensagens, destino);
    }

    @PostMapping("/estadios/{id}/excluir")
    public String excluirEstadio(@PathVariable int id, RedirectAttributes mensagens) {
        return executar(() -> estadioRepository.excluir(id),
                "Estádio excluído.",
                "Este estádio é usado em partidas e não pode ser excluído.",
                mensagens, "/gestao/estadios");
    }

    // ---------------------------------------------------------------
    // Árbitros (tabela ARBITRO)
    // ---------------------------------------------------------------

    @GetMapping("/arbitros")
    public String arbitros(@RequestParam(required = false) Integer editar, Model model) {
        model.addAttribute("arbitros", arbitroRepository.listar());
        model.addAttribute("arbitroEdicao",
                editar == null ? null : arbitroRepository.buscarPorId(editar).orElse(null));
        return "gestao/arbitros";
    }

    @PostMapping("/arbitros/salvar")
    public String salvarArbitro(@RequestParam(required = false) Integer idArb,
                                @RequestParam String pnome,
                                @RequestParam String unome,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNasc,
                                RedirectAttributes mensagens) {
        String destino = "/gestao/arbitros";
        String primeiroNome = pnome.trim();
        String ultimoNome = unome.trim();

        if (tamanhoInvalido(primeiroNome, 15)) {
            return recusar("O nome deve ter entre 1 e 15 caracteres.", mensagens, destino);
        }
        if (tamanhoInvalido(ultimoNome, 25)) {
            return recusar("O sobrenome deve ter entre 1 e 25 caracteres.", mensagens, destino);
        }
        if (dataNasc.isAfter(LocalDate.now())) {
            return recusar("A data de nascimento não pode ser no futuro.", mensagens, destino);
        }

        if (idArb == null) {
            return executar(() -> arbitroRepository.inserir(primeiroNome, ultimoNome, dataNasc),
                    "Árbitro cadastrado.", "Não foi possível cadastrar o árbitro.", mensagens, destino);
        }
        return executar(() -> arbitroRepository.atualizar(idArb, primeiroNome, ultimoNome, dataNasc),
                "Árbitro atualizado.", "Não foi possível atualizar o árbitro.", mensagens, destino);
    }

    @PostMapping("/arbitros/{id}/excluir")
    public String excluirArbitro(@PathVariable int id, RedirectAttributes mensagens) {
        return executar(() -> arbitroRepository.excluir(id),
                "Árbitro excluído.",
                "Este árbitro já foi escalado em partidas e não pode ser excluído.",
                mensagens, "/gestao/arbitros");
    }

    // ---------------------------------------------------------------
    // Elenco: atletas e comissão técnica (tabelas EQUIPE, ATLETA, COMISSAO_TECNICA)
    // ---------------------------------------------------------------

    @GetMapping("/elenco")
    public String elenco(@RequestParam(required = false) Integer clube,
                         @RequestParam(required = false) Integer editar,
                         Model model) {
        model.addAttribute("clubes", clubeRepository.listar());
        model.addAttribute("clubeFiltro", clube);
        model.addAttribute("membros",
                clube == null ? equipeRepository.listarTodos() : equipeRepository.listarPorClube(clube));
        model.addAttribute("membroEdicao",
                editar == null ? null : equipeRepository.buscarPorId(editar).orElse(null));
        return "gestao/elenco";
    }

    @PostMapping("/elenco/salvar")
    public String salvarMembro(@RequestParam(required = false) Integer idEq,
                               @RequestParam String tipo,
                               @RequestParam String pnome,
                               @RequestParam String unome,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNasc,
                               @RequestParam int idClube,
                               @RequestParam(required = false) String posicao,
                               @RequestParam(required = false) Integer numeroCamisa,
                               @RequestParam(required = false) String funcao,
                               RedirectAttributes mensagens) {
        String destino = "/gestao/elenco?clube=" + idClube;
        String primeiroNome = pnome.trim();
        String ultimoNome = unome.trim();
        String posicaoLimpa = posicao == null ? "" : posicao.trim();
        String funcaoLimpa = funcao == null ? "" : funcao.trim();
        boolean atleta = tipo.equals("ATLETA");

        if (tamanhoInvalido(primeiroNome, 15)) {
            return recusar("O nome deve ter entre 1 e 15 caracteres.", mensagens, destino);
        }
        if (tamanhoInvalido(ultimoNome, 25)) {
            return recusar("O sobrenome deve ter entre 1 e 25 caracteres.", mensagens, destino);
        }
        if (dataNasc.isAfter(LocalDate.now())) {
            return recusar("A data de nascimento não pode ser no futuro.", mensagens, destino);
        }

        if (atleta) {
            if (tamanhoInvalido(posicaoLimpa, 20)) {
                return recusar("Informe a posição do atleta.", mensagens, destino);
            }
            if (numeroCamisa == null || numeroCamisa < 1 || numeroCamisa > 99) {
                return recusar("O número da camisa deve estar entre 1 e 99.", mensagens, destino);
            }
        } else if (tamanhoInvalido(funcaoLimpa, 20)) {
            return recusar("Informe a função na comissão técnica.", mensagens, destino);
        }

        if (idEq == null && atleta) {
            return executar(() -> equipeRepository.inserirAtleta(primeiroNome, ultimoNome, dataNasc,
                            idClube, posicaoLimpa, numeroCamisa),
                    "Atleta cadastrado.", "Não foi possível cadastrar o atleta.", mensagens, destino);
        }
        if (idEq == null) {
            return executar(() -> equipeRepository.inserirComissao(primeiroNome, ultimoNome, dataNasc,
                            idClube, funcaoLimpa),
                    "Membro da comissão técnica cadastrado.",
                    "Não foi possível cadastrar o membro.", mensagens, destino);
        }
        if (atleta) {
            return executar(() -> equipeRepository.atualizarAtleta(idEq, primeiroNome, ultimoNome, dataNasc,
                            idClube, posicaoLimpa, numeroCamisa),
                    "Atleta atualizado.", "Não foi possível atualizar o atleta.", mensagens, destino);
        }
        return executar(() -> equipeRepository.atualizarComissao(idEq, primeiroNome, ultimoNome, dataNasc,
                        idClube, funcaoLimpa),
                "Membro da comissão técnica atualizado.",
                "Não foi possível atualizar o membro.", mensagens, destino);
    }

    @PostMapping("/elenco/{id}/excluir")
    public String excluirMembro(@PathVariable int id,
                                @RequestParam(required = false) Integer clube,
                                RedirectAttributes mensagens) {
        String destino = clube == null ? "/gestao/elenco" : "/gestao/elenco?clube=" + clube;
        return executar(() -> equipeRepository.excluir(id),
                "Membro excluído do elenco.",
                "Este membro tem eventos na súmula e não pode ser excluído.",
                mensagens, destino);
    }

    // ---------------------------------------------------------------
    // Partidas e árbitros escalados (tabelas PARTIDA e APITA)
    // ---------------------------------------------------------------

    @GetMapping("/partidas")
    public String partidas(@RequestParam(required = false) Integer campeonato,
                           @RequestParam(required = false) Integer rodada,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) Integer editar,
                           @RequestParam(required = false) Integer arbitros,
                           Model model) {
        String statusFiltro = (status == null || status.isBlank()) ? null : status;

        model.addAttribute("partidas", partidaRepository.listar(campeonato, rodada, statusFiltro));
        model.addAttribute("campeonatos", campeonatoRepository.listar());
        model.addAttribute("clubes", clubeRepository.listar());
        model.addAttribute("estadios", estadioRepository.listar());
        model.addAttribute("statusLista", STATUS_PARTIDA);

        model.addAttribute("filtroCampeonato", campeonato);
        model.addAttribute("filtroRodada", rodada);
        model.addAttribute("filtroStatus", statusFiltro);

        model.addAttribute("partidaEdicao",
                editar == null ? null : partidaRepository.buscarPorId(editar).orElse(null));

        Partida partidaArbitros = arbitros == null ? null : partidaRepository.buscarPorId(arbitros).orElse(null);
        model.addAttribute("partidaArbitros", partidaArbitros);
        if (partidaArbitros != null) {
            model.addAttribute("escalacao", apitaRepository.listarPorPartida(arbitros));
            model.addAttribute("todosArbitros", arbitroRepository.listar());
            model.addAttribute("funcoesArbitro", FUNCOES_ARBITRO);
        }
        return "gestao/partidas";
    }

    @PostMapping("/partidas/salvar")
    public String salvarPartida(@RequestParam(required = false) Integer idPartida,
                                @RequestParam int idCamp,
                                @RequestParam int rodada,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHora,
                                @RequestParam int idClubeCasa,
                                @RequestParam int idClubeFora,
                                @RequestParam int idEstadio,
                                @RequestParam int golsCasa,
                                @RequestParam int golsFora,
                                @RequestParam String status,
                                RedirectAttributes mensagens) {
        String destino = "/gestao/partidas?campeonato=" + idCamp;

        if (idClubeCasa == idClubeFora) {
            return recusar("O mandante e o visitante precisam ser clubes diferentes.", mensagens, destino);
        }
        if (rodada < 1) {
            return recusar("A rodada deve ser maior que zero.", mensagens, destino);
        }
        if (golsCasa < 0 || golsFora < 0) {
            return recusar("Os gols não podem ser negativos.", mensagens, destino);
        }
        if (!STATUS_PARTIDA.contains(status)) {
            return recusar("Status de partida inválido.", mensagens, destino);
        }
        if (!participanteRepository.clubeInscrito(idCamp, idClubeCasa)
                || !participanteRepository.clubeInscrito(idCamp, idClubeFora)) {
            return recusar("Os dois clubes precisam estar inscritos no campeonato.", mensagens, destino);
        }

        int idParaComparar = idPartida == null ? -1 : idPartida;
        if (partidaRepository.existeConflitoDeHorario(dataHora, idClubeCasa, idClubeFora, idParaComparar)) {
            return recusar("Um dos clubes já tem partida marcada nesse mesmo horário.", mensagens, destino);
        }

        if (idPartida == null) {
            return executar(() -> partidaRepository.inserir(rodada, dataHora, golsCasa, golsFora, status,
                            idCamp, idClubeCasa, idClubeFora, idEstadio),
                    "Partida cadastrada.", "Não foi possível cadastrar a partida.", mensagens, destino);
        }
        return executar(() -> partidaRepository.atualizar(idPartida, rodada, dataHora, golsCasa, golsFora,
                        status, idCamp, idClubeCasa, idClubeFora, idEstadio),
                "Partida atualizada.", "Não foi possível atualizar a partida.", mensagens, destino);
    }

    @PostMapping("/partidas/{id}/excluir")
    public String excluirPartida(@PathVariable int id, RedirectAttributes mensagens) {
        return executar(() -> partidaRepository.excluir(id),
                "Partida excluída junto com a súmula e a escalação de árbitros.",
                "Não foi possível excluir a partida.",
                mensagens, "/gestao/partidas");
    }

    @PostMapping("/partidas/{id}/arbitros")
    public String escalarArbitro(@PathVariable int id,
                                 @RequestParam int idArb,
                                 @RequestParam String funcao,
                                 RedirectAttributes mensagens) {
        String destino = "/gestao/partidas?arbitros=" + id;
        if (!FUNCOES_ARBITRO.contains(funcao)) {
            return recusar("Função de árbitro inválida.", mensagens, destino);
        }
        return executar(() -> apitaRepository.escalar(idArb, id, funcao),
                "Árbitro escalado.", "Não foi possível escalar o árbitro.", mensagens, destino);
    }

    @PostMapping("/partidas/{id}/arbitros/{idArb}/remover")
    public String removerArbitro(@PathVariable int id,
                                 @PathVariable int idArb,
                                 RedirectAttributes mensagens) {
        return executar(() -> apitaRepository.remover(idArb, id),
                "Árbitro removido da partida.", "Não foi possível remover o árbitro.",
                mensagens, "/gestao/partidas?arbitros=" + id);
    }

    // ---------------------------------------------------------------
    // Súmula (tabela SUMULA)
    // ---------------------------------------------------------------

    @GetMapping("/sumula")
    public String sumula(@RequestParam(required = false) Integer partida,
                         @RequestParam(required = false) Integer editar,
                         Model model) {
        model.addAttribute("partidas", partidaRepository.listar(null, null, null));
        model.addAttribute("tiposEvento", TIPOS_EVENTO);

        Partida selecionada = partida == null ? null : partidaRepository.buscarPorId(partida).orElse(null);
        model.addAttribute("partidaSel", selecionada);
        if (selecionada != null) {
            model.addAttribute("eventos", sumulaRepository.listarPorPartida(partida));
            model.addAttribute("membros", equipeRepository.listarDaPartida(partida));
            model.addAttribute("eventoEdicao",
                    editar == null ? null : sumulaRepository.buscarPorId(editar).orElse(null));
        }
        return "gestao/sumula";
    }

    @PostMapping("/sumula/salvar")
    public String salvarEvento(@RequestParam(required = false) Integer idSumula,
                               @RequestParam int idPartida,
                               @RequestParam String tipoEvento,
                               @RequestParam int min,
                               @RequestParam int idMembro,
                               RedirectAttributes mensagens) {
        String destino = "/gestao/sumula?partida=" + idPartida;

        if (!TIPOS_EVENTO.contains(tipoEvento)) {
            return recusar("Tipo de evento inválido.", mensagens, destino);
        }
        if (min < 0 || min > 130) {
            return recusar("O minuto deve estar entre 0 e 130.", mensagens, destino);
        }
        if (!sumulaRepository.membroPertenceAPartida(idPartida, idMembro)) {
            return recusar("O jogador precisa ser de um dos dois clubes da partida.", mensagens, destino);
        }

        if (idSumula == null) {
            return executar(() -> sumulaRepository.inserir(tipoEvento, min, idPartida, idMembro),
                    "Evento registrado na súmula.", "Não foi possível registrar o evento.",
                    mensagens, destino);
        }
        return executar(() -> sumulaRepository.atualizar(idSumula, tipoEvento, min, idMembro),
                "Evento atualizado.", "Não foi possível atualizar o evento.", mensagens, destino);
    }

    @PostMapping("/sumula/{id}/excluir")
    public String excluirEvento(@PathVariable int id,
                                @RequestParam int idPartida,
                                RedirectAttributes mensagens) {
        return executar(() -> sumulaRepository.excluir(id),
                "Evento excluído da súmula.", "Não foi possível excluir o evento.",
                mensagens, "/gestao/sumula?partida=" + idPartida);
    }

    @PostMapping("/sumula/recalcular")
    public String recalcularPlacar(@RequestParam int idPartida, RedirectAttributes mensagens) {
        return executar(() -> sumulaRepository.recalcularPlacar(idPartida),
                "Placar recalculado a partir dos gols da súmula.",
                "Não foi possível recalcular o placar.",
                mensagens, "/gestao/sumula?partida=" + idPartida);
    }
}
