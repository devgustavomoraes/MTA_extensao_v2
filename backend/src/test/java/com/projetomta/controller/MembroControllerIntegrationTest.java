package com.projetomta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetomta.domain.entity.Membro;
import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.Perfil;
import com.projetomta.dto.MembroRequest;
import com.projetomta.repository.MembroRepository;
import com.projetomta.repository.UsuarioRepository;
import com.projetomta.security.JwtService;
import com.projetomta.support.TestAuthHelper;
import com.projetomta.support.TestDatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MembroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MembroRepository membroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private String tokenAdmin;
    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        databaseCleaner.limparTudo();

        Usuario admin = usuarioRepository.save(
                TestAuthHelper.criarUsuario("admin@test.com", "senha123", Perfil.ADMIN, passwordEncoder));
        Usuario usuario = usuarioRepository.save(
                TestAuthHelper.criarUsuario("usuario@test.com", "senha123", Perfil.USUARIO, passwordEncoder));

        tokenAdmin = TestAuthHelper.gerarToken(admin, jwtService);
        tokenUsuario = TestAuthHelper.gerarToken(usuario, jwtService);
    }

    @Test
    void deveRejeitarEmailDuplicado() throws Exception {
        membroRepository.save(Membro.builder()
                .nomeCompleto("Existente")
                .email("duplicado@test.com")
                .ativo(true)
                .build());

        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("Novo Membro");
        request.setEmail("duplicado@test.com");

        mockMvc.perform(post("/api/membros")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("REGRA_NEGOCIO"));
    }

    @Test
    void adminDeveCriarMembro() throws Exception {
        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("João Silva");
        request.setEmail("joao@test.com");
        request.setFuncao("Músico");

        mockMvc.perform(post("/api/membros")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCompleto").value("João Silva"))
                .andExpect(jsonPath("$.funcao").value("Músico"));
    }

    @Test
    void usuarioNaoDeveCriarMembro() throws Exception {
        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("Maria Souza");

        mockMvc.perform(post("/api/membros")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioPodeListarMembros() throws Exception {
        membroRepository.save(Membro.builder()
                .nomeCompleto("Ana Costa")
                .ativo(true)
                .build());

        mockMvc.perform(get("/api/membros")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeCompleto").value("Ana Costa"));
    }

    @Test
    void adminDeveAtualizarMembro() throws Exception {
        Membro membro = membroRepository.save(Membro.builder()
                .nomeCompleto("Pedro Lima")
                .ativo(true)
                .build());

        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("Pedro Lima Santos");
        request.setFuncao("Intercessor");

        mockMvc.perform(put("/api/membros/" + membro.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Pedro Lima Santos"))
                .andExpect(jsonPath("$.funcao").value("Intercessor"));
    }

    @Test
    void usuarioNaoDeveExcluirMembro() throws Exception {
        Membro membro = membroRepository.save(Membro.builder()
                .nomeCompleto("Carlos Dias")
                .ativo(true)
                .build());

        mockMvc.perform(delete("/api/membros/" + membro.getId())
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeveExcluirMembroLogicamente() throws Exception {
        Membro membro = membroRepository.save(Membro.builder()
                .nomeCompleto("Lucas Alves")
                .ativo(true)
                .build());

        mockMvc.perform(delete("/api/membros/" + membro.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/membros/" + membro.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }
}
