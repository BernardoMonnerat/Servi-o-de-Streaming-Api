package br.com.api.streaming_plataform.Repository;

import br.com.api.streaming_plataform.Model.Filmes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FilmeRepository extends JpaRepository<Filmes, UUID> {
}
