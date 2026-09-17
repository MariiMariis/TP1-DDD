package br.edu.infnet.ecommerce.pagamento.domain.event;

import java.math.BigDecimal;

public final class PagamentoAprovadoEvent extends EventoDominio {

    private final Long pagamentoId;
    private final Long pedidoId;
    private final Long usuarioId;
    private final BigDecimal valor;
    private final String codigoAutorizacao;

    public PagamentoAprovadoEvent(
            Long pagamentoId,
            Long pedidoId,
            Long usuarioId,
            BigDecimal valor,
            String codigoAutorizacao
    ) {
        super();
        this.pagamentoId = pagamentoId;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.valor = valor;
        this.codigoAutorizacao = codigoAutorizacao;
    }

    @Override
    public String tipo() {
        return "PAGAMENTO_APROVADO";
    }

    public Long getPagamentoId() {
        return pagamentoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }
}
