package br.edu.infnet.ecommerce.pagamento.domain;

import java.util.Objects;

public final class NumeroCartao {

    private final String numero;

    public NumeroCartao(String numero) {
        if (numero == null || numero.length() < 4) {
            throw new IllegalArgumentException("Número do cartão inválido");
        }
        this.numero = numero;
    }

    public String ultimosQuatroDigitos() {
        return numero.substring(numero.length() - 4);
    }

    public String mascarado() {
        return "**** **** **** " + ultimosQuatroDigitos();
    }

    public boolean terminaCom(String sufixo) {
        return numero.endsWith(sufixo);
    }

    public String valor() {
        return numero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumeroCartao that = (NumeroCartao) o;
        return Objects.equals(numero, that.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    @Override
    public String toString() {
        return mascarado();
    }
}
