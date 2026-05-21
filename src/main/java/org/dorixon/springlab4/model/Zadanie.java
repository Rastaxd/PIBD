package org.dorixon.springlab4.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="zadanie")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class Zadanie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="zadanie_id")
    private Integer zadanieId;

    @ManyToOne
    @JoinColumn(name="projekt_id")
    @JsonIgnoreProperties({"zadania"})
    private Projekt projekt;

    @Column(nullable = false, length = 50)
    private String nazwa;

    @Column(nullable = false)
    private Integer kolejnosc;

    @Column(nullable = false, length = 1000)
    private String opis;

    @CreatedDate
    @Column(name="dataczas_utworzenia", nullable = false, updatable = false)
    private LocalDateTime dataczasUtworzenia;

    @LastModifiedDate
    @Column(name="dataczas_modyfikacji")
    private LocalDateTime dataczasModyfikacji;


}
