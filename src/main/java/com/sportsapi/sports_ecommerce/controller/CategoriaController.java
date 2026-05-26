package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Categoria;
import com.sportsapi.sports_ecommerce.repository.CategoriaRepository;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de categorias com os 5 métodos HTTP e HATEOAS")
public class CategoriaController {

    @Autowired
    private CategoriaRepository repository;

    // Converte a entidade em EntityModel com links HATEOAS (self, update, patch, delete, colecao)
    private EntityModel<Categoria> toModel(Categoria c) {
        return EntityModel.of(c,
                linkTo(methodOn(CategoriaController.class).buscar(c.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).substituir(c.getId(), null)).withRel("update"),
                linkTo(methodOn(CategoriaController.class).atualizarParcial(c.getId(), null)).withRel("patch"),
                linkTo(methodOn(CategoriaController.class).deletar(c.getId())).withRel("delete"),
                linkTo(methodOn(CategoriaController.class).listar(null, null)).withRel("colecao")
        );
    }

    @GetMapping
    @Operation(summary = "Listar (GET) - Paginado", description = "Busca categorias de forma paginada usando PagedModel com links de paginação.")
    public ResponseEntity<PagedModel<EntityModel<Categoria>>> listar(
            Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Categoria> assembler) {
        Page<Categoria> lista = repository.findAll(pageable);
        PagedModel<EntityModel<Categoria>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Recupera uma categoria específica com links HATEOAS.")
    public ResponseEntity<EntityModel<Categoria>> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(c -> ResponseEntity.ok(toModel(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar-por-nome")
    @Operation(summary = "Consulta Personalizada - Buscar por Nome (GET)", description = "Busca categorias cujo nome contém o texto fornecido (case-insensitive).")
    public ResponseEntity<PagedModel<EntityModel<Categoria>>> buscarPorNome(
            @RequestParam String nome,
            Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Categoria> assembler) {
        Page<Categoria> lista = repository.findByNomeContainingIgnoreCase(nome, pageable);
        PagedModel<EntityModel<Categoria>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    @Operation(summary = "Criar (POST)", description = "Cria uma nova categoria. Requer cabeçalho X-Idempotency-Key para evitar duplicados.")
    public ResponseEntity<EntityModel<Categoria>> criar(
            @RequestBody @Valid Categoria categoria,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        Categoria salva = repository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salva));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Atualiza todos os campos de uma categoria.")
    public ResponseEntity<EntityModel<Categoria>> substituir(@PathVariable Long id, @RequestBody @Valid Categoria novaCategoria) {
        return repository.findById(id).map(categoria -> {
            categoria.setNome(novaCategoria.getNome());
            Categoria salva = repository.save(categoria);
            return ResponseEntity.ok(toModel(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Parcial (PATCH)", description = "Atualiza apenas os campos enviados no corpo.")
    public ResponseEntity<EntityModel<Categoria>> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Categoria> categoriaAtual = repository.findById(id);

        if (categoriaAtual.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Categoria categoria = categoriaAtual.get();
        if (campos.containsKey("nome")) {
            categoria.setNome((String) campos.get("nome"));
        }

        Categoria salva = repository.save(categoria);
        return ResponseEntity.ok(toModel(salva));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui uma categoria do sistema.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}