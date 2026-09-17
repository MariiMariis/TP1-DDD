package br.edu.infnet.ecommerce.pagamento.domain;

import java.util.Objects;

public final class PagamentoId {

    private final Long valor;

    public PagamentoId(Long valor) {
        this.valor = valor;
    }

    public Long valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagamentoId that = (PagamentoId) o;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return "PagamentoId{" + valor + "}";
    }
}
