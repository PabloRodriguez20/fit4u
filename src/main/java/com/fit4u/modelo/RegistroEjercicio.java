package com.fit4u.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDate;

/**
 * Campos comunes de un ejercicio realizado. Cada grupo muscular (Hombro, Pecho, ...)
 * hereda de esta clase y tiene su propia tabla en MySQL.
 */
@MappedSuperclass
public abstract class RegistroEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 100)
    private String ejercicio;

    @Column(name = "peso_kg", nullable = false)
    private double pesoKg;

    @Column(nullable = false)
    private int series;

    @Column(nullable = false)
    private int repeticiones;

    public void rellenar(LocalDate fecha, String ejercicio, double pesoKg, int series, int repeticiones) {
        this.fecha = fecha;
        this.ejercicio = ejercicio;
        this.pesoKg = pesoKg;
        this.series = series;
        this.repeticiones = repeticiones;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getEjercicio() {
        return ejercicio;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public int getSeries() {
        return series;
    }

    public int getRepeticiones() {
        return repeticiones;
    }
}
