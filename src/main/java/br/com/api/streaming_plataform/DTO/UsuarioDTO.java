package br.com.api.streaming_plataform.DTO;

import br.com.api.streaming_plataform.Enum.Perfil;
import br.com.api.streaming_plataform.Enum.Plano;

// O que chega no corpo (JSON) da requisicao para criar um usuario.
// O plano so e usado na criacao do assinante (o nao assinante usa o plano da conta).
public class UsuarioDTO {

    private String email;
    private String senha;
    private Perfil perfil;
    private Plano plano;

    public UsuarioDTO() {
    }

    public UsuarioDTO(String email, String senha, Perfil perfil, Plano plano) {
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.plano = plano;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }
}
