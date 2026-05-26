package com.sportsapi.sports_ecommerce.repository;

import com.sportsapi.sports_ecommerce.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    org.springframework.data.domain.Page<Usuario> findByNomeContainingIgnoreCase(String nome, org.springframework.data.domain.Pageable pageable);
}
