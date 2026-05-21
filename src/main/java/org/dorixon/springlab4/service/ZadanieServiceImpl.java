package org.dorixon.springlab4.service;

import lombok.AllArgsConstructor;
import org.dorixon.springlab4.model.Zadanie;
import org.dorixon.springlab4.repository.ZadanieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ZadanieServiceImpl implements ZadanieService {

    private ZadanieRepository zadanieRepository;

    @Override
    public Optional<Zadanie> getZadanie(Integer zadanieId) {
        return zadanieRepository.findById(zadanieId);
    }

    @Override
    public Zadanie setZadanie(Zadanie zadanie) {
        return zadanieRepository.save(zadanie);
    }

    @Override
    public void deleteZadanie(Integer zadanieId) {
        zadanieRepository.deleteById(zadanieId);
    }

    @Override
    public Page<Zadanie> getZadania(Pageable pageable) {
        return zadanieRepository.findAll(pageable);
    }

    @Override
    public Page<Zadanie> getZadaniaByProjektId(Integer projektId, Pageable pageable) {
        return zadanieRepository.findByProjektProjektId(projektId, pageable);
    }

    @Override
    public Page<Zadanie> findByNazwaContaining(String nazwa, Pageable pageable) {
        return zadanieRepository.findByNazwaContainingIgnoreCase(nazwa, pageable);
    }
}