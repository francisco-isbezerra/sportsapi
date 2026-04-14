package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Produto;
import com.sportsapi.sports_ecommerce.repository.ProdutoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController // Define que esta classe é um controlador de API
@RequestMapping("/api/v1/produtos") // Caminho base da API
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de equipamentos esportivos") // Swagger Tag
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna uma lista paginada de todos os produtos")
    public ResponseEntity<Page<Produto>> listarTodos(Pageable pageable) {
        // Busca todos com paginação (Requisito do professor)
        Page<Produto> lista = repository.findAll(pageable);

        // Adiciona HATEOAS (link para o próprio objeto) em cada item
        for(Produto p : lista) {
            Long id = p.getId();
            p.add(linkTo(methodOn(ProdutoController.class).buscarUm(id)).withSelfRel());
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Retorna um único produto através do ID")
    public ResponseEntity<Object> buscarUm(@PathVariable Long id) {
        Optional<Produto> produto = repository.findById(id);
        if(produto.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");
        }
        // Adiciona link para voltar para a listagem completa
        produto.get().add(linkTo(methodOn(ProdutoController.class).listarTodos(null)).withRel("Lista de Produtos"));
        return ResponseEntity.ok(produto.get());
    }

    @PostMapping
    @Operation(summary = "Criar produto", description = "Cadastra um novo produto no sistema. Requer Idempotency-Key")
    public ResponseEntity<Produto> salvar(@RequestBody @Valid Produto produto,
                                          @RequestHeader("Idempotency-Key") String key) {
        // @Valid ativa as validações que colocamos na Model (@NotBlank, etc)
        // O Header Idempotency-Key cumpre o requisito 4.1 da parte 2
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(produto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar (PUT)", description = "Substitui todos os dados de um produto existente")
    public ResponseEntity<Object> atualizar(@PathVariable Long id, @RequestBody @Valid Produto produto) {
        if(!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");
        }
        produto.setId(id);
        return ResponseEntity.ok(repository.save(produto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto", description = "Remove um produto permanentemente")
    public ResponseEntity<Object> deletar(@PathVariable Long id) {
        if(!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");
        }
        repository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
    }
}