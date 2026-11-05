package com.fit4u.servicio;

import com.fit4u.datos.Conexion;
import com.fit4u.modelo.GrupoMuscular;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanasTest {

    private static final LocalDate HOY = LocalDate.now();
    private static final LocalDate ESTE_LUNES = SemanaService.lunesDe(HOY);
    private static final LocalDate LUNES_PASADO = ESTE_LUNES.minusWeeks(1);

    @TempDir
    Path carpeta;

    private SessionFactory sf;
    private EjercicioService ejercicios;
    private SemanaService semanas;
    private Exportador exportador;

    @BeforeEach
    void abrir() {
        sf = Conexion.abrir("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        ejercicios = new EjercicioService(sf);
        semanas = new SemanaService(ejercicios);
        exportador = new Exportador(carpeta.resolve("Fit4U"), semanas, ejercicios);
    }

    @AfterEach
    void cerrar() {
        sf.close();
    }

    @Test
    void recuerdaLoUltimoDeCadaEjercicioSinDistinguirMayusculas() {
        assertTrue(ejercicios.ultimo(GrupoMuscular.PECHO, "Press banca").isEmpty());

        ejercicios.anadir(GrupoMuscular.PECHO, LUNES_PASADO, "Press banca", 60, 4, 10);
        ejercicios.anadir(GrupoMuscular.PECHO, LUNES_PASADO.plusDays(2), "Press banca", 62.5, 3, 8);
        ejercicios.anadir(GrupoMuscular.PECHO, LUNES_PASADO.plusDays(1), "Aperturas", 15, 3, 12);

        var ultimo = ejercicios.ultimo(GrupoMuscular.PECHO, " press BANCA ").orElseThrow();
        assertEquals(62.5, ultimo.getPesoKg());
        assertEquals(3, ultimo.getSeries());
        assertEquals(8, ultimo.getRepeticiones());
        assertTrue(ejercicios.ultimo(GrupoMuscular.HOMBRO, "Press banca").isEmpty());
    }

    @Test
    void comparaLaMejorMarcaDeCadaSemana() {
        ejercicios.anadir(GrupoMuscular.PECHO, LUNES_PASADO.minusDays(1), "Fuera de rango", 200, 1, 1);
        ejercicios.anadir(GrupoMuscular.PECHO, LUNES_PASADO, "Press banca", 60, 4, 10);
        ejercicios.anadir(GrupoMuscular.PECHO, LUNES_PASADO.plusDays(6), "Press banca", 50, 4, 12);
        ejercicios.anadir(GrupoMuscular.PECHO, ESTE_LUNES, "press banca", 62.5, 4, 8);
        ejercicios.anadir(GrupoMuscular.ESPALDA, LUNES_PASADO, "Remo", 50, 4, 10);
        ejercicios.anadir(GrupoMuscular.ESPALDA, ESTE_LUNES, "Remo", 50, 4, 11);
        ejercicios.anadir(GrupoMuscular.PIERNA, LUNES_PASADO, "Sentadilla", 100, 3, 5);
        ejercicios.anadir(GrupoMuscular.BICEPS, ESTE_LUNES, "Curl", 12, 3, 12);

        List<ComparacionEjercicio> c = semanas.comparar(ESTE_LUNES);

        assertEquals(List.of("press banca", "Remo", "Curl", "Sentadilla"),
                c.stream().map(ComparacionEjercicio::ejercicio).toList());
        assertEquals(60, c.get(0).anterior().getPesoKg());
        assertEquals(List.of("+2,5 kg", "+4 reps", "Nuevo", "Sin hacer"),
                c.stream().map(ComparacionEjercicio::cambio).toList());
    }

    @Test
    void exportaLosEjerciciosYLaComparacion() throws IOException {
        ejercicios.anadir(GrupoMuscular.TRICEPS, LUNES_PASADO, "Fondos", 10, 3, 10);
        ejercicios.anadir(GrupoMuscular.TRICEPS, ESTE_LUNES, "Fondos", 7.5, 3, 10);

        String texto = Files.readString(exportador.exportar(ESTE_LUNES));

        assertTrue(texto.contains("SEMANA DEL " + Formato.fecha(ESTE_LUNES)));
        assertTrue(texto.contains("TRÍCEPS"));
        assertTrue(texto.contains(Formato.fecha(ESTE_LUNES) + "  7,5 kg · 3 x 10"));
        assertTrue(texto.contains("Tríceps - Fondos: 10 kg · 3 x 10 -> 7,5 kg · 3 x 10 (-2,5 kg)"));
    }

    @Test
    void laSemanaTerminadaSeExportaUnaSolaVez() throws IOException {
        assertEquals(Optional.empty(), exportador.exportarSemanaTerminadaSiFalta(HOY));

        ejercicios.anadir(GrupoMuscular.HOMBRO, ESTE_LUNES, "De esta semana", 10, 1, 1);
        assertEquals(Optional.empty(), exportador.exportarSemanaTerminadaSiFalta(HOY));

        ejercicios.anadir(GrupoMuscular.HOMBRO, LUNES_PASADO, "Press militar", 40, 4, 10);
        assertEquals(Optional.of(LUNES_PASADO), exportador.exportarSemanaTerminadaSiFalta(HOY));
        assertTrue(Files.exists(exportador.fichero(LUNES_PASADO)));
        assertEquals(Optional.empty(), exportador.exportarSemanaTerminadaSiFalta(HOY));
    }

    @Test
    void siPasanSemanasSinAbrirExportaLaUltimaConDatos() {
        LocalDate haceTres = ESTE_LUNES.minusWeeks(3);
        ejercicios.anadir(GrupoMuscular.PIERNA, haceTres.plusDays(2), "Prensa", 100, 3, 10);

        assertEquals(Optional.of(haceTres), exportador.exportarSemanaTerminadaSiFalta(HOY));
    }

    @Test
    void unaExportacionManualAMitadDeSemanaSeRehaceAlTerminar() throws IOException {
        ejercicios.anadir(GrupoMuscular.HOMBRO, LUNES_PASADO, "Press militar", 40, 4, 10);
        Path fichero = exportador.exportar(LUNES_PASADO);
        Files.writeString(fichero, "incompleto");
        Files.setLastModifiedTime(fichero, FileTime.from(
                LUNES_PASADO.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        assertEquals(Optional.of(LUNES_PASADO), exportador.exportarSemanaTerminadaSiFalta(HOY));
        assertFalse(Files.readString(fichero).contains("incompleto"));
    }
}
