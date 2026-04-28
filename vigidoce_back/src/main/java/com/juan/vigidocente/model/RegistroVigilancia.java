package com.juan.vigidocente.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_vigilancia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroVigilancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"registrosVigilancia", "incidentes", "reasignaciones", "docente", "zona"})
    @ManyToOne
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @JsonIgnoreProperties({"turnos", "registrosVigilancia", "perfil"})
    @ManyToOne
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @JsonIgnoreProperties({"turnos"})
    @ManyToOne
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(nullable = false)
    private LocalDateTime fechaHoraCheckIn;

    private LocalDateTime fechaHoraCheckOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoRegistro metodoRegistro;

    @Column(nullable = false)
    private Boolean recorridoRealizado = false;

    @Column
    private Integer calificacionLimpieza;
}
