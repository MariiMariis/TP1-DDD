package br.edu.infnet.ecommerce.pagamento.integration;

import br.edu.infnet.ecommerce.pagamento.application.PagamentoApplicationService;
import br.edu.infnet.ecommerce.pagamento.application.ProcessarPagamentoCommand;
import br.edu.infnet.ecommerce.pagamento.application.ResultadoPagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoFacadeImpl implements PagamentoFacade {

    private final PagamentoApplicationService applicationService;

    public PagamentoFacadeImpl(PagamentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public ResultadoPagamento processar(ProcessarPagamentoCommand command) {
        return applicationService.processar(command);
    }
}
