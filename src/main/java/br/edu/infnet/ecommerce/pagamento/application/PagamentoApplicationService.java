package br.edu.infnet.ecommerce.pagamento.application;

import br.edu.infnet.ecommerce.pagamento.domain.Dinheiro;
import br.edu.infnet.ecommerce.pagamento.domain.FormaPagamento;
import br.edu.infnet.ecommerce.pagamento.domain.NumeroCartao;
import br.edu.infnet.ecommerce.pagamento.domain.Pagamento;
import br.edu.infnet.ecommerce.pagamento.domain.port.PagamentoRepository;
import br.edu.infnet.ecommerce.pagamento.domain.port.ProcessadorCartao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagamentoApplicationService {

    private final PagamentoRepository pagamentoRepository;
    private final ProcessadorCartao processadorCartao;

    public PagamentoApplicationService(
            PagamentoRepository pagamentoRepository,
            ProcessadorCartao processadorCartao
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.processadorCartao = processadorCartao;
    }

    @Transactional
    public ResultadoPagamento processar(ProcessarPagamentoCommand command) {
        Dinheiro valor = Dinheiro.de(command.valor());
        NumeroCartao numeroCartao = new NumeroCartao(command.numeroCartao());
        FormaPagamento formaPagamento = FormaPagamento.valueOf(command.formaPagamento().toUpperCase());

        Pagamento pagamento = Pagamento.criar(
                command.pedidoId(),
                command.usuarioId(),
                valor,
                formaPagamento,
                numeroCartao,
                processadorCartao
        );

        Pagamento salvo = pagamentoRepository.salvar(pagamento);

        return new ResultadoPagamento(
                salvo.getId().valor(),
                salvo.getStatus().name(),
                salvo.getMotivo(),
                salvo.getCodigoAutorizacao(),
                salvo.foiAprovado()
        );
    }
}
