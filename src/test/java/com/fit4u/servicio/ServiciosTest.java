package com.fit4u.servicio;

import com.fit4u.datos.Conexion;
import com.fit4u.modelo.GrupoMuscular;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba la lógica contra una base de datos H2 en memoria, en modo compatible con MySQL. */
class ServiciosTest {

    private static final LocalDate HOY = LocalDate.now();

    private SessionFactory sf;
    private CaloriasService calorias;
    private PesoService peso;
    private EjercicioService ejercicios;

    @BeforeEach
    void abrir() {
        sf = Conexion.abrir("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        calorias = new CaloriasService(sf);
        peso = new PesoService(sf);
        ejercicios = new EjercicioService(sf);
    }

    @AfterEach
    void cerrar() {
        sf.close();
    }

    @Test
    void sinPreguntasContestadasNoHayObjetivo() {
        assertTrue(calorias.objetivo().isEmpty());
        calorias.guardarObjetivo(2000);
        assertEquals(2000, calorias.objetivo().orElseThrow());
        calorias.guardarObjetivo(2200);
        assertEquals(2200, calorias.objetivo().orElseThrow());
    }

    @Test
    void lasComidasRestanHastaCeroYLuegoAvisaDeExceso() {
        calorias.guardarObjetivo(1000);

        calorias.anadirComida("Desayuno", 400, HOY);
        ResumenCalorias r = calorias.resumen(HOY);
        assertEquals(600, r.restantes());
        assertFalse(r.justoEnCero());
        assertFalse(r.excedido());

        calorias.anadirComida("Comida", 600, HOY);
        r = calorias.resumen(HOY);
        assertEquals(0, r.restantes());
        assertTrue(r.justoEnCero());
        assertFalse(r.excedido());

        calorias.anadirComida("Postre", 150, HOY);
        r = calorias.resumen(HOY);
        assertEquals(-150, r.restantes());
        assertTrue(r.excedido());
    }

    @Test
    void cadaDiaEmpiezaDeCeroYEliminarDevuelveLasKcal() {
        calorias.guardarObjetivo(1000);
        calorias.anadirComida("Ayer", 900, HOY.minusDays(1));
        calorias.anadirComida("Hoy", 300, HOY);
        assertEquals(700, calorias.resumen(HOY).restantes());

        calorias.eliminarComida(calorias.comidasDe(HOY).get(0).getId());
        assertEquals(1000, calorias.resumen(HOY).restantes());
    }

    @Test
    void comidasInvalidasSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> calorias.anadirComida("  ", 100, HOY));
        assertThrows(IllegalArgumentException.class, () -> calorias.anadirComida("Pan", 0, HOY));
        assertThrows(IllegalArgumentException.class, () -> calorias.guardarObjetivo(0));
    }

    @Test
    void cadaGrupoMuscularGuardaEnSuPropiaTabla() {
        for (GrupoMuscular grupo : GrupoMuscular.values()) {
            ejercicios.anadir(grupo, HOY, "Ejercicio de " + grupo, 42.5, 4, 10);
        }
        for (GrupoMuscular grupo : GrupoMuscular.values()) {
            List<?> historial = ejercicios.historial(grupo);
            assertEquals(1, historial.size());
            assertEquals(grupo.entidad(), historial.get(0).getClass());
            assertEquals(List.of("Ejercicio de " + grupo), ejercicios.nombresUsados(grupo));
        }

        Long tabla = sf.fromTransaction(s -> s.createNativeQuery("select count(*) from pecho", Long.class).getSingleResult());
        assertEquals(1L, tabla);
    }

    @Test
    void ejercicioConservaLosDatosYSePuedeEliminar() {
        ejercicios.anadir(GrupoMuscular.PECHO, HOY.minusDays(1), "Press banca", 60, 4, 10);
        ejercicios.anadir(GrupoMuscular.PECHO, HOY, "Press banca", 62.5, 4, 8);

        var historial = ejercicios.historial(GrupoMuscular.PECHO);
        assertEquals(HOY, historial.get(0).getFecha());
        assertEquals(62.5, historial.get(0).getPesoKg());
        assertEquals(4, historial.get(0).getSeries());
        assertEquals(8, historial.get(0).getRepeticiones());
        assertEquals(List.of("Press banca"), ejercicios.nombresUsados(GrupoMuscular.PECHO));

        ejercicios.eliminar(GrupoMuscular.PECHO, historial.get(0).getId());
        assertEquals(1, ejercicios.historial(GrupoMuscular.PECHO).size());
    }

    @Test
    void ejerciciosInvalidosSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> ejercicios.anadir(GrupoMuscular.PIERNA, HOY, " ", 10, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> ejercicios.anadir(GrupoMuscular.PIERNA, HOY, "Sentadilla", -1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> ejercicios.anadir(GrupoMuscular.PIERNA, HOY, "Sentadilla", 10, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> ejercicios.anadir(GrupoMuscular.PIERNA, HOY.plusDays(1), "Sentadilla", 10, 1, 1));
    }

    @Test
    void kgPerdidosComparaElPrimerYUltimoPeso() {
        assertTrue(peso.resumen().isEmpty());

        peso.registrar(HOY.minusDays(30), 85.5);
        peso.registrar(HOY.minusDays(10), 83.2);
        peso.registrar(HOY, 82.3);

        ResumenPeso r = peso.resumen().orElseThrow();
        assertEquals(85.5, r.inicial());
        assertEquals(82.3, r.actual());
        assertEquals(3.2, r.kgPerdidos());

        peso.registrar(HOY.minusDays(40), 80);
        assertEquals(-2.3, peso.resumen().orElseThrow().kgPerdidos());
    }

    @Test
    void registrarDosVecesElMismoDiaActualiza() {
        peso.registrar(HOY, 80);
        peso.registrar(HOY, 79.5);
        assertEquals(1, peso.historial().size());
        assertEquals(79.5, peso.historial().get(0).getPesoKg());
    }

    @Test
    void pesosInvalidosSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> peso.registrar(HOY, 5));
        assertThrows(IllegalArgumentException.class, () -> peso.registrar(HOY.plusDays(1), 80));
    }
}
