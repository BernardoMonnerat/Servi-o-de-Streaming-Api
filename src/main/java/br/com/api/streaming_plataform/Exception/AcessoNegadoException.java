package br.com.api.streaming_plataform.Exception;

// Usada quando o usuario nao tem permissao para fazer algo (vira 403 Forbidden)
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
