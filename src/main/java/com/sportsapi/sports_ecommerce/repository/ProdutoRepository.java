package com.sportsapi.sports_ecommerce.repository;

import com.sportsapi.sports_ecommerce.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Aqui já ganhamos: save(), findAll(), findById(), delete() e Paginação!
}