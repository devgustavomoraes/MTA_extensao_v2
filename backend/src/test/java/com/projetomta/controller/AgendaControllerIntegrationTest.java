package com.projetomta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetomta.domain.entity.Evento;
import com.projetomta.domain.entity.Membro;
import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.Perfil;
import com.projetomta.domain.enums.PrioridadeAviso;
import com.projetomta.dto.AvisoRequest;
import com.projetomta.dto.EscalaRequest;
import com.projetomta.dto.EventoRequest;
import com.projetomta.repository.AvisoRepository;
import com.projetomta.repository.EscalaRepository;
import com.projetomta.repository.EventoRepository;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MembroRepository membroRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private AvisoRepository avisoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private String tokenAdmin;
    private String tokenUsuario;
    private Long membroId;

    @BeforeEach
    void setUp() {
        databaseCleaner.limparTudo();

        Usuario admin = usuarioRepository.save(
                TestAuthHelper.criarUsuario("admin@test.com", "senha123", Perfil.ADMIN, passwordEncoder));
        Usuario usuario = usuarioRepository.save(
                TestAuthHelper.criarUsuario("usuario@test.com", "senha123", Perfil.USUARIO, passwordEncoder));

        tokenAdmin = TestAuthHelper.gerarToken(admin, jwtService);
        tokenUsuario = TestAuthHelper.gerarToken(usuario, jwtService);

        membroId = membroRepository.save(Membro.builder()
                .nomeCompleto("Maria Escalada")
                .ativo(true)
                .build()).getId();
    }

    @Test
    void adminDeveCriarEvento() throws Exception {
        EventoRequest request = criarEventoRequest(
                "Culto de Domingo",
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(12).withMinute(0)
        );

        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Culto de Domingo"));
    }

    @Test
    void deveRejeitarEventoComHorarioInvalido() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        EventoRequest request = criarEventoRequest("Evento Inválido", inicio, inicio.minusHours(1));

        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("REGRA_NEGOCIO"));
    }

    @Test
    void deveDetectarConflitoDeHorarioEntreEventos() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withHour(18).withMinute(0);
        LocalDateTime fim = inicio.plusHours(2);

        EventoRequest primeiro = criarEventoRequest("Reunião A", inicio, fim);
        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primeiro)))
                .andExpect(status().isCreated());

        EventoRequest conflitante = criarEventoRequest("Reunião B", inicio.plusMinutes(30), fim.plusMinutes(30));
        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflitante)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("CONFLITO_HORARIO"));
    }

    @Test
    void deveDetectarConflitoDeEscalaDoMembro() throws Exception {
        LocalDateTime inicio1 = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0);
        Long evento1Id = criarEventoViaApi("Evento 1", inicio1, inicio1.plusHours(2));

        Usuario admin = usuarioRepository.findByEmail("admin@test.com").orElseThrow();
        LocalDateTime inicio2 = inicio1.plusMinutes(30);
        Evento evento2 = eventoRepository.save(Evento.builder()
                .titulo("Evento 2")
                .dataInicio(inicio2)
                .dataFim(inicio2.plusHours(2))
                .local("Templo Sede")
                .criadoPor(admin)
                .build());
        Long evento2Id = evento2.getId();

        EscalaRequest escala = new EscalaRequest();
        escala.setMembroId(membroId);
        escala.setFuncaoEscala("Músico");

        mockMvc.perform(post("/api/eventos/" + evento1Id + "/escalas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(escala)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/eventos/" + evento2Id + "/escalas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(escala)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("CONFLITO_HORARIO"));
    }

    @Test
    void usuarioPodeListarAvisosMasNaoCriarEvento() throws Exception {
        AvisoRequest avisoRequest = new AvisoRequest();
        avisoRequest.setTitulo("Aviso Geral");
        avisoRequest.setMensagem("Reunião na sexta-feira");
        avisoRequest.setPrioridade(PrioridadeAviso.URGENTE);
        avisoRequest.setDataExpiracao(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/api/avisos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(avisoRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/avisos")
                        .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Aviso Geral"));

        EventoRequest eventoRequest = criarEventoRequest(
                "Bloqueado",
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(10).plusHours(1)
        );

        mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventoRequest)))
                .andExpect(status().isForbidden());
    }

    private EventoRequest criarEventoRequest(String titulo, LocalDateTime inicio, LocalDateTime fim) {
        EventoRequest request = new EventoRequest();
        request.setTitulo(titulo);
        request.setDataInicio(inicio);
        request.setDataFim(fim);
        request.setLocal("Templo Sede");
        return request;
    }

    private Long criarEventoViaApi(String titulo, LocalDateTime inicio, LocalDateTime fim) throws Exception {
        EventoRequest request = criarEventoRequest(titulo, inicio, fim);
        String response = mockMvc.perform(post("/api/eventos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
