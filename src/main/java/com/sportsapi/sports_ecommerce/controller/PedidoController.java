package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Pedido;
import com.sportsapi.sports_ecommerce.repository.PedidoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Gerenciamento de vendas e processamento de pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository repository;

    // 1. GET - Recupera dados (Seguro e Idempotente)
    @GetMapping
    @Operation(summary = "Listar Pedidos (GET)", description = "Retorna todos os pedidos realizados com paginação")
    public ResponseEntity<Page<Pedido>> listar(Pageable pageable) {
        Page<Pedido> lista = repository.findAll(pageable);
        for(Pedido p : lista) {
            p.add(linkTo(methodOn(PedidoController.class).buscarPorId(p.getId())).withSelfRel());
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Busca os detalhes de um pedido específico")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(p -> ResponseEntity.ok(p.add(linkTo(methodOn(PedidoController.class).listar(null)).withRel("todos-pedidos"))))
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Cria novo recurso (Não seguro / Não idempotente)
    @PostMapping
    @Operation(summary = "Fechar Pedido (POST)", description = "Registra uma nova venda. Requer Idempotency-Key no Header")
    public ResponseEntity<Pedido> criar(@RequestBody @Valid Pedido pedido,
                                        @RequestHeader("Idempotency-Key") String key) {
        // O salvamento do pedido associa o cliente e os produtos selecionados
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(pedido));
    }

    // 3. PUT - Atualiza/Substitui tudo (Idempotente)
    @PutMapping("/{id}")
    @Operation(summary = "Substituir Pedido (PUT)", description = "Substitui completamente os dados de um pedido existente")
    public ResponseEntity<Pedido> substituir(@PathVariable Long id, @RequestBody @Valid Pedido novoPedido) {
        return repository.findById(id).map(pedido -> {
            pedido.setDataPedido(novoPedido.getDataPedido());
            pedido.setCliente(novoPedido.getCliente());
            pedido.setProdutos(novoPedido.getProdutos());
            return ResponseEntity.ok(repository.save(pedido));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. PATCH - Atualização Parcial (Modificações específicas)
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar Parcial (PATCH)", description = "Permite alterar campos específicos, como a lista de produtos")
    public ResponseEntity<Pedido> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Pedido> pedidoOpt = repository.findById(id);
        if (pedidoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Pedido pedido = pedidoOpt.get();

        // Se o mapa contiver a chave "dataPedido", atualiza apenas a data
        if (campos.containsKey("dataPedido")) {
            // Nota: Em um cenário real, precisaríamos converter a String do JSON para LocalDateTime
            // Aqui estamos focando na estrutura do método PATCH solicitado
        }

        return ResponseEntity.ok(repository.save(pedido));
    }

    // 5. DELETE - Remove o recurso (Idempotente)
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Pedido (DELETE)", description = "Cancela e exclui um pedido do sistema")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}