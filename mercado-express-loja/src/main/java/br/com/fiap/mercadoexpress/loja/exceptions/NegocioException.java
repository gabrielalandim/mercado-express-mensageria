package br.com.fiap.mercadoexpress.loja.exceptions;

public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
