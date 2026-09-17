package br.edu.infnet.ecommerce.pagamento.domain;

import java.math.BigDecimal;
import java.util.Objects;

public final class Dinheiro {

    private final BigDecimal valor;

    public Dinheiro(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor não pode ser nulo");
        }
        this.valor = valor;
    }

    public static Dinheiro zero() {
        return new Dinheiro(BigDecimal.ZERO);
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public boolean menorOuIgualAZero() {
        return valor.compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean maiorQue(Dinheiro outro) {
        return this.valor.compareTo(outro.valor) > 0;
    }

    public BigDecimal valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dinheiro dinheiro = (Dinheiro) o;
        return valor.compareTo(dinheiro.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "R$ " + valor.toPlainString();
    }
}
