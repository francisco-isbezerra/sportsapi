package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Produto;
import com.sportsapi.sports_ecommerce.repository.ProdutoRepository;
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
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Gerenciamento de produtos com paginação, HATEOAS e Versionamento (X-API-Version)")
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    // --- CLASSE DTO PARA A VERSÃO 2 DA API ---
    public static class ProdutoV2Dto extends RepresentationModel<ProdutoV2Dto> {
        private Long id;
        private String nome;
        private Double preco;
        private String precoFormatado; // Campo extra formatado da V2
        private String condicao;
        private String categoriaNome; // Nome simplificado da categoria para o cliente da V2
        private String desconto; // NOVO: Campo extra da V2
        private Double precoComDesconto; // NOVO: Campo extra da V2

        public ProdutoV2Dto(Produto p) {
            this.id = p.getId();
            this.nome = p.getNome();
            this.preco = p.getPreco();
            this.precoFormatado = String.format("R$ %.2f", p.getPreco());
            this.condicao = p.getCondicao() != null ? p.getCondicao().name() : null;
            this.categoriaNome = p.getCategoria() != null ? p.getCategoria().getNome() : "Sem Categoria";
            this.desconto = "10% de Desconto Esportivo Especial!";
            this.precoComDesconto = p.getPreco() != null ? Math.round((p.getPreco() * 0.9) * 100.0) / 100.0 : 0.0;
        }

        // Getters e Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public Double getPreco() { return preco; }
        public void setPreco(Double preco) { this.preco = preco; }
        public String getPrecoFormatado() { return precoFormatado; }
        public void setPrecoFormatado(String precoFormatado) { this.precoFormatado = precoFormatado; }
        public String getCondicao() { return condicao; }
        public void setCondicao(String condicao) { this.condicao = condicao; }
        public String getCategoriaNome() { return categoriaNome; }
        public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }
        public String getDesconto() { return desconto; }
        public void setDesconto(String desconto) { this.desconto = desconto; }
        public Double getPrecoComDesconto() { return precoComDesconto; }
        public void setPrecoComDesconto(Double precoComDesconto) { this.precoComDesconto = precoComDesconto; }
    }

    // Assembler auxiliar para V1
    private EntityModel<Produto> toModelV1(Produto p) {
        return EntityModel.of(p,
                linkTo(methodOn(ProdutoController.class).buscarPorId(p.getId())).withSelfRel(),
                linkTo(methodOn(ProdutoController.class).substituir(p.getId(), null)).withRel("update"),
                linkTo(methodOn(ProdutoController.class).atualizarParcial(p.getId(), null)).withRel("patch"),
                linkTo(methodOn(ProdutoController.class).deletar(p.getId())).withRel("delete"),
                linkTo(methodOn(ProdutoController.class).listar(null, null, null)).withRel("colecao")
        );
    }

    // Assembler auxiliar para V2
    private EntityModel<ProdutoV2Dto> toModelV2(Produto p) {
        ProdutoV2Dto dto = new ProdutoV2Dto(p);
        return EntityModel.of(dto,
                linkTo(methodOn(ProdutoController.class).buscarPorId(p.getId())).withSelfRel(),
                linkTo(methodOn(ProdutoController.class).listar(null, null, null)).withRel("colecao")
        );
    }

    // ==========================================
    // 1. GET - LISTAR (UNIFICADO COM VERSIONAMENTO)
    // ==========================================

    @GetMapping
    @Operation(summary = "Listar Produtos (GET)", description = "Retorna produtos de forma paginada. Se o cabeçalho X-API-Version for igual a '2', retorna o contrato V2 (com preço formatado, desconto e categoria simplificada). Caso contrário (ou se ausente), retorna a V1 padrão.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<?> listar(
            Pageable pageable,
            @RequestHeader(value = "X-API-Version", required = false) String apiVersion,
            @Parameter(hidden = true) PagedResourcesAssembler<Produto> assembler) {

        Page<Produto> lista = repository.findAll(pageable);

        if ("2".equals(apiVersion)) {
            PagedModel<EntityModel<ProdutoV2Dto>> pagedModel = assembler.toModel(lista, this::toModelV2);
            return ResponseEntity.ok(pagedModel);
        } else {
            PagedModel<EntityModel<Produto>> pagedModel = assembler.toModel(lista, this::toModelV1);
            return ResponseEntity.ok(pagedModel);
        }
    }

    // ==========================================
    // OUTROS MÉTODOS HTTP
    // ==========================================

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Recupera os detalhes de um produto com links HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto recuperado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Produto>> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(p -> ResponseEntity.ok(toModelV1(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar-por-preco")
    @Operation(summary = "Consulta Personalizada - Faixa de Preço (GET)", description = "Busca produtos com preço dentro do intervalo especificado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produtos filtrados por preço com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<PagedModel<EntityModel<Produto>>> buscarPorFaixaDePreco(
            @RequestParam Double min,
            @RequestParam Double max,
            Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Produto> assembler) {
        Page<Produto> lista = repository.findByPrecoBetween(min, max, pageable);
        PagedModel<EntityModel<Produto>> pagedModel = assembler.toModel(lista, this::toModelV1);
        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    @Operation(summary = "Criar Produto (POST)", description = "Cadastra um novo produto no estoque. Requer cabeçalho X-Idempotency-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou cabeçalho X-Idempotency-Key ausente."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "409", description = "Conflito. Chave de idempotência já utilizada."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Produto>> criar(
            @RequestBody @Valid Produto produto,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        Produto salvo = repository.save(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModelV1(salvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Substitui todos os dados de um produto existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto substituído com sucesso."),
        @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Produto>> substituir(@PathVariable Long id, @RequestBody @Valid Produto novoProduto) {
        return repository.findById(id).map(produto -> {
            produto.setNome(novoProduto.getNome());
            produto.setPreco(novoProduto.getPreco());
            produto.setCondicao(novoProduto.getCondicao());
            produto.setCategoria(novoProduto.getCategoria());
            Produto salvo = repository.save(produto);
            return ResponseEntity.ok(toModelV1(salvo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Parcial (PATCH)", description = "Altera campos específicos de um produto (ex: nome, preço).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<Produto>> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Produto> produtoOpt = repository.findById(id);
        if (produtoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Produto produto = produtoOpt.get();

        if (campos.containsKey("nome")) {
            produto.setNome((String) campos.get("nome"));
        }
        if (campos.containsKey("preco")) {
            produto.setPreco(Double.valueOf(campos.get("preco").toString()));
        }

        Produto salvo = repository.save(produto);
        return ResponseEntity.ok(toModelV1(salvo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui permanentemente um produto do estoque.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
        @ApiResponse(responseCode = "403", description = "Acesso proibido. Operação requer privilégios de ADMIN."),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado."),
        @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}