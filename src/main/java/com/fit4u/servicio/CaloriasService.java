package com.fit4u.servicio;

import com.fit4u.modelo.Comida;
import com.fit4u.modelo.Perfil;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CaloriasService {

    private static final int MAX_KCAL = 20000;

    private final SessionFactory sf;

    public CaloriasService(SessionFactory sf) {
        this.sf = sf;
    }

    /** Vacío si aún no se han contestado las preguntas iniciales. */
    public Optional<Integer> objetivo() {
        return sf.fromTransaction(s -> Optional.ofNullable(s.get(Perfil.class, Perfil.ID_UNICO))
                .map(Perfil::getKcalDiarias));
    }

    public void guardarObjetivo(int kcalDiarias) {
        if (kcalDiarias < 1 || kcalDiarias > MAX_KCAL) {
            throw new IllegalArgumentException("Las kcal diarias deben estar entre 1 y " + MAX_KCAL + ".");
        }
        sf.inTransaction(s -> s.merge(new Perfil(kcalDiarias)));
    }

    public void anadirComida(String nombre, int kcal, LocalDate fecha) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty() || limpio.length() > 100) {
            throw new IllegalArgumentException("Escribe el nombre de la comida (máximo 100 caracteres).");
        }
        if (kcal < 1 || kcal > MAX_KCAL) {
            throw new IllegalArgumentException("Las kcal de la comida deben estar entre 1 y " + MAX_KCAL + ".");
        }
        sf.inTransaction(s -> s.persist(new Comida(limpio, kcal, fecha)));
    }

    public List<Comida> comidasDe(LocalDate fecha) {
        return sf.fromTransaction(s -> s
                .createQuery("from Comida c where c.fecha = :fecha order by c.id", Comida.class)
                .setParameter("fecha", fecha)
                .getResultList());
    }

    public void eliminarComida(Long id) {
        sf.inTransaction(s -> {
            Comida comida = s.get(Comida.class, id);
            if (comida != null) {
                s.remove(comida);
            }
        });
    }

    public ResumenCalorias resumen(LocalDate fecha) {
        int objetivo = objetivo().orElse(0);
        int consumidas = comidasDe(fecha).stream().mapToInt(Comida::getKcal).sum();
        return new ResumenCalorias(objetivo, consumidas);
    }
}
