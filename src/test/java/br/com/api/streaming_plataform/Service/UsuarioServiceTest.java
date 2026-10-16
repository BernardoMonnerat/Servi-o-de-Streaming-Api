package br.com.api.streaming_plataform.Service;

import br.com.api.streaming_plataform.DTO.UsuarioDTO;
import br.com.api.streaming_plataform.Enum.Perfil;
import br.com.api.streaming_plataform.Enum.Plano;
import br.com.api.streaming_plataform.Enum.TipoAcessos;
import br.com.api.streaming_plataform.Exception.AcessoNegadoException;
import br.com.api.streaming_plataform.Exception.RegraNegocioException;
import br.com.api.streaming_plataform.Model.Acessos;
import br.com.api.streaming_plataform.Model.Conta;
import br.com.api.streaming_plataform.Model.Usuarios;
import br.com.api.streaming_plataform.Repository.AcessoRepository;
import br.com.api.streaming_plataform.Repository.ContaRepository;
import br.com.api.streaming_plataform.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Testes de unidade: os repositorios sao "falsos" (mocks), entao nao precisa de banco.
// Assim testamos so as regras de negocio do UsuarioService.
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AcessoRepository acessoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private Conta conta;
    private Usuarios assinante;
    private Usuarios comum;

    @BeforeEach
    void setUp() {
        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setPlano(Plano.BASICO);

        assinante = criarUsuario("dono@email.com", TipoAcessos.ROLE_ASSINANTE, conta);
        comum = criarUsuario("filho@email.com", TipoAcessos.ROLE_COMUM, conta);
    }

    private Usuarios criarUsuario(String email, TipoAcessos tipo, Conta contaDoUsuario) {
        Acessos acesso = new Acessos();
        acesso.setAcesso(tipo);

        Usuarios usuario = new Usuarios();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail(email);
        usuario.setPerfil(Perfil.ADULTO);
        usuario.setConta(contaDoUsuario);
        usuario.getAcessos().add(acesso);
        return usuario;
    }

    // ---------- Criacao de usuarios ----------

    @Test
    void deveCriarAssinanteComNovaContaEPlanoEscolhido() {
        UsuarioDTO dto = new UsuarioDTO("novo@email.com", "123", Perfil.ADULTO, Plano.PREMIUM);
        when(passwordEncoder.encode("123")).thenReturn("senha-criptografada");

        Usuarios criado = service.criarAssinante(dto);

        assertTrue(criado.isAssinante());
        assertEquals(Plano.PREMIUM, criado.getConta().getPlano());
        assertEquals("senha-criptografada", criado.getSenha());
        verify(contaRepository).save(any(Conta.class));
        verify(usuarioRepository).save(criado);
    }

    @Test
    void naoDeveCriarAssinanteComEmailRepetido() {
        UsuarioDTO dto = new UsuarioDTO("dono@email.com", "123", Perfil.ADULTO, Plano.BASICO);
        when(usuarioRepository.findByEmail("dono@email.com")).thenReturn(assinante);

        assertThrows(RegraNegocioException.class, () -> service.criarAssinante(dto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarAssinanteSemPlano() {
        UsuarioDTO dto = new UsuarioDTO("novo@email.com", "123", Perfil.ADULTO, null);

        assertThrows(RegraNegocioException.class, () -> service.criarAssinante(dto));
    }

    @Test
    void assinanteDeveAdicionarUsuarioNaoAssinanteNaMesmaConta() {
        conta.setPlano(Plano.PADRAO);
        UsuarioDTO dto = new UsuarioDTO("novo@email.com", "123", Perfil.INFANTIL, null);
        when(usuarioRepository.countByConta(conta)).thenReturn(1L); // so o assinante

        Usuarios criado = service.adicionarUsuario(assinante, dto);

        assertFalse(criado.isAssinante());
        assertEquals(conta, criado.getConta());
        assertEquals(Perfil.INFANTIL, criado.getPerfil());
        verify(usuarioRepository).save(criado);
    }

    // ---------- Restricao ao assinante ----------

    @Test
    void naoAssinanteNaoPodeAdicionarUsuario() {
        UsuarioDTO dto = new UsuarioDTO("novo@email.com", "123", Perfil.ADULTO, null);

        assertThrows(AcessoNegadoException.class, () -> service.adicionarUsuario(comum, dto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void assinanteDeveRemoverUsuarioDaSuaConta() {
        when(usuarioRepository.findById(comum.getId())).thenReturn(Optional.of(comum));

        service.removerUsuario(assinante, comum.getId());

        verify(usuarioRepository).delete(comum);
    }

    @Test
    void naoAssinanteNaoPodeRemoverUsuario() {
        assertThrows(AcessoNegadoException.class, () -> service.removerUsuario(comum, assinante.getId()));
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void assinanteNaoPodeRemoverUsuarioDeOutraConta() {
        Conta outraConta = new Conta();
        outraConta.setId(UUID.randomUUID());
        Usuarios deOutraConta = criarUsuario("outro@email.com", TipoAcessos.ROLE_COMUM, outraConta);
        when(usuarioRepository.findById(deOutraConta.getId())).thenReturn(Optional.of(deOutraConta));

        assertThrows(AcessoNegadoException.class, () -> service.removerUsuario(assinante, deOutraConta.getId()));
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void assinanteNaoPodeRemoverASiMesmo() {
        when(usuarioRepository.findById(assinante.getId())).thenReturn(Optional.of(assinante));

        assertThrows(RegraNegocioException.class, () -> service.removerUsuario(assinante, assinante.getId()));
        verify(usuarioRepository, never()).delete(any());
    }

    // ---------- Cota de compartilhamento ----------

    @Test
    void cotaDeCadaPlano() {
        assertEquals(1, service.getLimiteCompartilhamento(Plano.BASICO));
        assertEquals(3, service.getLimiteCompartilhamento(Plano.PADRAO));
        assertEquals(5, service.getLimiteCompartilhamento(Plano.PREMIUM));
    }

    @Test
    void naoDeveAdicionarUsuarioQuandoACotaFoiAtingida() {
        // BASICO permite 1 nao assinante, e a conta ja tem assinante + 1
        UsuarioDTO dto = new UsuarioDTO("novo@email.com", "123", Perfil.ADULTO, null);
        when(usuarioRepository.countByConta(conta)).thenReturn(2L);

        assertThrows(RegraNegocioException.class, () -> service.adicionarUsuario(assinante, dto));
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- Migracao de plano ----------

    @Test
    void assinanteDeveMigrarPlanoDaConta() {
        when(usuarioRepository.countByConta(conta)).thenReturn(2L);

        Conta atualizada = service.migrarPlano(assinante, Plano.PREMIUM);

        // O plano fica na conta, entao vale para todos os usuarios dela
        assertEquals(Plano.PREMIUM, atualizada.getPlano());
        assertEquals(Plano.PREMIUM, comum.getConta().getPlano());
        verify(contaRepository).save(conta);
    }

    @Test
    void naoAssinanteNaoPodeMigrarPlano() {
        assertThrows(AcessoNegadoException.class, () -> service.migrarPlano(comum, Plano.PREMIUM));
        verify(contaRepository, never()).save(any());
    }

    @Test
    void naoDeveMigrarParaPlanoComCotaMenorQueOsUsuariosAtuais() {
        conta.setPlano(Plano.PREMIUM);
        when(usuarioRepository.countByConta(conta)).thenReturn(4L); // assinante + 3

        assertThrows(RegraNegocioException.class, () -> service.migrarPlano(assinante, Plano.BASICO));
        assertEquals(Plano.PREMIUM, conta.getPlano());
        verify(contaRepository, never()).save(any());
    }
}
