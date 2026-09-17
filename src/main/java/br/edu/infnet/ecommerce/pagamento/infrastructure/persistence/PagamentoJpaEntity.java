package br.edu.infnet.ecommerce.pagamento.infrastructure.persistence;

import br.edu.infnet.ecommerce.pagamento.domain.Dinheiro;
import br.edu.infnet.ecommerce.pagamento.domain.FormaPagamento;
import br.edu.infnet.ecommerce.pagamento.domain.NumeroCartao;
import br.edu.infnet.ecommerce.pagamento.domain.Pagamento;
import br.edu.infnet.ecommerce.pagamento.domain.PagamentoId;
import br.edu.infnet.ecommerce.pagamento.domain.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
public class PagamentoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    private String numeroCartaoMascarado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    private String motivo;

    private String codigoAutorizacao;

    @Column(nullable = false)
    private LocalDateTime processadoEm;

    protected PagamentoJpaEntity() {
    }

    public static PagamentoJpaEntity fromDomain(Pagamento pagamento) {
        PagamentoJpaEntity entity = new PagamentoJpaEntity();
        if (pagamento.getId() != null) {
            entity.id = pagamento.getId().valor();
        }
        entity.pedidoId = pagamento.getPedidoId();
        entity.usuarioId = pagamento.getUsuarioId();
        entity.valor = pagamento.getValor().valor();
        entity.formaPagamento = pagamento.getFormaPagamento();
        entity.numeroCartaoMascarado = pagamento.getNumeroCartao().mascarado();
        entity.status = pagamento.getStatus();
        entity.motivo = pagamento.getMotivo();
        entity.codigoAutorizacao = pagamento.getCodigoAutorizacao();
        entity.processadoEm = pagamento.getProcessadoEm();
        return entity;
    }

    public Pagamento toDomain() {
        return Pagamento.reconstituir(
                new PagamentoId(id),
                pedidoId,
                usuarioId,
                Dinheiro.de(valor),
                formaPagamento,
                new NumeroCartao(extrairUltimosDigitos()),
                status,
                motivo,
                codigoAutorizacao,
                processadoEm
        );
    }

    private String extrairUltimosDigitos() {
        if (numeroCartaoMascarado == null || numeroCartaoMascarado.length() < 4) {
            return "0000";
        }
        return numeroCartaoMascarado.substring(numeroCartaoMascarado.length() - 4);
    }

    public Long getId() {
        return id;
    }
}
