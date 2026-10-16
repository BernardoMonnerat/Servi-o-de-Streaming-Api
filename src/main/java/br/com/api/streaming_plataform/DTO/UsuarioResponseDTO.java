package br.com.api.streaming_plataform.DTO;

import br.com.api.streaming_plataform.Enum.Perfil;
import br.com.api.streaming_plataform.Enum.Plano;
import br.com.api.streaming_plataform.Model.Usuarios;

import java.util.UUID;

// O que a API devolve sobre um usuario (sem a senha!)
public class UsuarioResponseDTO {

    private UUID id;
    private String email;
    private Perfil perfil;
    private Plano plano;
    private boolean assinante;

    public UsuarioResponseDTO(Usuarios usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.perfil = usuario.getPerfil();
        this.plano = usuario.getConta().getPlano();
        this.assinante = usuario.isAssinante();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Plano getPlano() {
        return plano;
    }

    public boolean isAssinante() {
        return assinante;
    }
}
