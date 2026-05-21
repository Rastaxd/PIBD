package org.dorixon.springlab4.repository;

import org.dorixon.springlab4.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByNrIndeksu(String nrIndeksu);
    Optional<Student> findByEmail(String email);
    Page<Student> findByNrIndeksuStartsWith(String nrIndeksu, Pageable pageable);
    Page<Student> findByNazwiskoStartsWithIgnoreCase(String nazwisko, Pageable pageable);


    Page<Student> findByProjektyProjektId(Integer projektId, Pageable pageable);

    Long countByProjektyProjektId(Integer projektId);
}
