package com.fit4u.modelo;

import java.util.function.Supplier;

/** Los seis grupos musculares; cada uno enlaza con su entidad y, por tanto, con su tabla. */
public enum GrupoMuscular {
    HOMBRO("Hombro", Hombro.class, Hombro::new),
    PECHO("Pecho", Pecho.class, Pecho::new),
    TRICEPS("Tríceps", Triceps.class, Triceps::new),
    ESPALDA("Espalda", Espalda.class, Espalda::new),
    BICEPS("Bíceps", Biceps.class, Biceps::new),
    PIERNA("Pierna", Pierna.class, Pierna::new);

    private final String etiqueta;
    private final Class<? extends RegistroEjercicio> entidad;
    private final Supplier<RegistroEjercicio> constructor;

    GrupoMuscular(String etiqueta, Class<? extends RegistroEjercicio> entidad, Supplier<RegistroEjercicio> constructor) {
        this.etiqueta = etiqueta;
        this.entidad = entidad;
        this.constructor = constructor;
    }

    public Class<? extends RegistroEjercicio> entidad() {
        return entidad;
    }

    public RegistroEjercicio nuevoRegistro() {
        return constructor.get();
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
