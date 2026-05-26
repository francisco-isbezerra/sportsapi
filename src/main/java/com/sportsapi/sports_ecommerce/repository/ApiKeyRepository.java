package com.sportsapi.sports_ecommerce.repository;

import com.sportsapi.sports_ecommerce.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByChave(String chave);
    Optional<ApiKey> findByChaveAndAtivoTrue(String chave);
    org.springframework.data.domain.Page<ApiKey> findByUsuarioId(Long usuarioId, org.springframework.data.domain.Pageable pageable);
}
