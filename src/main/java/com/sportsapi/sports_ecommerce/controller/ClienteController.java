package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Cliente;
import com.sportsapi.sports_ecommerce.repository.ClienteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gerenciamento de clientes com suporte aos 5 métodos HTTP e HATEOAS")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    private EntityModel<Cliente> toModel(Cliente c) {
        return EntityModel.of(c,
                linkTo(methodOn(ClienteController.class).buscar(c.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).substituir(c.getId(), null)).withRel("update"),
                linkTo(methodOn(ClienteController.class).atualizarParcial(c.getId(), null)).withRel("patch"),
                linkTo(methodOn(ClienteController.class).deletar(c.getId())).withRel("delete"),
                linkTo(methodOn(ClienteController.class).listar(null, null)).withRel("colecao")
        );
    }

    @GetMapping
    @Operation(summary = "Listar Clientes (GET)", description = "Retorna uma lista paginada de clientes com links de paginação HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes recuperada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<PagedModel<EntityModel<Cliente>>> listar(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Cliente> assembler) {
        Page<Cliente> lista = repository.findAll(pageable);
        PagedModel<EntityModel<Cliente>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Recupera um cliente específico com links HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente recuperado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Cliente>> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(c -> ResponseEntity.ok(toModel(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar-por-nome")
    @Operation(summary = "Consulta Personalizada - Buscar por Nome (GET)", description = "Retorna clientes cujo nome contém o texto especificado (case-insensitive).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Clientes filtrados por nome com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<PagedModel<EntityModel<Cliente>>> buscarPorNome(
            @RequestParam String nome,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Cliente> assembler) {
        Page<Cliente> lista = repository.findByNomeContainingIgnoreCase(nome, pageable);
        PagedModel<EntityModel<Cliente>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    @Operation(summary = "Criar Cliente (POST)", description = "Cadastra um cliente e seu endereço. Requer cabeçalho X-Idempotency-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou cabeçalho X-Idempotency-Key ausente."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "409", description = "Conflito. Chave de idempotência já utilizada ou violação de integridade no banco."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Cliente>> criar(
            @RequestBody @Valid Cliente cliente,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        Cliente salvo = repository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Atualiza todos os dados do cliente e do endereço associado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente substituído com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Cliente>> substituir(@PathVariable Long id, @RequestBody @Valid Cliente novoCliente) {
        return repository.findById(id).map(cliente -> {
            cliente.setNome(novoCliente.getNome());
            cliente.setEndereco(novoCliente.getEndereco());
            Cliente salvo = repository.save(cliente);
            return ResponseEntity.ok(toModel(salvo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar Parcial (PATCH)", description = "Atualiza apenas os campos enviados no corpo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Cliente>> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Cliente> clienteOpt = repository.findById(id);
        if (clienteOpt.isEmpty()) return ResponseEntity.notFound().build();

        Cliente cliente = clienteOpt.get();

        if (campos.containsKey("nome")) {
            cliente.setNome((String) campos.get("nome"));
        }

        if (campos.containsKey("logradouro") && cliente.getEndereco() != null) {
            cliente.getEndereco().setLogradouro((String) campos.get("logradouro"));
        }

        if (campos.containsKey("cep") && cliente.getEndereco() != null) {
            cliente.getEndereco().setCep((String) campos.get("cep"));
        }

        Cliente salvo = repository.save(cliente);
        return ResponseEntity.ok(toModel(salvo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui o cliente e seu endereço associado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente excluído com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}