package br.com.api.streaming_plataform.Exception;

// Usada quando alguma regra de negocio nao foi respeitada (vira 400 Bad Request)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
