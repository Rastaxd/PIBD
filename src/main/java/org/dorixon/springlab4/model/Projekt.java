package org.dorixon.springlab4.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@Entity
@Table(name="projekt", indexes = {
        @Index(name = "idx_projekt_nazwa", columnList = "nazwa")
}) 

@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Projekt {


    public Projekt(Integer projektId, String nazwa, String opis, LocalDate lastModifiedDate) {
        this.projektId = projektId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.dataczasUtworzenia = LocalDateTime.now();
        this.dataczasModyfikacji = lastModifiedDate != null ? lastModifiedDate.atStartOfDay() : null;
    }

    public Projekt(String nazwa, String opis, LocalDate of) {
        this.nazwa = nazwa;
        this.opis = opis;
        this.dataczasUtworzenia = LocalDateTime.now();
    }
    
    public Projekt(Integer projektId, String nazwa, String opis, LocalDateTime createdDate, LocalDate lastModifiedDate)
    {
        this.projektId = projektId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.dataczasUtworzenia = createdDate;
        this.dataczasModyfikacji = lastModifiedDate.atStartOfDay();
    }

    public Projekt(int i, String nazwa1, String opis1, LocalDate of) {
        this.projektId = i;
        this.nazwa = nazwa1;
        this.opis = opis1;
        this.dataczasModyfikacji = of.atStartOfDay();
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="projekt_id")
    private Integer projektId;


    @NotBlank(message = "Nazwa projektu nie może być pusta")
    @Column(nullable = false, length = 50)
    private String nazwa;

    @Column(nullable = false, length = 1000)
    private String opis;

    @CreatedDate
    @Column(name="dataczas_utworzenia", nullable = false, updatable = false)
    private LocalDateTime dataczasUtworzenia;

    @LastModifiedDate
    @Column(name="dataczas_modyfikacji", insertable = false)
    private LocalDateTime dataczasModyfikacji;
    
    @Column(name = "data_oddania")
    private LocalDate dataOddania;



    @OneToMany(mappedBy = "projekt", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"projekt"})
    private List<Zadanie> zadania;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "projekt_student",
            joinColumns = @JoinColumn(name = "projekt_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnoreProperties("projekty")
    private Set<Student> studenci;


}
