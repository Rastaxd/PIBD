package org.dorixon.springlab4.service;

import org.dorixon.springlab4.model.Projekt;
import org.dorixon.springlab4.model.Student;
import org.dorixon.springlab4.model.Zadanie;
import org.dorixon.springlab4.repository.ProjektRepository;
import org.dorixon.springlab4.repository.StudentRepository;
import org.dorixon.springlab4.repository.ZadanieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjektServiceUnitTest {

    @Mock
    private ProjektRepository projektRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ZadanieRepository zadanieRepository;

    @InjectMocks
    private ProjektServiceImpl projektService;

    private Projekt projekt;
    private Student student;
    private Zadanie zadanie;

    @BeforeEach
    void setUp() {
        projekt = new Projekt();
        projekt.setProjektId(1);
        projekt.setNazwa("Testowy Projekt");

        student = new Student();
        student.setStudentId(1);
        student.setNrIndeksu("111222");

        zadanie = new Zadanie();
        zadanie.setZadanieId(1);
        zadanie.setNazwa("Zadanie");
    }

    @Test
    void getProjekt_whenValidId_shouldReturnGivenProject() {
        when(projektRepository.findById(1)).thenReturn(Optional.of(projekt));

        Optional<Projekt> found = projektService.getProjekt(1);

        assertTrue(found.isPresent());
        assertEquals("Testowy Projekt", found.get().getNazwa());
        verify(projektRepository, times(1)).findById(1);
    }

    @Test
    void setProjekt_shouldSaveAndReturnProject() {
        when(projektRepository.save(any(Projekt.class))).thenReturn(projekt);

        Projekt saved = projektService.setProjekt(projekt);

        assertNotNull(saved);
        assertEquals(1, saved.getProjektId());
        verify(projektRepository, times(1)).save(projekt);
    }

    @Test
    void deleteProjekt_shouldDeleteZadaniaAndProject() {
        when(zadanieRepository.findZadaniaProjektu(1)).thenReturn(Collections.singletonList(zadanie));
        doNothing().when(zadanieRepository).deleteAll(anyList());
        doNothing().when(projektRepository).deleteById(1);

        projektService.deleteProjekt(1);

        verify(zadanieRepository, times(1)).deleteAll(anyList());
        verify(projektRepository, times(1)).deleteById(1);
    }

    @Test
    void searchByNazwa_shouldReturnPage() {
        Page<Projekt> page = new PageImpl<>(List.of(projekt));
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(projektRepository.findByNazwaContainingIgnoreCase(eq("Test"), eq(pageRequest))).thenReturn(page);

        Page<Projekt> result = projektService.searchByNazwa("Test", pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(projektRepository, times(1)).findByNazwaContainingIgnoreCase("Test", pageRequest);
    }

    @Test
    void addZadanieToProjekt_whenValid_shouldAddAndSave() {
        when(projektRepository.findById(1)).thenReturn(Optional.of(projekt));
        when(zadanieRepository.findById(1)).thenReturn(Optional.of(zadanie));
        
        when(zadanieRepository.save(any())).thenReturn(zadanie);
        when(projektRepository.save(any())).thenReturn(projekt);

        projektService.addZadanieToProjekt(1, 1);

        assertTrue(projekt.getZadania().contains(zadanie));
        assertEquals(projekt, zadanie.getProjekt());
        verify(zadanieRepository, times(1)).save(zadanie);
        verify(projektRepository, times(1)).save(projekt);
    }

    @Test
    void addStudentToProjekt_whenValid_shouldAddAndSave() {
        when(projektRepository.findById(1)).thenReturn(Optional.of(projekt));
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));

        when(studentRepository.save(any())).thenReturn(student);
        when(projektRepository.save(any())).thenReturn(projekt);

        projektService.addStudentToProjekt(1, 1);

        assertTrue(projekt.getStudenci().contains(student));
        assertTrue(student.getProjekty().contains(projekt));
        verify(studentRepository, times(1)).save(student);
        verify(projektRepository, times(1)).save(projekt);
    }

    @Test
    void getZadaniaCount_shouldReturnCorrectCount() {
        when(zadanieRepository.countByProjektProjektId(1)).thenReturn(5L);

        Long count = projektService.getZadaniaCount(1);

        assertEquals(5L, count);
        verify(zadanieRepository, times(1)).countByProjektProjektId(1);
    }

    @Test
    void getStudentCount_shouldReturnCorrectCount() {
        when(studentRepository.countByProjektyProjektId(1)).thenReturn(3L);

        Long count = projektService.getStudentCount(1);

        assertEquals(3L, count);
        verify(studentRepository, times(1)).countByProjektyProjektId(1);
    }
}
