package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Cliente;
import com.sportsapi.sports_ecommerce.model.Endereco;
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

import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Gerenciamento de clientes com suporte aos 5 métodos HTTP")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    // 1. GET - Recupera dados (Seguro e Idempotente)
    @GetMapping
    @Operation(summary = "Listar Clientes (GET)", description = "Retorna uma lista paginada de clientes")
    public ResponseEntity<Page<Cliente>> listar(Pageable pageable) {
        Page<Cliente> lista = repository.findAll(pageable);
        for(Cliente c : lista) {
            c.add(linkTo(methodOn(ClienteController.class).buscar(c.getId())).withSelfRel());
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Recupera um cliente específico")
    public ResponseEntity<Cliente> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(c -> ResponseEntity.ok(c.add(linkTo(methodOn(ClienteController.class).listar(null)).withRel("lista"))))
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Cria novo recurso (Não seguro / Não idempotente)
    @PostMapping
    @Operation(summary = "Criar Cliente (POST)", description = "Cadastra um cliente e seu endereço. Requer Idempotency-Key")
    public ResponseEntity<Cliente> criar(@RequestBody @Valid Cliente cliente,
                                         @RequestHeader("Idempotency-Key") String key) {
        // Devido ao CascadeType.ALL na Model, o endereço será salvo junto
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(cliente));
    }

    // 3. PUT - Atualiza/Substitui tudo (Idempotente)
    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Atualiza todos os dados do cliente e endereço")
    public ResponseEntity<Cliente> substituir(@PathVariable Long id, @RequestBody @Valid Cliente novoCliente) {
        return repository.findById(id).map(cliente -> {
            cliente.setNome(novoCliente.getNome());
            cliente.setEndereco(novoCliente.getEndereco());
            return ResponseEntity.ok(repository.save(cliente));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. PATCH - Atualização Parcial (Modificações específicas)
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar Parcial (PATCH)", description = "Atualiza apenas os campos enviados (ex: só o nome)")
    public ResponseEntity<Cliente> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Cliente> clienteOpt = repository.findById(id);
        if (clienteOpt.isEmpty()) return ResponseEntity.notFound().build();

        Cliente cliente = clienteOpt.get();

        // Atualiza o nome se enviado
        if (campos.containsKey("nome")) {
            cliente.setNome((String) campos.get("nome"));
        }

        // Exemplo de como atualizar um campo do Endereço via Patch
        if (campos.containsKey("logradouro") && cliente.getEndereco() != null) {
            cliente.getEndereco().setLogradouro((String) campos.get("logradouro"));
        }

        return ResponseEntity.ok(repository.save(cliente));
    }

    // 5. DELETE - Remove o recurso (Idempotente)
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui o cliente e seu endereço")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}