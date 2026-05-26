package com.sportsapi.sports_ecommerce.config;

import com.sportsapi.sports_ecommerce.enums.CondicaoProduto;
import com.sportsapi.sports_ecommerce.enums.NivelAcesso;
import com.sportsapi.sports_ecommerce.model.*;
import com.sportsapi.sports_ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeedConfig implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n>>> [SEED DATA] Iniciando carga de dados do Sports E-Commerce...");

        // 1. Criar Categorias Esportivas
        Categoria futebol = new Categoria();
        futebol.setNome("Futebol");
        Categoria corrida = new Categoria();
        corrida.setNome("Corrida");
        Categoria tenis = new Categoria();
        tenis.setNome("Tênis");
        Categoria academia = new Categoria();
        academia.setNome("Academia");

        categoriaRepository.saveAll(Arrays.asList(futebol, corrida, tenis, academia));

        // 2. Criar Produtos Esportivos
        Produto chuteira = new Produto();
        chuteira.setNome("Chuteira Nike Mercurial");
        chuteira.setPreco(349.90);
        chuteira.setCondicao(CondicaoProduto.NOVO);
        chuteira.setCategoria(futebol);

        Produto bola = new Produto();
        bola.setNome("Bola de Futebol Adidas Al Rihla");
        bola.setPreco(189.90);
        bola.setCondicao(CondicaoProduto.NOVO);
        bola.setCategoria(futebol);

        Produto tenisNike = new Produto();
        tenisNike.setNome("Tênis de Corrida Nike Pegasus 40");
        tenisNike.setPreco(599.90);
        tenisNike.setCondicao(CondicaoProduto.NOVO);
        tenisNike.setCategoria(corrida);

        Produto raquete = new Produto();
        raquete.setNome("Raquete de Tênis Wilson Pro Staff");
        raquete.setPreco(1250.00);
        raquete.setCondicao(CondicaoProduto.USADO);
        raquete.setCategoria(tenis);

        Produto camisa = new Produto();
        camisa.setNome("Camisa Flamengo Oficial 2026");
        camisa.setPreco(299.90);
        camisa.setCondicao(CondicaoProduto.NOVO);
        camisa.setCategoria(futebol);

        Produto halter = new Produto();
        halter.setNome("Par de Halteres Hexagonais 5kg");
        halter.setPreco(150.00);
        halter.setCondicao(CondicaoProduto.USADO);
        halter.setCategoria(academia);

        produtoRepository.saveAll(Arrays.asList(chuteira, bola, tenisNike, raquete, camisa, halter));

        // 3. Criar Clientes e Endereços (Relacionamento One-to-One)
        Endereco end1 = new Endereco();
        end1.setLogradouro("Avenida Paulista, 1000 - Bela Vista");
        end1.setCep("01310-100");

        Cliente cli1 = new Cliente();
        cli1.setNome("Francisco Silva");
        cli1.setEndereco(end1);

        Endereco end2 = new Endereco();
        end2.setLogradouro("Rua das Flores, 450 - Centro");
        end2.setCep("80010-000");

        Cliente cli2 = new Cliente();
        cli2.setNome("Maria Oliveira");
        cli2.setEndereco(end2);

        clienteRepository.saveAll(Arrays.asList(cli1, cli2));

        // 4. Criar Pedidos (Relacionamentos One-to-Many e Many-to-Many)
        Pedido ped1 = new Pedido();
        ped1.setCliente(cli1);
        ped1.setDataPedido(LocalDateTime.now().minusDays(2));
        ped1.setProdutos(Arrays.asList(chuteira, bola));

        Pedido ped2 = new Pedido();
        ped2.setCliente(cli2);
        ped2.setDataPedido(LocalDateTime.now());
        ped2.setProdutos(Arrays.asList(tenisNike, camisa));

        pedidoRepository.saveAll(Arrays.asList(ped1, ped2));

        // 5. Criar Usuários e Chaves de API para testes do avaliador
        Usuario professor = new Usuario("Professor Avaliador", "professor@faculdade.edu");
        usuarioRepository.save(professor);

        // Chave de nível ADMIN
        ApiKey adminKey = new ApiKey("sports-admin-test-key", professor, NivelAcesso.ADMIN);
        apiKeyRepository.save(adminKey);

        // Chave de nível CLIENTE
        ApiKey clientKey = new ApiKey("sports-client-test-key", professor, NivelAcesso.CLIENTE);
        apiKeyRepository.save(clientKey);

        System.out.println(">>> [SEED DATA] Chaves de teste criadas para o professor:");
        System.out.println("   - ADMIN:   X-API-Key = sports-admin-test-key");
        System.out.println("   - CLIENTE: X-API-Key = sports-client-test-key");
        System.out.println(">>> [SEED DATA] Carga de dados concluída com sucesso!\n");
    }
}
