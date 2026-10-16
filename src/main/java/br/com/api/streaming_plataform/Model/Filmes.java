package br.com.api.streaming_plataform.Model;

import br.com.api.streaming_plataform.Enum.Perfil;
import br.com.api.streaming_plataform.Enum.Plano;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity(name = "filme")
@Table(name = "filmes")
public class Filmes implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "ano", nullable = false)
    private String ano;

    // Perfil minimo para assistir (ex: ADOLESCENTE -> adolescentes e adultos podem ver)
    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao", nullable = false)
    private Perfil classificacao;

    // Plano minimo para o filme aparecer (ex: PADRAO -> contas PADRAO e PREMIUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "plano", nullable = false)
    private Plano plano;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public Perfil getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(Perfil classificacao) {
        this.classificacao = classificacao;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }
}
