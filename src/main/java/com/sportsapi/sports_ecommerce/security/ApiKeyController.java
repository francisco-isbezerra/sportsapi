package com.sportsapi.sports_ecommerce.security;

import com.sportsapi.sports_ecommerce.enums.NivelAcesso;
import com.sportsapi.sports_ecommerce.model.ApiKey;
import com.sportsapi.sports_ecommerce.model.Usuario;
import com.sportsapi.sports_ecommerce.repository.ApiKeyRepository;
import com.sportsapi.sports_ecommerce.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/auth/keys")
@Tag(name = "Autenticação (Chaves de API)", description = "Geração e revogação de chaves X-API-Key")
public class ApiKeyController {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public static class KeyRequest {
        @NotBlank(message = "O nome é obrigatório")
        private String nome;

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        private NivelAcesso nivelAcesso = NivelAcesso.CLIENTE;

        // Getters e Setters
        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public NivelAcesso getNivelAcesso() {
            return nivelAcesso;
        }

        public void setNivelAcesso(NivelAcesso nivelAcesso) {
            this.nivelAcesso = nivelAcesso;
        }
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar Chave de API (Público)", description = "Gera uma nova chave X-API-Key associada a um usuário. Se o usuário não existir, cria um novo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chave de API gerada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou e-mail malformatado."),
            @ApiResponse(responseCode = "409", description = "Conflito de dados no cadastro do usuário."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<ApiKey>> gerarChave(@RequestBody @Valid KeyRequest request) {
        // Buscar ou criar usuário
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseGet(() -> usuarioRepository.save(new Usuario(request.getNome(), request.getEmail())));

        // Gerar chave UUID aleatória
        String chave = "sports-" + UUID.randomUUID().toString();
        ApiKey apiKey = new ApiKey(chave, usuario, request.getNivelAcesso());
        ApiKey salva = apiKeyRepository.save(apiKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salva));
    }

    @GetMapping
    @Operation(summary = "Listar Chaves (Requer ADMIN)", description = "Retorna lista paginada de chaves cadastradas no sistema. Exige cabeçalho X-API-Key com nível ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de chaves de API recuperada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
            @ApiResponse(responseCode = "403", description = "Acesso proibido. Requer privilégios de ADMIN."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<PagedModel<EntityModel<ApiKey>>> listarChaves(Pageable pageable,
            PagedResourcesAssembler<ApiKey> assembler) {
        Page<ApiKey> chaves = apiKeyRepository.findAll(pageable);
        PagedModel<EntityModel<ApiKey>> pagedModel = assembler.toModel(chaves, this::toModel);
        return ResponseEntity.ok(pagedModel);
    }

    @PatchMapping("/{id}/revogar")
    @Operation(summary = "Revogar Chave (Requer ADMIN)", description = "Inativa uma chave de API para bloquear futuros acessos. Exige cabeçalho X-API-Key com nível ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave de API revogada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API ausente ou inválida."),
            @ApiResponse(responseCode = "403", description = "Acesso proibido. Requer privilégios de ADMIN."),
            @ApiResponse(responseCode = "404", description = "Chave de API não encontrada."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (Rate Limiting)."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
    })
    public ResponseEntity<EntityModel<ApiKey>> revogarChave(@PathVariable Long id) {
        return apiKeyRepository.findById(id).map(key -> {
            key.setAtivo(false);
            ApiKey salva = apiKeyRepository.save(key);
            return ResponseEntity.ok(toModel(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    private EntityModel<ApiKey> toModel(ApiKey apiKey) {
        return EntityModel.of(apiKey,
                linkTo(methodOn(ApiKeyController.class).listarChaves(null, null)).withRel("chaves"),
                linkTo(methodOn(ApiKeyController.class).revogarChave(apiKey.getId())).withRel("revogar"));
    }
}
