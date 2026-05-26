package com.sportsapi.sports_ecommerce.repository;

import com.sportsapi.sports_ecommerce.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    org.springframework.data.domain.Page<Pedido> findByClienteId(Long clienteId, org.springframework.data.domain.Pageable pageable);
}