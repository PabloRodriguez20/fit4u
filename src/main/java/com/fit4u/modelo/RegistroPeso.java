package com.fit4u.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Peso corporal de un día. Solo hay un registro por fecha. */
@Entity
@Table(name = "peso_corporal")
public class RegistroPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "peso_kg", nullable = false)
    private double pesoKg;

    protected RegistroPeso() {
    }

    public RegistroPeso(LocalDate fecha, double pesoKg) {
        this.fecha = fecha;
        this.pesoKg = pesoKg;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }
}
