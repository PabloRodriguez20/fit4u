package com.fit4u.servicio;

/** Evolución del peso corporal: el registro más antiguo frente al más reciente. */
public record ResumenPeso(double inicial, double actual) {

    /** Kg perdidos. Es negativo si has ganado peso. */
    public double kgPerdidos() {
        return Math.round((inicial - actual) * 100) / 100.0;
    }
}
