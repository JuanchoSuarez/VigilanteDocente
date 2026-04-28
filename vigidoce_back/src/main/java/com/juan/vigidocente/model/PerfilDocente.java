package com.juan.vigidocente.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfiles_docente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"perfil", "turnos", "registrosVigilancia"})
    @OneToOne
    @JoinColumn(name = "docente_id", nullable = false, unique = true)
    private Docente docente;

    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private Integer aniosExperiencia;

    @Column(nullable = false)
    private Integer puntosGamificacion;

    @Column
    private String reconocimientoTrimestral;
}
