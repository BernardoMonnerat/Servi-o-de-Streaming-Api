package br.com.api.streaming_plataform.Service;

import br.com.api.streaming_plataform.Exception.AcessoNegadoException;
import br.com.api.streaming_plataform.Exception.RegraNegocioException;
import br.com.api.streaming_plataform.Model.Filmes;
import br.com.api.streaming_plataform.Model.Historico;
import br.com.api.streaming_plataform.Model.Usuarios;
import br.com.api.streaming_plataform.Repository.FilmeRepository;
import br.com.api.streaming_plataform.Repository.HistoricoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FilmeService {

    private final FilmeRepository filmeRepository;
    private final HistoricoRepository historicoRepository;

    public FilmeService(FilmeRepository filmeRepository, HistoricoRepository historicoRepository) {
        this.filmeRepository = filmeRepository;
        this.historicoRepository = historicoRepository;
    }

    public Filmes cadastrar(Filmes filme) {
        if (filme.getTitulo() == null || filme.getAno() == null
                || filme.getClassificacao() == null || filme.getPlano() == null) {
            throw new RegraNegocioException("Título, ano, classificação e plano são obrigatórios");
        }
        filmeRepository.save(filme);
        return filme;
    }

    public List<Filmes> listarDisponiveis(Usuarios usuario) {
        List<Filmes> disponiveis = new ArrayList<>();
        for (Filmes filme : filmeRepository.findAll()) {
            if (planoPermite(usuario, filme) && perfilPermite(usuario, filme)) {
                disponiveis.add(filme);
            }
        }
        return disponiveis;
    }

    public Historico assistir(Usuarios usuario, UUID filmeId) {
        Filmes filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new RegraNegocioException("Filme não encontrado"));

        if (!planoPermite(usuario, filme)) {
            throw new AcessoNegadoException("Seu plano não inclui esse filme");
        }
        if (!perfilPermite(usuario, filme)) {
            throw new AcessoNegadoException("Seu perfil não permite assistir esse filme");
        }

        Historico historico = new Historico();
        historico.setUsuario(usuario);
        historico.setFilme(filme);
        historico.setAssistidoEm(LocalDateTime.now());
        historicoRepository.save(historico);
        return historico;
    }

    public List<Historico> historico(Usuarios usuario) {
        return historicoRepository.findByUsuarioOrderByIdAsc(usuario);
    }

    // ordinal() e a posicao no enum: BASICO = 0, PADRAO = 1, PREMIUM = 2.
    // O plano da conta precisa ser igual ou maior que o plano do filme.
    private boolean planoPermite(Usuarios usuario, Filmes filme) {
        return usuario.getConta().getPlano().ordinal() >= filme.getPlano().ordinal();
    }

    // INFANTIL = 0, ADOLESCENTE = 1, ADULTO = 2.
    // O perfil do usuario precisa ser igual ou maior que a classificacao do filme.
    private boolean perfilPermite(Usuarios usuario, Filmes filme) {
        return usuario.getPerfil().ordinal() >= filme.getClassificacao().ordinal();
    }
}
