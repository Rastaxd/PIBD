package org.dorixon.springlab4.controller;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.dorixon.springlab4.model.Projekt;
import org.dorixon.springlab4.service.ProjektService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


@RestController
@RequestMapping("/api")
@Tag(name = "Projekt")

@AllArgsConstructor
public class ProjektRestController {
    private final ProjektService projektService;
    private final org.dorixon.springlab4.validation.ValidationService<Projekt> validationService;

    @GetMapping("/projekty/{projektId}")
    public ResponseEntity<Projekt> getProjekt(@PathVariable("projektId") Integer projektId)
    {
        return ResponseEntity.of(projektService.getProjekt(projektId));
    }

    @PostMapping("/projekty")
    public ResponseEntity<Void> createProjekt(@RequestBody Projekt projekt) {
        validationService.validate(projekt);
        Projekt savedProjekt = projektService.setProjekt(projekt);
        if (savedProjekt == null) {
            return ResponseEntity.badRequest().build();
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedProjekt.getProjektId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/projekty/{projektyId}")
    public ResponseEntity<Void> updateProjekt(@RequestBody Projekt projekt, @PathVariable("projektyId") Integer projektyId)
    {
        validationService.validate(projekt);
        return projektService.getProjekt(projektyId).map(
                p -> {
                    projekt.setProjektId(projektyId);
                    projektService.setProjekt(projekt);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                }
        ).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/projekty/{projektId}")
    public ResponseEntity<Void> deleteProjekt(@PathVariable("projektId") Integer projektId)
    {
        return projektService.getProjekt(projektId).map(
                p -> {
                    projektService.deleteProjekt(projektId);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                }
        ).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/projekty")
    public Page<Projekt> getProjekty(Pageable pageable)
    {
        return projektService.getProjekty(pageable);
    }

    @GetMapping(value = "/projekty", params = "nazwa")
    Page<Projekt> getProjektByNazwa(@RequestParam(name="nazwa") String nazwa, Pageable pageable)
    {
        return projektService.searchByNazwa(nazwa, pageable);
    }
    
    @PostMapping("/projekty/{projektId}/zadania/{zadanieId}")
    public ResponseEntity<Void> addZadanieToProjekt(@PathVariable("projektId") Integer projektId, @PathVariable("zadanieId") Integer zadanieId)
    {
        projektService.addZadanieToProjekt(projektId, zadanieId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/projekty/{projektId}/zadania/{zadanieId}")
    public ResponseEntity<Void> removeZadanieFromProjekt(@PathVariable("projektId") Integer projektId, @PathVariable("zadanieId") Integer zadanieId)
    {
        projektService.removeZadanieFromProjekt(projektId, zadanieId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("projekty/{projektId}/studenci/{studentId}")
    public ResponseEntity<Void> addStudentToProjekt(@PathVariable("projektId") Integer projektId, @PathVariable("studentId") Integer studentId)
    {
        projektService.addStudentToProjekt(projektId, studentId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("projekty/{projektId}/studenci/{studentId}")
    public ResponseEntity<Void> removeStudentFromProjekt(@PathVariable("projektId") Integer projektId, @PathVariable("studentId") Integer studentId)
    {
        projektService.removeStudentFromProjekt(projektId, studentId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/projekty/{projektId}/stats")
    public ResponseEntity<Map<String, Long>> getProjektStats(@PathVariable("projektId") Integer projektId)
    {
        Map<String, Long> stats = new HashMap<>();
        stats.put("zadaniaCount", projektService.getZadaniaCount(projektId));
        stats.put("studentCount", projektService.getStudentCount(projektId));
        return ResponseEntity.ok(stats);
    }


}
