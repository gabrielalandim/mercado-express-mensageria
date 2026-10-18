package br.com.fiap.mercadoexpress.loja.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutForm {

    @NotBlank(message = "Informe seu nome")
    private String clienteNome;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    private String clienteEmail;

    @NotBlank(message = "Informe seu CPF")
    private String clienteCpf;

    @NotBlank(message = "Informe a conta do Banco Tranquilo")
    private String numeroConta;
}
