package br.com.api.streaming_plataform.Repository;

import br.com.api.streaming_plataform.Model.Conta;
import br.com.api.streaming_plataform.Model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuarios, UUID> {

    // Retorna null se nao encontrar
    Usuarios findByEmail(String email);

    // Quantos usuarios (assinante + nao assinantes) existem na conta
    long countByConta(Conta conta);
}
