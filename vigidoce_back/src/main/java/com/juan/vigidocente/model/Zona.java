package com.juan.vigidocente.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "zonas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoZona tipo;

    @Column(nullable = false)
    private Integer capacidadMaxima;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "pos_x")
    private Double posX;

    @Column(name = "pos_y")
    private Double posY;

    @Column(name = "pos_width")
    private Double posWidth;

    @Column(name = "pos_height")
    private Double posHeight;

    @Column(name = "imagen_mapa_url")
    private String imagenMapaUrl;

    @JsonIgnoreProperties({"zona", "docente", "incidentes", "registrosVigilancia", "reasignaciones"})
    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Turno> turnos;
}
