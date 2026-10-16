package br.com.api.streaming_plataform.Repository;

import br.com.api.streaming_plataform.Model.Historico;
import br.com.api.streaming_plataform.Model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    // Ordem crescente de id = ordem em que os filmes foram assistidos
    List<Historico> findByUsuarioOrderByIdAsc(Usuarios usuario);
}
