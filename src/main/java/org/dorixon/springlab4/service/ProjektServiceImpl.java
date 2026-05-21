package org.dorixon.springlab4.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.dorixon.springlab4.model.Projekt;
import org.dorixon.springlab4.model.Student;
import org.dorixon.springlab4.model.Zadanie;
import org.dorixon.springlab4.repository.ProjektRepository;
import org.dorixon.springlab4.repository.StudentRepository;
import org.dorixon.springlab4.repository.ZadanieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@AllArgsConstructor
@Transactional
public class ProjektServiceImpl implements ProjektService{
    private ProjektRepository projektRepository;
    private StudentRepository studentRepository;
    private ZadanieRepository zadanieRepository;

    @Override
    public Optional<Projekt> getProjekt(Integer projektId) {
        return projektRepository.findById(projektId);
    }

    @Override
    public Projekt setProjekt(Projekt projekt) {
        if (projekt.getProjektId() != null && projekt.getProjektId() == 0) {
            projekt.setProjektId(null);
        }


        if (projekt.getStudenci() != null) {
            Set<Student> managedStudents = new HashSet<>();
            for (Student student : projekt.getStudenci()) {
                // Clear ID if it's 0
                if (student.getStudentId() != null && student.getStudentId() == 0) {
                    student.setStudentId(null);
                }

                Optional<Student> existingStudent = studentRepository.findByNrIndeksu(student.getNrIndeksu());
                if (existingStudent.isPresent()) {
                    managedStudents.add(existingStudent.get());
                } else {
                    managedStudents.add(student);
                }
            }
            projekt.setStudenci(managedStudents);
        }

        if (projekt.getZadania() != null) {
            for (Zadanie zadanie : projekt.getZadania()) {
                if (zadanie.getZadanieId() != null && zadanie.getZadanieId() == 0) {
                    zadanie.setZadanieId(null);
                }
                zadanie.setProjekt(projekt);
            }
        }

        return projektRepository.save(projekt);
    }

    @Override
    public void deleteProjekt(Integer projektId) {
        zadanieRepository.deleteAll(zadanieRepository.findZadaniaProjektu(projektId));
        projektRepository.deleteById(projektId);
    }

    @Override
    public Page<Projekt> getProjekty(Pageable pageable) {
        return projektRepository.findAll(pageable);
    }

    @Override
    public Page<Projekt> searchByNazwa(String nazwa, Pageable pageable) {
        return projektRepository.findByNazwaContainingIgnoreCase(nazwa, pageable);
    }

    @Override
    public void addZadanieToProjekt(Integer projektId, Integer zadanieId) {
        Projekt projekt = projektRepository.findById(projektId)
                .orElseThrow(() -> new IllegalArgumentException("Projekt o id " + projektId + " nie istnieje"));
        Zadanie zadanie = zadanieRepository.findById(zadanieId)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie o id " + zadanieId + " nie istnieje"));

        zadanie.setProjekt(projekt);
        if (projekt.getZadania() == null) {
            projekt.setZadania(new ArrayList<>());
        }
        projekt.getZadania().add(zadanie);

        zadanieRepository.save(zadanie);
        projektRepository.save(projekt);
    }

    @Override
    public void removeZadanieFromProjekt(Integer projektId, Integer zadanieId) {
        Projekt projekt = projektRepository.findById(projektId)
                .orElseThrow(() -> new IllegalArgumentException("Projekt o id " + projektId + " nie istnieje"));
        Zadanie zadanie = zadanieRepository.findById(zadanieId)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie o id " + zadanieId + " nie istnieje"));

        if (projekt.getZadania() != null) {
            projekt.getZadania().remove(zadanie);
        }

        zadanie.setProjekt(null);

        zadanieRepository.save(zadanie);
        projektRepository.save(projekt);
    }

    @Override
    public void removeStudentFromProjekt(Integer projektId, Integer studentId) {
        Projekt projekt = projektRepository.findById(projektId)
                .orElseThrow(() -> new IllegalArgumentException("Projekt o id " + projektId + " nie istnieje"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student o id " + studentId + " nie istnieje"));

        if (projekt.getStudenci() != null) {
            projekt.getStudenci().remove(student);
        }

        if (student.getProjekty() != null) {
            student.getProjekty().remove(projekt);
        }

        studentRepository.save(student);
        projektRepository.save(projekt);
    }

    @Override
    public void addStudentToProjekt(Integer projektId, Integer studentId) {
        Projekt projekt = projektRepository.findById(projektId)
                .orElseThrow(() -> new IllegalArgumentException("Projekt o id " + projektId + " nie istnieje"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student o id " + studentId + " nie istnieje"));

        if (projekt.getStudenci() == null) {
            projekt.setStudenci(new HashSet<>());
        }
        projekt.getStudenci().add(student);

        if (student.getProjekty() == null) {
            student.setProjekty(new HashSet<>());
        }
        student.getProjekty().add(projekt);

        studentRepository.save(student);
        projektRepository.save(projekt);
    }

    @Override
    public Long getZadaniaCount(Integer projektId) {
        return zadanieRepository.countByProjektProjektId(projektId);
    }

    @Override
    public Long getStudentCount(Integer projektId) {
        return studentRepository.countByProjektyProjektId(projektId);
    }
}
