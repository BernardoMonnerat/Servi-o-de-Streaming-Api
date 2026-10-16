package br.com.api.streaming_plataform.Service;

import br.com.api.streaming_plataform.Enum.Perfil;
import br.com.api.streaming_plataform.Enum.Plano;
import br.com.api.streaming_plataform.Exception.AcessoNegadoException;
import br.com.api.streaming_plataform.Model.Conta;
import br.com.api.streaming_plataform.Model.Filmes;
import br.com.api.streaming_plataform.Model.Historico;
import br.com.api.streaming_plataform.Model.Usuarios;
import br.com.api.streaming_plataform.Repository.FilmeRepository;
import br.com.api.streaming_plataform.Repository.HistoricoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmeServiceTest {

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private HistoricoRepository historicoRepository;

    @InjectMocks
    private FilmeService service;

    private Conta conta;
    private Usuarios usuario;

    private Filmes infantilBasico;
    private Filmes adolescenteBasico;
    private Filmes adultoBasico;
    private Filmes infantilPremium;

    @BeforeEach
    void setUp() {
        conta = new Conta();
        conta.setPlano(Plano.BASICO);

        usuario = new Usuarios();
        usuario.setConta(conta);
        usuario.setPerfil(Perfil.ADULTO);

        infantilBasico = criarFilme("Toy Story", Perfil.INFANTIL, Plano.BASICO);
        adolescenteBasico = criarFilme("Homem-Aranha", Perfil.ADOLESCENTE, Plano.BASICO);
        adultoBasico = criarFilme("Coringa", Perfil.ADULTO, Plano.BASICO);
        infantilPremium = criarFilme("Divertida Mente", Perfil.INFANTIL, Plano.PREMIUM);
    }

    private Filmes criarFilme(String titulo, Perfil classificacao, Plano plano) {
        Filmes filme = new Filmes();
        filme.setId(UUID.randomUUID());
        filme.setTitulo(titulo);
        filme.setAno("2020");
        filme.setClassificacao(classificacao);
        filme.setPlano(plano);
        return filme;
    }

    private List<Filmes> todosOsFilmes() {
        List<Filmes> filmes = new ArrayList<>();
        filmes.add(infantilBasico);
        filmes.add(adolescenteBasico);
        filmes.add(adultoBasico);
        filmes.add(infantilPremium);
        return filmes;
    }

    // ---------- Listagem por plano e perfil ----------

    @Test
    void adultoNoPlanoBasicoVeTodosOsFilmesDoBasico() {
        when(filmeRepository.findAll()).thenReturn(todosOsFilmes());

        List<Filmes> disponiveis = service.listarDisponiveis(usuario);

        assertEquals(3, disponiveis.size());
        assertFalse(disponiveis.contains(infantilPremium));
    }

    @Test
    void infantilVeSoFilmesInfantisDoSeuPlano() {
        usuario.setPerfil(Perfil.INFANTIL);
        when(filmeRepository.findAll()).thenReturn(todosOsFilmes());

        List<Filmes> disponiveis = service.listarDisponiveis(usuario);

        assertEquals(1, disponiveis.size());
        assertTrue(disponiveis.contains(infantilBasico));
    }

    @Test
    void adolescenteVeFilmesInfantisEAdolescentes() {
        usuario.setPerfil(Perfil.ADOLESCENTE);
        when(filmeRepository.findAll()).thenReturn(todosOsFilmes());

        List<Filmes> disponiveis = service.listarDisponiveis(usuario);

        assertEquals(2, disponiveis.size());
        assertFalse(disponiveis.contains(adultoBasico));
    }

    @Test
    void planoPremiumVeFilmesDeTodosOsPlanos() {
        conta.setPlano(Plano.PREMIUM);
        when(filmeRepository.findAll()).thenReturn(todosOsFilmes());

        List<Filmes> disponiveis = service.listarDisponiveis(usuario);

        assertEquals(4, disponiveis.size());
    }

    // ---------- Assistir filme ----------

    @Test
    void deveAssistirFilmePermitidoESalvarNoHistorico() {
        when(filmeRepository.findById(adultoBasico.getId())).thenReturn(Optional.of(adultoBasico));

        Historico historico = service.assistir(usuario, adultoBasico.getId());

        assertEquals(usuario, historico.getUsuario());
        assertEquals(adultoBasico, historico.getFilme());
        assertNotNull(historico.getAssistidoEm());
        verify(historicoRepository).save(historico);
    }

    @Test
    void deveBloquearFilmeQueOPerfilNaoPermite() {
        usuario.setPerfil(Perfil.INFANTIL);
        when(filmeRepository.findById(adultoBasico.getId())).thenReturn(Optional.of(adultoBasico));

        assertThrows(AcessoNegadoException.class, () -> service.assistir(usuario, adultoBasico.getId()));
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void deveBloquearFilmeQueOPlanoNaoInclui() {
        when(filmeRepository.findById(infantilPremium.getId())).thenReturn(Optional.of(infantilPremium));

        assertThrows(AcessoNegadoException.class, () -> service.assistir(usuario, infantilPremium.getId()));
        verify(historicoRepository, never()).save(any());
    }

    // ---------- Historico ----------

    @Test
    void historicoDeveVirNaOrdemAssistidaComRepeticoes() {
        // Assistiu Toy Story, depois Coringa, depois Toy Story de novo
        List<Historico> doBanco = new ArrayList<>();
        doBanco.add(criarHistorico(1L, infantilBasico));
        doBanco.add(criarHistorico(2L, adultoBasico));
        doBanco.add(criarHistorico(3L, infantilBasico));
        when(historicoRepository.findByUsuarioOrderByIdAsc(usuario)).thenReturn(doBanco);

        List<Historico> historico = service.historico(usuario);

        assertEquals(3, historico.size());
        assertEquals("Toy Story", historico.get(0).getFilme().getTitulo());
        assertEquals("Coringa", historico.get(1).getFilme().getTitulo());
        assertEquals("Toy Story", historico.get(2).getFilme().getTitulo());
    }

    private Historico criarHistorico(Long id, Filmes filme) {
        Historico historico = new Historico();
        historico.setId(id);
        historico.setUsuario(usuario);
        historico.setFilme(filme);
        return historico;
    }
}
