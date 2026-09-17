package br.edu.infnet.ecommerce.pagamento.domain.event;

import java.math.BigDecimal;

public final class PagamentoRecusadoEvent extends EventoDominio {

    private final Long pedidoId;
    private final Long usuarioId;
    private final BigDecimal valor;
    private final String motivo;
    private final String status;

    public PagamentoRecusadoEvent(
            Long pedidoId,
            Long usuarioId,
            BigDecimal valor,
            String motivo,
            String status
    ) {
        super();
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.valor = valor;
        this.motivo = motivo;
        this.status = status;
    }

    @Override
    public String tipo() {
        return "PAGAMENTO_RECUSADO";
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

    public String getMotivo() {
        return motivo;
    }

    public String getStatus() {
        return status;
    }
}
