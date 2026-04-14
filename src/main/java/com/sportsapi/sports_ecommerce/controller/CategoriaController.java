package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Categoria;
import com.sportsapi.sports_ecommerce.repository.CategoriaRepository;
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
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de categorias de produtos")
public class CategoriaController {

    @Autowired
    private CategoriaRepository repository;

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Lista paginada de categorias")
    public ResponseEntity<Page<Categoria>> listar(Pageable pageable) {
        Page<Categoria> lista = repository.findAll(pageable);
        for(Categoria c : lista) {
            c.add(linkTo(methodOn(CategoriaController.class).buscar(c.getId())).withSelfRel());
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria", description = "Busca uma categoria por ID")
    public ResponseEntity<Categoria> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(c -> ResponseEntity.ok(c.add(linkTo(methodOn(CategoriaController.class).listar(null)).withRel("todas-categorias"))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria. Requer Idempotency-Key")
    public ResponseEntity<Categoria> criar(@RequestBody @Valid Categoria categoria, @RequestHeader("Idempotency-Key") String key) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(categoria));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar categoria", description = "Remove uma categoria")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}