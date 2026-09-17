package br.edu.infnet.ecommerce.pagamento.integration;

import br.edu.infnet.ecommerce.pagamento.application.ProcessarPagamentoCommand;
import br.edu.infnet.ecommerce.pagamento.application.ResultadoPagamento;

public interface PagamentoFacade {
    ResultadoPagamento processar(ProcessarPagamentoCommand command);
}
