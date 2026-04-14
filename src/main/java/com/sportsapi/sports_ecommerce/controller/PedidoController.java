package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Pedido;
import com.sportsapi.sports_ecommerce.repository.PedidoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Processamento de vendas")
public class PedidoController {

    @Autowired
    private PedidoRepository repository;

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Lista todas as vendas realizadas")
    public ResponseEntity<Page<Pedido>> listar(Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @PostMapping
    @Operation(summary = "Fechar pedido", description = "Cria um novo pedido. Requer Idempotency-Key")
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido, @RequestHeader("Idempotency-Key") String key) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(pedido));
    }
}