package br.com.api.streaming_plataform.Controller;

import br.com.api.streaming_plataform.DTO.HistoricoDTO;
import br.com.api.streaming_plataform.Exception.AcessoNegadoException;
import br.com.api.streaming_plataform.Exception.RegraNegocioException;
import br.com.api.streaming_plataform.Model.Filmes;
import br.com.api.streaming_plataform.Model.Historico;
import br.com.api.streaming_plataform.Model.Usuarios;
import br.com.api.streaming_plataform.Service.FilmeService;
import br.com.api.streaming_plataform.Service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FilmeController {

    private final FilmeService filmeService;
    private final UsuarioService usuarioService;

    public FilmeController(FilmeService filmeService, UsuarioService usuarioService) {
        this.filmeService = filmeService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/filmes")
    public ResponseEntity<Object> cadastrar(@RequestBody Filmes filme) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(filmeService.cadastrar(filme));
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lista so os filmes que o plano e o perfil de quem esta logado permitem
    @GetMapping("/filmes")
    public ResponseEntity<Object> listar(Authentication authentication) {
        Usuarios logado = usuarioService.buscarPorEmail(authentication.getName());
        return ResponseEntity.ok(filmeService.listarDisponiveis(logado));
    }

    @PostMapping("/filmes/{id}/assistir")
    public ResponseEntity<Object> assistir(@PathVariable UUID id, Authentication authentication) {
        try {
            Usuarios logado = usuarioService.buscarPorEmail(authentication.getName());
            Historico historico = filmeService.assistir(logado, id);
            return ResponseEntity.ok(new HistoricoDTO(historico));
        } catch (AcessoNegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RegraNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/historico")
    public ResponseEntity<Object> historico(Authentication authentication) {
        Usuarios logado = usuarioService.buscarPorEmail(authentication.getName());

        List<HistoricoDTO> resposta = new ArrayList<>();
        for (Historico historico : filmeService.historico(logado)) {
            resposta.add(new HistoricoDTO(historico));
        }
        return ResponseEntity.ok(resposta);
    }
}
