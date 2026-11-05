package com.fit4u.servicio;

import com.fit4u.modelo.GrupoMuscular;
import com.fit4u.modelo.RegistroEjercicio;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EjercicioService {

    private final SessionFactory sf;

    public EjercicioService(SessionFactory sf) {
        this.sf = sf;
    }

    public void anadir(GrupoMuscular grupo, LocalDate fecha, String ejercicio, double pesoKg, int series, int repeticiones) {
        String limpio = ejercicio == null ? "" : ejercicio.trim();
        if (limpio.isEmpty() || limpio.length() > 100) {
            throw new IllegalArgumentException("Escribe el nombre del ejercicio (máximo 100 caracteres).");
        }
        if (pesoKg < 0 || pesoKg > 1000) {
            throw new IllegalArgumentException("El peso levantado debe estar entre 0 y 1000 kg.");
        }
        if (series < 1 || series > 100) {
            throw new IllegalArgumentException("Las series deben estar entre 1 y 100.");
        }
        if (repeticiones < 1 || repeticiones > 1000) {
            throw new IllegalArgumentException("Las repeticiones deben estar entre 1 y 1000.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser futura.");
        }
        RegistroEjercicio registro = grupo.nuevoRegistro();
        registro.rellenar(fecha, limpio, pesoKg, series, repeticiones);
        sf.inTransaction(s -> s.persist(registro));
    }

    /** Del más reciente al más antiguo. */
    public List<RegistroEjercicio> historial(GrupoMuscular grupo) {
        String hql = "from " + grupo.entidad().getSimpleName() + " r order by r.fecha desc, r.id desc";
        return sf.fromTransaction(s -> new ArrayList<RegistroEjercicio>(
                s.createQuery(hql, grupo.entidad()).getResultList()));
    }

    /** Ejercicios ya usados en ese grupo, para no tener que reescribirlos. */
    public List<String> nombresUsados(GrupoMuscular grupo) {
        String hql = "select distinct r.ejercicio from " + grupo.entidad().getSimpleName() + " r order by r.ejercicio";
        return sf.fromTransaction(s -> s.createQuery(hql, String.class).getResultList());
    }

    /** Lo último que se hizo de ese ejercicio, para no tener que recordarlo. */
    public Optional<RegistroEjercicio> ultimo(GrupoMuscular grupo, String ejercicio) {
        String hql = "from " + grupo.entidad().getSimpleName()
                + " r where lower(r.ejercicio) = lower(:nombre) order by r.fecha desc, r.id desc";
        return sf.fromTransaction(s -> s.createQuery(hql, grupo.entidad())
                .setParameter("nombre", ejercicio.trim())
                .setMaxResults(1)
                .getResultList()
                .stream()
                .<RegistroEjercicio>map(r -> r)
                .findFirst());
    }

    /** Registros entre dos fechas (ambas incluidas), del más antiguo al más reciente. */
    public List<RegistroEjercicio> entre(GrupoMuscular grupo, LocalDate desde, LocalDate hasta) {
        String hql = "from " + grupo.entidad().getSimpleName()
                + " r where r.fecha between :desde and :hasta order by r.fecha, r.id";
        return sf.fromTransaction(s -> new ArrayList<RegistroEjercicio>(s.createQuery(hql, grupo.entidad())
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList()));
    }

    /** La fecha más reciente con algún ejercicio, de cualquier grupo, anterior a la indicada. */
    public Optional<LocalDate> ultimaFechaAntesDe(LocalDate fecha) {
        Optional<LocalDate> ultima = Optional.empty();
        for (GrupoMuscular grupo : GrupoMuscular.values()) {
            String hql = "select max(r.fecha) from " + grupo.entidad().getSimpleName() + " r where r.fecha < :fecha";
            LocalDate delGrupo = sf.fromTransaction(s -> s.createQuery(hql, LocalDate.class)
                    .setParameter("fecha", fecha)
                    .getSingleResultOrNull());
            if (delGrupo != null && (ultima.isEmpty() || delGrupo.isAfter(ultima.get()))) {
                ultima = Optional.of(delGrupo);
            }
        }
        return ultima;
    }

    public void eliminar(GrupoMuscular grupo, Long id) {
        sf.inTransaction(s -> {
            RegistroEjercicio registro = s.get(grupo.entidad(), id);
            if (registro != null) {
                s.remove(registro);
            }
        });
    }
}
