package com.sportsapi.sports_ecommerce.model; // CORRIGIDO: Adicionado o .sports_

import com.sportsapi.sports_ecommerce.enums.CondicaoProduto; // CORRIGIDO: Adicionado o .sports_
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_produto") // Boa prática: definir um nome para a tabela
public class Produto extends RepresentationModel<Produto> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode estar em branco")
    private String nome;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    private Double preco;

    @Enumerated(EnumType.STRING)
    private CondicaoProduto condicao;

    // Muitos produtos pertencem a uma categoria
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Construtor padrão obrigatório
    public Produto() {}

    // GETTERS E SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public CondicaoProduto getCondicao() { return condicao; }
    public void setCondicao(CondicaoProduto condicao) { this.condicao = condicao; }

    // Faltava o getter e setter da categoria para o relacionamento funcionar!
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}