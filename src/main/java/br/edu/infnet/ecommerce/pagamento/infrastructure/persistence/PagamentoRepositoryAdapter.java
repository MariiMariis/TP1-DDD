package br.edu.infnet.ecommerce.pagamento.infrastructure.persistence;

import br.edu.infnet.ecommerce.pagamento.domain.Pagamento;
import br.edu.infnet.ecommerce.pagamento.domain.port.PagamentoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PagamentoRepositoryAdapter implements PagamentoRepository {

    private final PagamentoJpaRepository jpaRepository;

    public PagamentoRepositoryAdapter(PagamentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pagamento salvar(Pagamento pagamento) {
        PagamentoJpaEntity entity = PagamentoJpaEntity.fromDomain(pagamento);
        PagamentoJpaEntity salvo = jpaRepository.save(entity);
        return salvo.toDomain();
    }

    @Override
    public Optional<Pagamento> buscarPorPedidoId(Long pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId)
                .map(PagamentoJpaEntity::toDomain);
    }
}
