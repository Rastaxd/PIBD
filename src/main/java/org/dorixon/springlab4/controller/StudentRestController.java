package org.dorixon.springlab4.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.dorixon.springlab4.model.Student;
import org.dorixon.springlab4.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Tag(name = "Student")
public class StudentRestController {
    private final StudentService studentService;



    @GetMapping("/studenci/{studentId}")
    ResponseEntity<Student> getStudent(@PathVariable("studentId") Integer studentId) {
        return ResponseEntity.of(studentService.getStudent(studentId));
    }

    @PostMapping(path = "/studenci")
    ResponseEntity<Void> createStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.setStudent(student);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{studentId}").buildAndExpand(createdStudent.getStudentId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/studenci/{studentId}")
    public ResponseEntity<Void> updateStudent(@Valid @RequestBody Student student,
                                              @PathVariable("studentId") Integer studentId) {
        return studentService.getStudent(studentId)
                .map(s -> {
                    student.setStudentId(studentId);
                    studentService.setStudent(student);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/studenci/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("studentId") Integer studentId) {
        return studentService.getStudent(studentId).map(s -> {
            studentService.deleteStudent(studentId);
            return new ResponseEntity<Void>(HttpStatus.OK);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/studenci")
    Page<Student> getStudenci(Pageable pageable) {
        return studentService.getStudenci(pageable);
    }

    @GetMapping(value = "/projekty/{projektId}/studenci")
    Page<Student> getStudenciByProjekt(@PathVariable("projektId") Integer projektId, Pageable pageable) {
        return studentService.getStudenciByProjektId(projektId, pageable);
    }

    @GetMapping(value = "/studenci", params="nrIndeksu")
    ResponseEntity<Student> getStudentByNrIndeksu(@RequestParam("nrIndeksu") String nrIndeksu) {
        return ResponseEntity.of(studentService.getStudentByNrIndeksu(nrIndeksu));
    }

    @GetMapping(value = "/studenci/szukaj", params="nrIndeksu")
    Page<Student> findStudenciByNrIndeksuStartsWith(@RequestParam("nrIndeksu") String nrIndeksu, Pageable pageable) {
        return studentService.findByNrIndeksuStartsWith(nrIndeksu, pageable);
    }

    @GetMapping(value = "/studenci/szukaj", params="nazwisko")
    Page<Student> findStudenciByNazwisko(@RequestParam("nazwisko") String nazwisko, Pageable pageable) {
        return studentService.findByNazwiskoStartsWithIgnoreCase(nazwisko, pageable);
    }
}