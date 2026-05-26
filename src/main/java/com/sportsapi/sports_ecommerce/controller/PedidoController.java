package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Pedido;
import com.sportsapi.sports_ecommerce.repository.PedidoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gerenciamento de vendas e processamento de pedidos com HATEOAS")
public class PedidoController {

    @Autowired
    private PedidoRepository repository;

    private EntityModel<Pedido> toModel(Pedido p) {
        return EntityModel.of(p,
                linkTo(methodOn(PedidoController.class).buscarPorId(p.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).substituir(p.getId(), null)).withRel("update"),
                linkTo(methodOn(PedidoController.class).atualizarParcial(p.getId(), null)).withRel("patch"),
                linkTo(methodOn(PedidoController.class).deletar(p.getId())).withRel("delete"),
                linkTo(methodOn(PedidoController.class).listar(null, null)).withRel("colecao")
        );
    }

    @GetMapping
    @Operation(summary = "Listar Pedidos (GET)", description = "Retorna todos os pedidos realizados com paginação e links HATEOAS.")
    public ResponseEntity<PagedModel<EntityModel<Pedido>>> listar(
            Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Pedido> assembler) {
        Page<Pedido> lista = repository.findAll(pageable);
        PagedModel<EntityModel<Pedido>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Busca os detalhes de um pedido específico com links HATEOAS.")
    public ResponseEntity<EntityModel<Pedido>> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(p -> ResponseEntity.ok(toModel(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar-por-cliente")
    @Operation(summary = "Consulta Personalizada - Buscar por Cliente (GET)", description = "Retorna uma lista paginada de todos os pedidos feitos por um cliente específico.")
    public ResponseEntity<PagedModel<EntityModel<Pedido>>> buscarPorCliente(
            @RequestParam Long clienteId,
            Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Pedido> assembler) {
        Page<Pedido> lista = repository.findByClienteId(clienteId, pageable);
        PagedModel<EntityModel<Pedido>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    @Operation(summary = "Fechar Pedido (POST)", description = "Registra uma nova venda. Requer cabeçalho X-Idempotency-Key.")
    public ResponseEntity<EntityModel<Pedido>> criar(
            @RequestBody @Valid Pedido pedido,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        Pedido salvo = repository.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substituir Pedido (PUT)", description = "Substitui completamente os dados de um pedido existente.")
    public ResponseEntity<EntityModel<Pedido>> substituir(@PathVariable Long id, @RequestBody @Valid Pedido novoPedido) {
        return repository.findById(id).map(pedido -> {
            pedido.setDataPedido(novoPedido.getDataPedido());
            pedido.setCliente(novoPedido.getCliente());
            pedido.setProdutos(novoPedido.getProdutos());
            Pedido salvo = repository.save(pedido);
            return ResponseEntity.ok(toModel(salvo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar Parcial (PATCH)", description = "Permite alterar campos específicos do pedido.")
    public ResponseEntity<EntityModel<Pedido>> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Pedido> pedidoOpt = repository.findById(id);
        if (pedidoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Pedido pedido = pedidoOpt.get();

        // No PATCH, atualizamos a data do pedido se enviada
        if (campos.containsKey("dataPedido")) {
            pedido.setDataPedido(java.time.LocalDateTime.parse(campos.get("dataPedido").toString()));
        }

        Pedido salva = repository.save(pedido);
        return ResponseEntity.ok(toModel(salva));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Pedido (DELETE)", description = "Exclui um pedido do sistema.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}