package br.com.api.streaming_plataform.Controller;

import br.com.api.streaming_plataform.DTO.UsuarioDTO;
import br.com.api.streaming_plataform.DTO.UsuarioResponseDTO;
import br.com.api.streaming_plataform.Enum.Plano;
import br.com.api.streaming_plataform.Exception.AcessoNegadoException;
import br.com.api.streaming_plataform.Exception.RegraNegocioException;
import br.com.api.streaming_plataform.Model.Conta;
import br.com.api.streaming_plataform.Model.Usuarios;
import br.com.api.streaming_plataform.Service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // Publico: cria o assinante e a conta dele
    @PostMapping("/add-usuario")
    public ResponseEntity<Object> criarAssinante(@RequestBody UsuarioDTO dto) {
        try {
            Usuarios usuario = service.criarAssinante(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(usuario));
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Authentication tem os dados de quem fez login (getName() devolve o email)
    @PostMapping("/conta/usuarios")
    public ResponseEntity<Object> adicionarUsuario(@RequestBody UsuarioDTO dto, Authentication authentication) {
        try {
            Usuarios logado = service.buscarPorEmail(authentication.getName());
            Usuarios usuario = service.adicionarUsuario(logado, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(usuario));
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/conta/usuarios/{id}")
    public ResponseEntity<Object> removerUsuario(@PathVariable UUID id, Authentication authentication) {
        try {
            Usuarios logado = service.buscarPorEmail(authentication.getName());
            service.removerUsuario(logado, id);
            return ResponseEntity.noContent().build();
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/conta/plano/{plano}")
    public ResponseEntity<Object> migrarPlano(@PathVariable Plano plano, Authentication authentication) {
        try {
            Usuarios logado = service.buscarPorEmail(authentication.getName());
            Conta conta = service.migrarPlano(logado, plano);
            return ResponseEntity.ok("Plano da conta alterado para " + conta.getPlano());
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
