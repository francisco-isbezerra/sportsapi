package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Endereco;
import com.sportsapi.sports_ecommerce.repository.EnderecoRepository;
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
@RequestMapping("/api/v1/enderecos")
@Tag(name = "Endereços", description = "Gerenciamento de endereços com suporte aos 5 métodos HTTP")
public class EnderecoController {

    @Autowired
    private EnderecoRepository repository;

    // 1. GET - Recupera dados (Seguro e Idempotente)
    @GetMapping
    @Operation(summary = "Listar Endereços (GET)", description = "Retorna uma lista paginada de todos os endereços")
    public ResponseEntity<Page<Endereco>> listar(Pageable pageable) {
        Page<Endereco> lista = repository.findAll(pageable);
        for(Endereco e : lista) {
            e.add(linkTo(methodOn(EnderecoController.class).buscarPorId(e.getId())).withSelfRel());
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Recupera um endereço específico pelo ID")
    public ResponseEntity<Endereco> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.add(linkTo(methodOn(EnderecoController.class).listar(null)).withRel("lista-enderecos"))))
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Cria novo recurso (Não seguro / Não idempotente)
    @PostMapping
    @Operation(summary = "Criar Endereço (POST)", description = "Cadastra um novo endereço no sistema. Requer Idempotency-Key")
    public ResponseEntity<Endereco> criar(@RequestBody @Valid Endereco endereco,
                                          @RequestHeader("Idempotency-Key") String key) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(endereco));
    }

    // 3. PUT - Atualiza/Substitui tudo (Idempotente)
    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Substitui completamente os dados de um endereço")
    public ResponseEntity<Endereco> substituir(@PathVariable Long id, @RequestBody @Valid Endereco novoEndereco) {
        return repository.findById(id).map(endereco -> {
            endereco.setLogradouro(novoEndereco.getLogradouro());
            endereco.setCep(novoEndereco.getCep());
            return ResponseEntity.ok(repository.save(endereco));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. PATCH - Atualização Parcial (Aplica modificações específicas)
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar Parcial (PATCH)", description = "Atualiza apenas campos específicos (ex: só o CEP)")
    public ResponseEntity<Endereco> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Endereco> enderecoOpt = repository.findById(id);
        if (enderecoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Endereco endereco = enderecoOpt.get();

        // Atualiza o logradouro se ele estiver no corpo da requisição
        if (campos.containsKey("logradouro")) {
            endereco.setLogradouro((String) campos.get("logradouro"));
        }

        // Atualiza o CEP se ele estiver no corpo da requisição
        if (campos.containsKey("cep")) {
            endereco.setCep((String) campos.get("cep"));
        }

        return ResponseEntity.ok(repository.save(endereco));
    }

    // 5. DELETE - Remove o recurso (Idempotente)
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui um endereço permanentemente")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}