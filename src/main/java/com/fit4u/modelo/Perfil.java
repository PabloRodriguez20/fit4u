package com.fit4u.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Datos del usuario. La aplicación es de una sola persona, así que solo hay una fila (id = 1). */
@Entity
@Table(name = "perfil")
public class Perfil {

    public static final long ID_UNICO = 1L;

    @Id
    private Long id = ID_UNICO;

    @Column(name = "kcal_diarias", nullable = false)
    private int kcalDiarias;

    protected Perfil() {
    }

    public Perfil(int kcalDiarias) {
        this.kcalDiarias = kcalDiarias;
    }

    public int getKcalDiarias() {
        return kcalDiarias;
    }
}
