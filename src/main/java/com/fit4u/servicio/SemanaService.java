package com.fit4u.servicio;

import com.fit4u.modelo.GrupoMuscular;
import com.fit4u.modelo.RegistroEjercicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Resumen de una semana (de lunes a domingo) y comparación con la semana anterior. */
public class SemanaService {

    private static final String NL = System.lineSeparator();

    private static final Comparator<RegistroEjercicio> MEJOR_MARCA = Comparator
            .comparingDouble(RegistroEjercicio::getPesoKg)
            .thenComparingInt(ComparacionEjercicio::totalRepeticiones);

    private final EjercicioService ejercicios;

    public SemanaService(EjercicioService ejercicios) {
        this.ejercicios = ejercicios;
    }

    public static LocalDate lunesDe(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** Un elemento por ejercicio hecho en esa semana o en la anterior, ordenado por grupo y nombre. */
    public List<ComparacionEjercicio> comparar(LocalDate lunes) {
        List<ComparacionEjercicio> resultado = new ArrayList<>();
        for (GrupoMuscular grupo : GrupoMuscular.values()) {
            Map<String, RegistroEjercicio> actuales =
                    mejoresMarcas(ejercicios.entre(grupo, lunes, lunes.plusDays(6)));
            Map<String, RegistroEjercicio> anteriores =
                    mejoresMarcas(ejercicios.entre(grupo, lunes.minusDays(7), lunes.minusDays(1)));

            Map<String, Boolean> claves = new TreeMap<>();
            actuales.keySet().forEach(k -> claves.put(k, true));
            anteriores.keySet().forEach(k -> claves.put(k, true));

            for (String clave : claves.keySet()) {
                RegistroEjercicio actual = actuales.get(clave);
                RegistroEjercicio anterior = anteriores.get(clave);
                String nombre = (actual != null ? actual : anterior).getEjercicio();
                resultado.add(new ComparacionEjercicio(grupo, nombre, anterior, actual));
            }
        }
        return resultado;
    }

    /** Agrupa ignorando mayúsculas: "Press banca" y "press banca" son el mismo ejercicio. */
    private static Map<String, RegistroEjercicio> mejoresMarcas(List<RegistroEjercicio> registros) {
        Map<String, RegistroEjercicio> mejores = new LinkedHashMap<>();
        for (RegistroEjercicio r : registros) {
            mejores.merge(clave(r.getEjercicio()), r,
                    (a, b) -> MEJOR_MARCA.compare(b, a) >= 0 ? b : a);
        }
        return mejores;
    }

    private static String clave(String ejercicio) {
        return ejercicio.trim().toLowerCase(Locale.ROOT);
    }

    /** El texto del fichero de exportación de esa semana. */
    public String informe(LocalDate lunes) {
        StringBuilder t = new StringBuilder();
        t.append("FIT4U - SEMANA DEL ").append(Formato.fecha(lunes))
                .append(" AL ").append(Formato.fecha(lunes.plusDays(6))).append(NL);
        t.append("=".repeat(50)).append(NL).append(NL);

        boolean hayEjercicios = false;
        for (GrupoMuscular grupo : GrupoMuscular.values()) {
            List<RegistroEjercicio> registros = ejercicios.entre(grupo, lunes, lunes.plusDays(6));
            if (registros.isEmpty()) {
                continue;
            }
            hayEjercicios = true;
            t.append(grupo.toString().toUpperCase(Locale.ROOT)).append(NL);

            Map<String, List<RegistroEjercicio>> porEjercicio = new TreeMap<>();
            for (RegistroEjercicio r : registros) {
                porEjercicio.computeIfAbsent(clave(r.getEjercicio()), k -> new ArrayList<>()).add(r);
            }
            for (List<RegistroEjercicio> delEjercicio : porEjercicio.values()) {
                t.append("  ").append(delEjercicio.get(0).getEjercicio()).append(NL);
                for (RegistroEjercicio r : delEjercicio) {
                    t.append("    ").append(Formato.fecha(r.getFecha()))
                            .append("  ").append(Formato.marca(r)).append(NL);
                }
            }
            t.append(NL);
        }
        if (!hayEjercicios) {
            t.append("Esta semana no hay ejercicios registrados.").append(NL).append(NL);
        }

        t.append("COMPARACIÓN CON LA SEMANA ANTERIOR (").append(Formato.fecha(lunes.minusDays(7)))
                .append(" al ").append(Formato.fecha(lunes.minusDays(1))).append(")").append(NL);
        t.append("Mejor marca de cada ejercicio: peso · series x repeticiones").append(NL);
        t.append("-".repeat(50)).append(NL);
        List<ComparacionEjercicio> comparaciones = comparar(lunes);
        if (comparaciones.isEmpty()) {
            t.append("No hay datos que comparar.").append(NL);
        }
        for (ComparacionEjercicio c : comparaciones) {
            t.append(c.grupo()).append(" - ").append(c.ejercicio()).append(": ")
                    .append(c.anterior() != null ? Formato.marca(c.anterior()) : "sin datos")
                    .append(" -> ")
                    .append(c.actual() != null ? Formato.marca(c.actual()) : "sin datos")
                    .append(" (").append(c.cambio()).append(")").append(NL);
        }
        return t.toString();
    }
}
