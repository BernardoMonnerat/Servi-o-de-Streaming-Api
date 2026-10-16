package br.com.api.streaming_plataform.Repository;

import br.com.api.streaming_plataform.Enum.TipoAcessos;
import br.com.api.streaming_plataform.Model.Acessos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcessoRepository extends JpaRepository<Acessos, UUID> {

    // Retorna null se nao encontrar
    Acessos findByAcesso(TipoAcessos acesso);
}
