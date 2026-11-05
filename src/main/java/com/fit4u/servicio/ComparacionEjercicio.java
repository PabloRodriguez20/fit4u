package com.fit4u.servicio;

import com.fit4u.modelo.GrupoMuscular;
import com.fit4u.modelo.RegistroEjercicio;

/** Mejor marca de un ejercicio en dos semanas seguidas. Cualquiera de las dos puede ser null. */
public record ComparacionEjercicio(GrupoMuscular grupo, String ejercicio,
                                   RegistroEjercicio anterior, RegistroEjercicio actual) {

    /** Compara primero el peso y, si es el mismo, el total de repeticiones. */
    public String cambio() {
        if (anterior == null) {
            return "Nuevo";
        }
        if (actual == null) {
            return "Sin hacer";
        }
        double kg = Math.round((actual.getPesoKg() - anterior.getPesoKg()) * 100) / 100.0;
        if (kg != 0) {
            return (kg > 0 ? "+" : "-") + Formato.numero(Math.abs(kg)) + " kg";
        }
        int reps = totalRepeticiones(actual) - totalRepeticiones(anterior);
        if (reps != 0) {
            return (reps > 0 ? "+" : "-") + Math.abs(reps) + " reps";
        }
        return "Igual";
    }

    static int totalRepeticiones(RegistroEjercicio r) {
        return r.getSeries() * r.getRepeticiones();
    }
}
