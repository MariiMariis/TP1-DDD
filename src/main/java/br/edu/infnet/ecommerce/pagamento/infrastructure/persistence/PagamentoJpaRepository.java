package br.edu.infnet.ecommerce.pagamento.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, Long> {
    Optional<PagamentoJpaEntity> findByPedidoId(Long pedidoId);
}
