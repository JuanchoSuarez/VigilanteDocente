package com.juan.vigidocente.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reasignaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reasignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"reasignaciones", "incidentes", "registrosVigilancia", "docente", "zona"})
    @ManyToOne
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @JsonIgnoreProperties({"turnos", "registrosVigilancia", "perfil"})
    @ManyToOne
    @JoinColumn(name = "docente_original_id", nullable = false)
    private Docente docenteOriginal;

    @JsonIgnoreProperties({"turnos", "registrosVigilancia", "perfil"})
    @ManyToOne
    @JoinColumn(name = "docente_reemplazo_id")
    private Docente docenteReemplazo;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fechaHoraSolicitud;

    private LocalDateTime fechaHoraRespuesta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReasignacion estado;
}
