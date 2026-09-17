package br.edu.infnet.ecommerce.pagamento.domain.port;

import br.edu.infnet.ecommerce.pagamento.domain.Pagamento;

import java.util.Optional;

public interface PagamentoRepository {
    Pagamento salvar(Pagamento pagamento);
    Optional<Pagamento> buscarPorPedidoId(Long pedidoId);
}
