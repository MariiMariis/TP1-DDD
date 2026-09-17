package br.edu.infnet.ecommerce.pagamento.application;

public record ResultadoPagamento(
        Long pagamentoId,
        String status,
        String motivo,
        String codigoAutorizacao,
        boolean aprovado
) {
}
