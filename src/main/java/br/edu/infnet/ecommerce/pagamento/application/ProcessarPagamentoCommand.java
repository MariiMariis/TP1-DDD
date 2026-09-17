package br.edu.infnet.ecommerce.pagamento.application;

import java.math.BigDecimal;

public record ProcessarPagamentoCommand(
        Long pedidoId,
        Long usuarioId,
        BigDecimal valor,
        String formaPagamento,
        String numeroCartao
) {
}
