package com.sportsapi.sports_ecommerce.model;

import jakarta.persistence.*;
import org.springframework.hateoas.RepresentationModel;

@Entity
public class Endereco extends RepresentationModel<Endereco> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logradouro;
    private String cep;

    public Endereco() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
}