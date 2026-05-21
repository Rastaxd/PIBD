package org.dorixon.springlab4.repository;

import org.dorixon.springlab4.model.Zadanie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ZadanieRepository extends JpaRepository<Zadanie, Integer> {
    @Query("SELECT z FROM Zadanie z WHERE z.projekt.projektId = :projektId")
    Page<Zadanie> findZadaniaProjektu(@Param("projektId") Integer projektId, Pageable pageable);

    @Query("SELECT z FROM Zadanie z WHERE z.projekt.projektId = :projektId")
    List<Zadanie> findZadaniaProjektu(@Param("projektId") Integer projektId);


    Page<Zadanie> findByProjektProjektId(Integer projektId, Pageable pageable);

    Long countByProjektProjektId(Integer projektId);

    Page<Zadanie> findByNazwaContainingIgnoreCase(String nazwa, Pageable pageable);
}
