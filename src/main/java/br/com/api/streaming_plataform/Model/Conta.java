package br.com.api.streaming_plataform.Model;

import br.com.api.streaming_plataform.Enum.Plano;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

// Uma conta tem um plano. Todos os usuarios da conta usam esse mesmo plano,
// entao quando o plano muda, muda para todos.
@Entity(name = "conta")
@Table(name = "contas")
public class Conta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "plano", nullable = false)
    private Plano plano;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }
}
