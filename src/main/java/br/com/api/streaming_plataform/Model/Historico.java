package br.com.api.streaming_plataform.Model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

// Cada vez que um usuario assiste um filme, uma linha nova e criada aqui.
// Assim, se ele assistir o mesmo filme 2 vezes, aparecem 2 linhas (repeticao).
@Entity(name = "historico")
@Table(name = "historicos")
public class Historico implements Serializable {

    private static final long serialVersionUID = 1L;

    // Aqui o id e um numero que sempre cresce (1, 2, 3...), diferente do UUID.
    // Isso garante a ordem em que os filmes foram assistidos.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuarios usuario;

    @ManyToOne
    @JoinColumn(name = "filme_id", nullable = false)
    private Filmes filme;

    @Column(name = "assistido_em", nullable = false)
    private LocalDateTime assistidoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    public Filmes getFilme() {
        return filme;
    }

    public void setFilme(Filmes filme) {
        this.filme = filme;
    }

    public LocalDateTime getAssistidoEm() {
        return assistidoEm;
    }

    public void setAssistidoEm(LocalDateTime assistidoEm) {
        this.assistidoEm = assistidoEm;
    }
}
