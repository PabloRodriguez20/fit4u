package com.fit4u.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "comida")
public class Comida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private int kcal;

    @Column(nullable = false)
    private LocalDate fecha;

    protected Comida() {
    }

    public Comida(String nombre, int kcal, LocalDate fecha) {
        this.nombre = nombre;
        this.kcal = kcal;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getKcal() {
        return kcal;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
