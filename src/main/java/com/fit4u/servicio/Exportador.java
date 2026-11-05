package com.fit4u.servicio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

/** Guarda el informe de cada semana en un .txt que se abre con el Bloc de notas. */
public class Exportador {

    private final Path carpeta;
    private final SemanaService semanas;
    private final EjercicioService ejercicios;

    public Exportador(Path carpeta, SemanaService semanas, EjercicioService ejercicios) {
        this.carpeta = carpeta;
        this.semanas = semanas;
        this.ejercicios = ejercicios;
    }

    public Path carpeta() {
        return carpeta;
    }

    public Path fichero(LocalDate lunes) {
        return carpeta.resolve("semana_" + lunes + ".txt");
    }

    /** Escribe el fichero de esa semana, sustituyendo el anterior si ya existía. */
    public Path exportar(LocalDate lunes) {
        Path destino = fichero(lunes);
        try {
            Files.createDirectories(carpeta);
            Files.writeString(destino, semanas.informe(lunes), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir " + destino, e);
        }
        return destino;
    }

    /**
     * Exporta la última semana terminada con ejercicios si aún no tiene su fichero definitivo.
     * Un fichero creado a mano antes de que acabara la semana está incompleto, así que se rehace.
     *
     * @return el lunes de la semana exportada, o vacío si no había nada que hacer
     */
    public Optional<LocalDate> exportarSemanaTerminadaSiFalta(LocalDate hoy) {
        Optional<LocalDate> ultimaFecha = ejercicios.ultimaFechaAntesDe(SemanaService.lunesDe(hoy));
        if (ultimaFecha.isEmpty()) {
            return Optional.empty();
        }
        LocalDate lunes = SemanaService.lunesDe(ultimaFecha.get());
        if (estaCompleto(fichero(lunes), lunes)) {
            return Optional.empty();
        }
        exportar(lunes);
        return Optional.of(lunes);
    }

    private static boolean estaCompleto(Path fichero, LocalDate lunes) {
        if (!Files.exists(fichero)) {
            return false;
        }
        try {
            LocalDate modificado = Files.getLastModifiedTime(fichero).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            return modificado.isAfter(lunes.plusDays(6));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + fichero, e);
        }
    }
}
