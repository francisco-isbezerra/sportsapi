package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Endereco;
import com.sportsapi.sports_ecommerce.repository.EnderecoRepository;
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
@RequestMapping("/api/enderecos")
@Tag(name = "Endereços", description = "Gerenciamento de endereços com suporte aos 5 métodos HTTP e HATEOAS")
public class EnderecoController {

    @Autowired
    private EnderecoRepository repository;

    private EntityModel<Endereco> toModel(Endereco e) {
        return EntityModel.of(e,
                linkTo(methodOn(EnderecoController.class).buscarPorId(e.getId())).withSelfRel(),
                linkTo(methodOn(EnderecoController.class).substituir(e.getId(), null)).withRel("update"),
                linkTo(methodOn(EnderecoController.class).atualizarParcial(e.getId(), null)).withRel("patch"),
                linkTo(methodOn(EnderecoController.class).deletar(e.getId())).withRel("delete"),
                linkTo(methodOn(EnderecoController.class).listar(null, null)).withRel("colecao")
        );
    }

    @GetMapping
    @Operation(summary = "Listar Endereços (GET)", description = "Retorna uma lista paginada de todos os endereços com links HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de endereços recuperada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<PagedModel<EntityModel<Endereco>>> listar(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Endereco> assembler) {
        Page<Endereco> lista = repository.findAll(pageable);
        PagedModel<EntityModel<Endereco>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Recupera um endereço específico pelo ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço recuperado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Endereco>> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(toModel(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar-por-cep")
    @Operation(summary = "Consulta Personalizada - Buscar por CEP (GET)", description = "Retorna uma lista paginada de endereços filtrada por CEP exato.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereços filtrados por CEP recuperados com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<PagedModel<EntityModel<Endereco>>> buscarPorCep(
            @RequestParam String cep,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Endereco> assembler) {
        Page<Endereco> lista = repository.findByCep(cep, pageable);
        PagedModel<EntityModel<Endereco>> pagedModel = assembler.toModel(lista, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    @Operation(summary = "Criar Endereço (POST)", description = "Cadastra um novo endereço no sistema. Requer cabeçalho X-Idempotency-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou cabeçalho X-Idempotency-Key ausente."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "409", description = "Conflito. Chave de idempotência já utilizada."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Endereco>> criar(
            @RequestBody @Valid Endereco endereco,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        Endereco salvo = repository.save(endereco);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Substitui completamente os dados de um endereço.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço substituído com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Endereco>> substituir(@PathVariable Long id, @RequestBody @Valid Endereco novoEndereco) {
        return repository.findById(id).map(endereco -> {
            endereco.setLogradouro(novoEndereco.getLogradouro());
            endereco.setCep(novoEndereco.getCep());
            Endereco salvo = repository.save(endereco);
            return ResponseEntity.ok(toModel(salvo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar Parcial (PATCH)", description = "Atualiza apenas campos específicos do endereço.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Endereco>> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Endereco> enderecoOpt = repository.findById(id);
        if (enderecoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Endereco endereco = enderecoOpt.get();

        if (campos.containsKey("logradouro")) {
            endereco.setLogradouro((String) campos.get("logradouro"));
        }

        if (campos.containsKey("cep")) {
            endereco.setCep((String) campos.get("cep"));
        }

        Endereco salvo = repository.save(endereco);
        return ResponseEntity.ok(toModel(salvo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui um endereço permanentemente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Endereço excluído com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}