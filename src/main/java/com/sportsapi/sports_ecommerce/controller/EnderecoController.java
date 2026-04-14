package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Endereco;
import com.sportsapi.sports_ecommerce.repository.EnderecoRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enderecos")
@Tag(name = "Endereços", description = "Consulta de localizações")
public class EnderecoController {

    @Autowired
    private EnderecoRepository repository;

    @GetMapping("/{id}")
    public ResponseEntity<Endereco> buscar(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
