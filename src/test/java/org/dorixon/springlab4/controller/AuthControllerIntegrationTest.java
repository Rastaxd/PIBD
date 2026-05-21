package org.dorixon.springlab4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dorixon.springlab4.auth.Credentials;
import org.dorixon.springlab4.auth.Tokens;
import org.dorixon.springlab4.config.SecurityWebConfig;
import org.dorixon.springlab4.model.Student;
import org.dorixon.springlab4.security.JwtAuthFilter;
import org.dorixon.springlab4.security.JwtService;
import org.dorixon.springlab4.service.AuthService;
import org.dorixon.springlab4.validation.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test integracyjny warstwy web kontrolera AuthController.
 * Używa @WebMvcTest — ładuje tylko kontekst MVC (bez bazy danych).
 * Importuje realną konfigurację bezpieczeństwa (@Import), co pozwala
 * weryfikować, które endpointy są dostępne bez uwierzytelnienia.
 * Serwisy biznesowe są mockowane przez @MockBean.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityWebConfig.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private ValidationService<Credentials> credentialsValidationService;

    @MockBean
    private ValidationService<Student> studentValidationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUpFilter() throws Exception {
        // JwtAuthFilter mock musi propagować żądanie dalej w łańcuchu filtrów —
        // bez tego żądanie nie dotrze do kontrolera i odpowiedź będzie pusta.
        doAnswer(invocation -> {
            HttpServletRequest request   = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain            = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    // -----------------------------------------------------------------------
    // POST /api/register
    // -----------------------------------------------------------------------

    @Test
    void register_whenValidStudent_shouldReturnTokens() throws Exception {
        Student student = new Student();
        student.setEmail("jan@example.com");
        student.setPassword("haslo1234");
        student.setImie("Jan");
        student.setNazwisko("Kowalski");
        student.setNrIndeksu("123456");
        student.setStacjonarny(true);

        Tokens tokens = new Tokens("access-token-abc", "refresh-token-xyz");
        when(authService.register(any(Credentials.class), any(Student.class))).thenReturn(tokens);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token-abc"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-xyz"));
    }

    // -----------------------------------------------------------------------
    // POST /api/login
    // -----------------------------------------------------------------------

    @Test
    void login_whenValidCredentials_shouldReturnTokens() throws Exception {
        Credentials creds = new Credentials("jan@example.com", "haslo1234");
        Tokens tokens = new Tokens("access-token-abc", "refresh-token-xyz");

        when(authService.login(any(Credentials.class))).thenReturn(tokens);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token-abc"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-xyz"));
    }

    @Test
    void login_whenServiceThrowsException_shouldPropagateError() {
        Credentials creds = new Credentials("zly@example.com", "zlehaslo123");

        when(authService.login(any(Credentials.class)))
                .thenThrow(new RuntimeException("User not found"));

        // Aplikacja nie ma @ExceptionHandler dla RuntimeException, więc MockMvc
        // propaguje błąd jako wyjątek zamiast zwracać kod HTTP 500.
        // Weryfikujemy że żądanie nie zakończy się sukcesem.
        Exception ex = org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
        );
        org.junit.jupiter.api.Assertions.assertNotNull(ex);
    }

    // -----------------------------------------------------------------------
    // POST /api/refresh
    // -----------------------------------------------------------------------

    @Test
    void refresh_whenValidBearerToken_shouldReturnNewTokens() throws Exception {
        Tokens tokens = new Tokens("new-access-token", "new-refresh-token");
        when(authService.refreshToken(eq("valid-refresh-token"))).thenReturn(tokens);

        mockMvc.perform(post("/api/refresh")
                        .header("Authorization", "Bearer valid-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refresh_whenMissingAuthorizationHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/refresh"))
                .andExpect(status().isBadRequest());
    }
}
