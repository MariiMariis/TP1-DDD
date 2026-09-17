package br.edu.infnet.ecommerce.pagamento.application;

import br.edu.infnet.ecommerce.pagamento.domain.Dinheiro;
import br.edu.infnet.ecommerce.pagamento.domain.FormaPagamento;
import br.edu.infnet.ecommerce.pagamento.domain.NumeroCartao;
import br.edu.infnet.ecommerce.pagamento.domain.Pagamento;
import br.edu.infnet.ecommerce.pagamento.domain.event.EventoDominio;
import br.edu.infnet.ecommerce.pagamento.domain.port.PagamentoRepository;
import br.edu.infnet.ecommerce.pagamento.domain.port.ProcessadorCartao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagamentoApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoApplicationService.class);

    private final PagamentoRepository pagamentoRepository;
    private final ProcessadorCartao processadorCartao;
    private final ApplicationEventPublisher eventPublisher;

    public PagamentoApplicationService(
            PagamentoRepository pagamentoRepository,
            ProcessadorCartao processadorCartao,
            ApplicationEventPublisher eventPublisher
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.processadorCartao = processadorCartao;
        this.eventPublisher = eventPublisher;
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

        publicarEventos(pagamento);

        return new ResultadoPagamento(
                salvo.getId().valor(),
                salvo.getStatus().name(),
                salvo.getMotivo(),
                salvo.getCodigoAutorizacao(),
                salvo.foiAprovado()
        );
    }

    private void publicarEventos(Pagamento pagamento) {
        for (EventoDominio evento : pagamento.getEventos()) {
            log.info("Publicando evento de dominio: {} [id={}]", evento.tipo(), evento.getEventId());
            eventPublisher.publishEvent(evento);
        }
        pagamento.limparEventos();
    }
}
