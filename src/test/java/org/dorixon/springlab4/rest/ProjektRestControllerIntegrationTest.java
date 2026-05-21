package org.dorixon.springlab4.rest;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.dorixon.springlab4.model.Projekt;
import org.dorixon.springlab4.service.ProjektService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", password = "admin")
public class ProjektRestControllerIntegrationTest {
private final String apiPath = "/api/projekty";
    @MockBean
    private ProjektService mockProjektService; 
    @Autowired
    private MockMvc mockMvc;
    private JacksonTester<Projekt> jacksonTester;
    @Test
    public void getProject_whenValidId_shouldReturnGivenProject() throws Exception {
        Projekt projekt = new Projekt(2, "Nazwa2", "Opis2", LocalDateTime.now(), LocalDate.of(2024, 6, 7));
        when(mockProjektService.getProjekt(projekt.getProjektId()))
                .thenReturn(Optional.of(projekt));
        mockMvc.perform(get(apiPath + "/{projektId}", projekt.getProjektId()).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projektId").value(projekt.getProjektId()))
                .andExpect(jsonPath("$.nazwa").value(projekt.getNazwa()));
        verify(mockProjektService, times(1)).getProjekt(projekt.getProjektId());
        verifyNoMoreInteractions(mockProjektService);
    }
    @Test
    public void getProject_whenInvalidId_shouldReturnNotFound() throws Exception {
        Integer projektId = 2;
        when(mockProjektService.getProjekt(projektId)).thenReturn(Optional.empty());
        mockMvc.perform(get(apiPath + "/{projektId}", projektId).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(mockProjektService, times(1)).getProjekt(projektId);
        verifyNoMoreInteractions(mockProjektService);
    }
    @Test
    public void getProjects_whenTwoAvailable_shouldReturnContentWithPagingParams() throws Exception {
        Projekt projekt1 = new Projekt(1, "Nazwa1", "Opis1", LocalDateTime.now(), LocalDate.of(2024, 6, 1));
        Projekt projekt2 = new Projekt(2, "Nazwa2", "Opis2", LocalDateTime.now(), LocalDate.of(2024, 6, 2));
     
        Page<Projekt> page = new PageImpl<>(List.of(projekt1, projekt2));
        when(mockProjektService.getProjekty(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get(apiPath).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]").exists()) //content[*] - oznacza całą zawartość tablicy content
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].projektId").value(projekt1.getProjektId()))
                .andExpect(jsonPath("$.content[1].projektId").value(projekt2.getProjektId()));
        verify(mockProjektService, times(1)).getProjekty(any(Pageable.class));
        verifyNoMoreInteractions(mockProjektService);
    }
    @Test
    public void createProject_whenValidData_shouldReturnCreatedStatusWithLocation() throws Exception {
        Projekt projekt = new Projekt("Nazwa3", "Opis3", LocalDate.of(2024, 6, 7));
        String jsonProjekt = jacksonTester.write(projekt).getJson();
        projekt.setProjektId(3);
        when(mockProjektService.setProjekt(any(Projekt.class))).thenReturn(projekt);
        mockMvc.perform(post(apiPath).content(jsonProjekt).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString(apiPath + "/" + projekt.getProjektId())));
    }
    @Test
    public void createProject_whenEmptyName_shouldReturnNotValidException() throws Exception {
        Projekt projekt = new Projekt( "", "Opis4", LocalDate.of(2024, 6, 7));
        MvcResult result = mockMvc.perform(post(apiPath)
                        .content(jacksonTester.write(projekt).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        verify(mockProjektService, times(0)).setProjekt(any(Projekt.class));
        Exception exception = result.getResolvedException();
        assertNotNull(exception);
        assertTrue(exception instanceof jakarta.validation.ConstraintViolationException);
        System.out.println(exception.getMessage());
    }
    @Test
    public void updateProject_whenValidData_shouldReturnOkStatus() throws Exception {
        Projekt projekt = new Projekt(5, "Nazwa5", "Opis5", LocalDateTime.now(), LocalDate.of(2024, 6, 7));
        String jsonProjekt = jacksonTester.write(projekt).getJson();
        when(mockProjektService.getProjekt(projekt.getProjektId())).thenReturn(Optional.of(projekt));
        when(mockProjektService.setProjekt(any(Projekt.class))).thenReturn(projekt);
        mockMvc.perform(put(apiPath + "/{projektId}", projekt.getProjektId())
                        .content(jsonProjekt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isOk());
        verify(mockProjektService, times(1)).getProjekt(projekt.getProjektId());
        verify(mockProjektService, times(1)).setProjekt(any(Projekt.class));
        verifyNoMoreInteractions(mockProjektService);
    }
    /**
     * Test sprawdza czy żądanie o danych parametrach stronicowania i sortowania
     * spowoduje przekazanie do serwisu odpowiedniego obiektu Pageable, wcześniej
     * wstrzykniętego do parametru wejściowego metody kontrolera
     */
    @Test
    public void getProjectsAndVerifyPagingParams() throws Exception {
        Integer page = 5;
        Integer size = 15;
        String sortProperty = "nazwa";
        String sortDirection = "desc";
        mockMvc.perform(get(apiPath)
                        .param("page", page.toString())
                        .param("size", size.toString())
                        .param("sort", String.format("%s,%s", sortProperty, sortDirection)))
                .andExpect(status().isOk());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(mockProjektService, times(1)).getProjekty(pageableCaptor.capture());
        PageRequest pageable = (PageRequest) pageableCaptor.getValue();
        assertEquals(page, pageable.getPageNumber());
        assertEquals(size, pageable.getPageSize());
        assertEquals(sortProperty, pageable.getSort().getOrderFor(sortProperty).getProperty());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor(sortProperty).getDirection());
    }
    @BeforeEach
    public void before(TestInfo testInfo) {
        System.out.printf("-- METODA -> %s%n", testInfo.getTestMethod().get().getName());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonTester.initFields(this, mapper);
    }
    @AfterEach
    public void after(TestInfo testInfo) {
        System.out.printf("<- KONIEC -- %s%n", testInfo.getTestMethod().get().getName());
    }
}
