package org.dorixon.springlab4.service;

import org.dorixon.springlab4.model.Projekt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProjektService
{
    Optional<Projekt> getProjekt(Integer projektId);
    Projekt setProjekt(Projekt projekt);
    void deleteProjekt(Integer projektId);
    Page<Projekt> getProjekty(Pageable pageable);
    Page<Projekt> searchByNazwa(String nazwa, Pageable pageable);

    void addZadanieToProjekt(Integer projektId, Integer zadanieId);

    void removeZadanieFromProjekt(Integer projektId, Integer zadanieId);

    void removeStudentFromProjekt(Integer projektId, Integer studentId);

    void addStudentToProjekt(Integer projektId, Integer studentId);

    Long getZadaniaCount(Integer projektId);

    Long getStudentCount(Integer projektId);
}
