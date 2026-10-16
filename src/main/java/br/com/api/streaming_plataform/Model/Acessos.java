package br.com.api.streaming_plataform.Model;

import br.com.api.streaming_plataform.Enum.TipoAcessos;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.UUID;

@Entity(name = "acesso")
@Table(name = "acessos")
public class Acessos implements Serializable, GrantedAuthority {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "acesso",  nullable = false)
    private TipoAcessos acesso;

    @Override
    public @Nullable String getAuthority() {
        return acesso.toString();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TipoAcessos getAcesso() {
        return acesso;
    }

    public void setAcesso(TipoAcessos acesso) {
        this.acesso = acesso;
    }
}
