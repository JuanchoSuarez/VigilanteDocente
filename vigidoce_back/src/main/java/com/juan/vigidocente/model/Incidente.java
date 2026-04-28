package com.juan.vigidocente.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"incidentes", "registrosVigilancia", "reasignaciones", "docente", "zona"})
    @ManyToOne
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @JsonIgnoreProperties({"turnos"})
    @ManyToOne
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoIncidente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeveridadIncidente severidad;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String observacionEstudiante;
}
