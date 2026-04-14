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

import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias") // REMOVIDO O V1 como você pediu
@Tag(name = "Categorias", description = "Gerenciamento de categorias com os 5 métodos HTTP")
public class CategoriaController {

    @Autowired
    private CategoriaRepository repository;

    // 1. GET - Recupera dados (Seguro e Idempotente)
    @GetMapping
    @Operation(summary = "Listar (GET)", description = "Busca categorias de forma paginada")
    public ResponseEntity<Page<Categoria>> listar(Pageable pageable) {
        Page<Categoria> lista = repository.findAll(pageable);
        for(Categoria c : lista) {
            c.add(linkTo(methodOn(CategoriaController.class).buscar(c.getId())).withSelfRel());
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Cria novo recurso (Não é seguro / Não é idempotente)
    @PostMapping
    @Operation(summary = "Criar (POST)", description = "Cria uma nova categoria")
    public ResponseEntity<Categoria> criar(@RequestBody @Valid Categoria categoria,
                                           @RequestHeader("Idempotency-Key") String key) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(categoria));
    }

    // 3. PUT - Atualiza/Substitui tudo (Idempotente)
    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Atualiza todos os campos de uma categoria")
    public ResponseEntity<Categoria> substituir(@PathVariable Long id, @RequestBody @Valid Categoria novaCategoria) {
        return repository.findById(id).map(categoria -> {
            categoria.setNome(novaCategoria.getNome());
            return ResponseEntity.ok(repository.save(categoria));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. PATCH - Atualização Parcial (Aplica modificações específicas)
    @PatchMapping("/{id}")
    @Operation(summary = "Parcial (PATCH)", description = "Atualiza apenas campos específicos enviados no corpo")
    public ResponseEntity<Categoria> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Categoria> categoriaAtual = repository.findById(id);

        if (categoriaAtual.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Categoria categoria = categoriaAtual.get();
        // Verifica se o campo "nome" veio no mapa e atualiza apenas ele
        if (campos.containsKey("nome")) {
            categoria.setNome((String) campos.get("nome"));
        }

        return ResponseEntity.ok(repository.save(categoria));
    }

    // 5. DELETE - Remove o recurso (Idempotente)
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui uma categoria do sistema")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}