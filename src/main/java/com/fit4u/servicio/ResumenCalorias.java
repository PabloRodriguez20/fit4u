package com.fit4u.servicio;

/** Situación de las calorías de un día respecto al objetivo. */
public record ResumenCalorias(int objetivo, int consumidas) {

    /** Lo que queda por consumir. Es negativo si te has pasado. */
    public int restantes() {
        return objetivo - consumidas;
    }

    public boolean excedido() {
        return consumidas > objetivo;
    }

    public boolean justoEnCero() {
        return consumidas == objetivo;
    }
}
