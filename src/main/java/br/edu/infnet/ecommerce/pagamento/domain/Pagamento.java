package br.edu.infnet.ecommerce.pagamento.domain;

import br.edu.infnet.ecommerce.pagamento.domain.event.EventoDominio;
import br.edu.infnet.ecommerce.pagamento.domain.event.PagamentoAprovadoEvent;
import br.edu.infnet.ecommerce.pagamento.domain.event.PagamentoRecusadoEvent;
import br.edu.infnet.ecommerce.pagamento.domain.port.ProcessadorCartao;
import br.edu.infnet.ecommerce.pagamento.domain.port.ResultadoProcessadorCartao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pagamento {

    private PagamentoId id;
    private final Long pedidoId;
    private final Long usuarioId;
    private final Dinheiro valor;
    private final FormaPagamento formaPagamento;
    private final NumeroCartao numeroCartao;
    private StatusPagamento status;
    private String motivo;
    private String codigoAutorizacao;
    private final LocalDateTime processadoEm;

    private final List<EventoDominio> eventos = new ArrayList<>();

    private Pagamento(
            Long pedidoId,
            Long usuarioId,
            Dinheiro valor,
            FormaPagamento formaPagamento,
            NumeroCartao numeroCartao,
            StatusPagamento status,
            String motivo,
            String codigoAutorizacao,
            LocalDateTime processadoEm
    ) {
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.numeroCartao = numeroCartao;
        this.status = status;
        this.motivo = motivo;
        this.codigoAutorizacao = codigoAutorizacao;
        this.processadoEm = processadoEm;
    }

    public static Pagamento criar(
            Long pedidoId,
            Long usuarioId,
            Dinheiro valor,
            FormaPagamento formaPagamento,
            NumeroCartao numeroCartao,
            ProcessadorCartao processadorCartao
    ) {
        if (valor.menorOuIgualAZero()) {
            Pagamento p = recusado(pedidoId, usuarioId, valor, formaPagamento, numeroCartao, "VALOR_INVALIDO");
            p.registrarEvento(new PagamentoRecusadoEvent(
                    pedidoId, usuarioId, valor.valor(), "VALOR_INVALIDO", StatusPagamento.RECUSADO.name()
            ));
            return p;
        }

        Dinheiro limite = Dinheiro.de(new java.math.BigDecimal("10000.00"));
        if (valor.maiorQue(limite)) {
            Pagamento p = recusado(pedidoId, usuarioId, valor, formaPagamento, numeroCartao, "LIMITE_EXCEDIDO");
            p.registrarEvento(new PagamentoRecusadoEvent(
                    pedidoId, usuarioId, valor.valor(), "LIMITE_EXCEDIDO", StatusPagamento.RECUSADO.name()
            ));
            return p;
        }

        if (numeroCartao.terminaCom("0000")) {
            Pagamento p = bloqueado(pedidoId, usuarioId, valor, formaPagamento, numeroCartao, "CARTAO_BLOQUEADO");
            p.registrarEvento(new PagamentoRecusadoEvent(
                    pedidoId, usuarioId, valor.valor(), "CARTAO_BLOQUEADO", StatusPagamento.BLOQUEADO.name()
            ));
            return p;
        }

        ResultadoProcessadorCartao resultado = processadorCartao.processar(valor, numeroCartao);

        if (!resultado.aprovado()) {
            Pagamento p = recusado(pedidoId, usuarioId, valor, formaPagamento, numeroCartao, resultado.motivo());
            p.registrarEvento(new PagamentoRecusadoEvent(
                    pedidoId, usuarioId, valor.valor(), resultado.motivo(), StatusPagamento.RECUSADO.name()
            ));
            return p;
        }

        Pagamento p = new Pagamento(
                pedidoId,
                usuarioId,
                valor,
                formaPagamento,
                numeroCartao,
                StatusPagamento.APROVADO,
                null,
                resultado.codigoAutorizacao(),
                LocalDateTime.now()
        );
        p.registrarEvento(new PagamentoAprovadoEvent(
                null, pedidoId, usuarioId, valor.valor(), resultado.codigoAutorizacao()
        ));
        return p;
    }

    public static Pagamento reconstituir(
            PagamentoId id,
            Long pedidoId,
            Long usuarioId,
            Dinheiro valor,
            FormaPagamento formaPagamento,
            NumeroCartao numeroCartao,
            StatusPagamento status,
            String motivo,
            String codigoAutorizacao,
            LocalDateTime processadoEm
    ) {
        Pagamento pagamento = new Pagamento(
                pedidoId, usuarioId, valor, formaPagamento,
                numeroCartao, status, motivo, codigoAutorizacao, processadoEm
        );
        pagamento.id = id;
        return pagamento;
    }

    private static Pagamento recusado(
            Long pedidoId,
            Long usuarioId,
            Dinheiro valor,
            FormaPagamento formaPagamento,
            NumeroCartao numeroCartao,
            String motivo
    ) {
        return new Pagamento(
                pedidoId, usuarioId, valor, formaPagamento,
                numeroCartao, StatusPagamento.RECUSADO, motivo, null, LocalDateTime.now()
        );
    }

    private static Pagamento bloqueado(
            Long pedidoId,
            Long usuarioId,
            Dinheiro valor,
            FormaPagamento formaPagamento,
            NumeroCartao numeroCartao,
            String motivo
    ) {
        return new Pagamento(
                pedidoId, usuarioId, valor, formaPagamento,
                numeroCartao, StatusPagamento.BLOQUEADO, motivo, null, LocalDateTime.now()
        );
    }

    private void registrarEvento(EventoDominio evento) {
        this.eventos.add(evento);
    }

    public List<EventoDominio> getEventos() {
        return Collections.unmodifiableList(eventos);
    }

    public void limparEventos() {
        this.eventos.clear();
    }

    public boolean foiAprovado() {
        return status == StatusPagamento.APROVADO;
    }

    public PagamentoId getId() {
        return id;
    }

    public void setId(PagamentoId id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public NumeroCartao getNumeroCartao() {
        return numeroCartao;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }

    public LocalDateTime getProcessadoEm() {
        return processadoEm;
    }
}
