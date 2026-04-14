package com.sportsapi.sports_ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

@Entity
public class Categoria extends RepresentationModel<Categoria> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da categoria é obrigatório")
    private String nome;

    // mappedBy aponta para o nome do campo 'categoria' que criamos na classe Produto
    @OneToMany(mappedBy = "categoria")
    private List<Produto> produtos;

    public Categoria() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}