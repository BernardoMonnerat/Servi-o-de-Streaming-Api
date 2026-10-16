package br.com.api.streaming_plataform.Service;

import br.com.api.streaming_plataform.DTO.UsuarioDTO;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final AcessoRepository acessoRepository;
    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, AcessoRepository acessoRepository,
                          ContaRepository contaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.acessoRepository = acessoRepository;
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Usado pelo Spring Security no login
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuarios usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return usuario;
    }

    public Usuarios buscarPorEmail(String email) {
        Usuarios usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new RegraNegocioException("Usuário não encontrado");
        }
        return usuario;
    }

    // Quantos usuarios NAO assinantes cada plano permite
    public int getLimiteCompartilhamento(Plano plano) {
        if (plano == Plano.BASICO) {
            return 1;
        } else if (plano == Plano.PADRAO) {
            return 3;
        } else {
            return 5;
        }
    }

    // Cria o assinante junto com uma conta nova
    public Usuarios criarAssinante(UsuarioDTO dto) {
        if (dto.getPlano() == null) {
            throw new RegraNegocioException("O plano é obrigatório para o assinante");
        }
        validarDados(dto);

        Conta conta = new Conta();
        conta.setPlano(dto.getPlano());
        contaRepository.save(conta);

        Usuarios usuario = montarUsuario(dto, conta, TipoAcessos.ROLE_ASSINANTE);
        usuarioRepository.save(usuario);
        return usuario;
    }

    // Assinante adiciona um usuario nao assinante na conta dele
    public Usuarios adicionarUsuario(Usuarios logado, UsuarioDTO dto) {
        verificarAssinante(logado);
        validarDados(dto);

        Conta conta = logado.getConta();
        long naoAssinantes = usuarioRepository.countByConta(conta) - 1;
        if (naoAssinantes >= getLimiteCompartilhamento(conta.getPlano())) {
            throw new RegraNegocioException("A cota de compartilhamento do plano " + conta.getPlano() + " já foi atingida");
        }

        Usuarios usuario = montarUsuario(dto, conta, TipoAcessos.ROLE_COMUM);
        usuarioRepository.save(usuario);
        return usuario;
    }

    // Assinante remove um usuario nao assinante da conta dele
    public void removerUsuario(Usuarios logado, UUID id) {
        verificarAssinante(logado);

        Usuarios usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));

        if (!usuario.getConta().getId().equals(logado.getConta().getId())) {
            throw new AcessoNegadoException("Esse usuário não pertence à sua conta");
        }
        if (usuario.isAssinante()) {
            throw new RegraNegocioException("O assinante não pode ser removido da conta");
        }

        usuarioRepository.delete(usuario);
    }

    // O plano fica na conta, entao mudar aqui muda para todos os usuarios dela
    public Conta migrarPlano(Usuarios logado, Plano novoPlano) {
        verificarAssinante(logado);
        if (novoPlano == null) {
            throw new RegraNegocioException("Informe o novo plano");
        }

        Conta conta = logado.getConta();
        long naoAssinantes = usuarioRepository.countByConta(conta) - 1;
        if (naoAssinantes > getLimiteCompartilhamento(novoPlano)) {
            throw new RegraNegocioException("A conta tem " + naoAssinantes
                    + " usuários não assinantes, mas o plano " + novoPlano + " permite só "
                    + getLimiteCompartilhamento(novoPlano) + ". Remova usuários antes de migrar.");
        }

        conta.setPlano(novoPlano);
        contaRepository.save(conta);
        return conta;
    }

    private void verificarAssinante(Usuarios usuario) {
        if (!usuario.isAssinante()) {
            throw new AcessoNegadoException("Apenas o assinante da conta pode fazer essa operação");
        }
    }

    private void validarDados(UsuarioDTO dto) {
        if (dto.getEmail() == null || dto.getSenha() == null || dto.getPerfil() == null) {
            throw new RegraNegocioException("Email, senha e perfil são obrigatórios");
        }
        if (usuarioRepository.findByEmail(dto.getEmail()) != null) {
            throw new RegraNegocioException("Já existe um usuário com esse email");
        }
    }

    private Usuarios montarUsuario(UsuarioDTO dto, Conta conta, TipoAcessos tipo) {
        Usuarios usuario = new Usuarios();
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setPerfil(dto.getPerfil());
        usuario.setConta(conta);
        usuario.getAcessos().add(buscarAcesso(tipo));
        return usuario;
    }

    // Busca o acesso no banco; se ainda nao existir, cria
    private Acessos buscarAcesso(TipoAcessos tipo) {
        Acessos acesso = acessoRepository.findByAcesso(tipo);
        if (acesso == null) {
            acesso = new Acessos();
            acesso.setAcesso(tipo);
            acessoRepository.save(acesso);
        }
        return acesso;
    }
}
