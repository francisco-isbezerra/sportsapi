package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Cliente;
import com.sportsapi.sports_ecommerce.repository.ClienteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gestão de clientes e endereços")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes cadastrados")
    public ResponseEntity<Page<Cliente>> listar(Pageable pageable) {
        Page<Cliente> clientes = repository.findAll(pageable);
        clientes.forEach(c -> c.add(linkTo(methodOn(ClienteController.class).buscar(c.getId())).withSelfRel()));
        return ResponseEntity.ok(clientes);
    }

    @PostMapping
    @Operation(summary = "Cadastrar cliente", description = "Cria um cliente e seu endereço simultaneamente")
    public ResponseEntity<Cliente> criar(@RequestBody @Valid Cliente cliente, @RequestHeader("Idempotency-Key") String key) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(cliente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscar(@PathVariable Long id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}