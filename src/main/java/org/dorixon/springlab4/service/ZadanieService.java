package org.dorixon.springlab4.service;

import org.dorixon.springlab4.model.Zadanie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ZadanieService {
    Optional<Zadanie> getZadanie(Integer zadanieId);
    Zadanie setZadanie(Zadanie zadanie);
    void deleteZadanie(Integer zadanieId);
    Page<Zadanie> getZadania(Pageable pageable);
    Page<Zadanie> getZadaniaByProjektId(Integer projektId, Pageable pageable);

    Page<Zadanie> findByNazwaContaining(String nazwa, Pageable pageable);
}