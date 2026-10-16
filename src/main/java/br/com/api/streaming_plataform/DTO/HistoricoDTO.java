package br.com.api.streaming_plataform.DTO;

import br.com.api.streaming_plataform.Model.Historico;

import java.time.LocalDateTime;
import java.util.UUID;

// O que a API devolve para cada item do historico
public class HistoricoDTO {

    private UUID filmeId;
    private String titulo;
    private LocalDateTime assistidoEm;

    public HistoricoDTO(Historico historico) {
        this.filmeId = historico.getFilme().getId();
        this.titulo = historico.getFilme().getTitulo();
        this.assistidoEm = historico.getAssistidoEm();
    }

    public UUID getFilmeId() {
        return filmeId;
    }

    public String getTitulo() {
        return titulo;
    }

    public LocalDateTime getAssistidoEm() {
        return assistidoEm;
    }
}
