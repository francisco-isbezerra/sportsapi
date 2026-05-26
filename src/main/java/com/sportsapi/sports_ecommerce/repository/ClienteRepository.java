package com.sportsapi.sports_ecommerce.repository;

import com.sportsapi.sports_ecommerce.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    org.springframework.data.domain.Page<Cliente> findByNomeContainingIgnoreCase(String nome, org.springframework.data.domain.Pageable pageable);
}