package br.com.fiap.bancotranquilo.exceptions;

public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
