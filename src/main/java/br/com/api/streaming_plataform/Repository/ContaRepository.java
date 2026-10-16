package br.com.api.streaming_plataform.Repository;

import br.com.api.streaming_plataform.Model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContaRepository extends JpaRepository<Conta, UUID> {
}
