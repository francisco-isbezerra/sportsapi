package com.sportsapi.sports_ecommerce.controller;

import com.sportsapi.sports_ecommerce.model.Produto;
import com.sportsapi.sports_ecommerce.repository.ProdutoRepository;
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
@RequestMapping("/api/v1/produtos")
@Tag(name = "Produtos", description = "Gerenciamento de estoque com os 5 métodos HTTP")
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    // 1. GET - Recupera dados (Seguro e Idempotente)
    @GetMapping
    @Operation(summary = "Listar (GET)", description = "Retorna produtos de forma paginada. É seguro e idempotente.")
    public ResponseEntity<Page<Produto>> listar(Pageable pageable) {
        Page<Produto> lista = repository.findAll(pageable);
        for(Produto p : lista) {
            // HATEOAS: Adiciona link para o próprio recurso
            p.add(linkTo(methodOn(ProdutoController.class).buscarPorId(p.getId())).withSelfRel());
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID (GET)", description = "Recupera um produto específico sem alterar o estado do servidor.")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(p -> ResponseEntity.ok(p.add(linkTo(methodOn(ProdutoController.class).listar(null)).withRel("colecao"))))
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Cria novo recurso (Não seguro / Não idempotente)
    @PostMapping
    @Operation(summary = "Criar (POST)", description = "Cadastra um novo produto. Não é idempotente (múltiplos envios criam múltiplos recursos).")
    public ResponseEntity<Produto> criar(@RequestBody @Valid Produto produto,
                                         @RequestHeader("Idempotency-Key") String key) {
        // O uso do Header Idempotency-Key ajuda a mitigar a falta de idempotência do POST
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(produto));
    }

    // 3. PUT - Atualiza/Substitui totalmente (Idempotente)
    @PutMapping("/{id}")
    @Operation(summary = "Substituir (PUT)", description = "Substitui todos os dados do produto. É idempotente.")
    public ResponseEntity<Produto> substituir(@PathVariable Long id, @RequestBody @Valid Produto novoProduto) {
        return repository.findById(id).map(produto -> {
            produto.setNome(novoProduto.getNome());
            produto.setPreco(novoProduto.getPreco());
            produto.setCondicao(novoProduto.getCondicao());
            produto.setCategoria(novoProduto.getCategoria());
            return ResponseEntity.ok(repository.save(produto));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. PATCH - Atualização Parcial (Modificações específicas)
    @PatchMapping("/{id}")
    @Operation(summary = "Parcial (PATCH)", description = "Altera apenas campos específicos (ex: só o preço).")
    public ResponseEntity<Produto> atualizarParcial(@PathVariable Long id, @RequestBody Map<String, Object> campos) {
        Optional<Produto> produtoOpt = repository.findById(id);
        if (produtoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Produto produto = produtoOpt.get();

        // Lógica para aplicar apenas o que veio no JSON
        if (campos.containsKey("nome")) {
            produto.setNome((String) campos.get("nome"));
        }
        if (campos.containsKey("preco")) {
            produto.setPreco(Double.valueOf(campos.get("preco").toString()));
        }

        return ResponseEntity.ok(repository.save(produto));
    }

    // 5. DELETE - Remove o recurso (Idempotente)
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (DELETE)", description = "Exclui permanentemente o produto. É idempotente.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}