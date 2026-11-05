package com.fit4u.servicio;

import com.fit4u.modelo.RegistroPeso;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PesoService {

    private final SessionFactory sf;

    public PesoService(SessionFactory sf) {
        this.sf = sf;
    }

    /** Guarda el peso de ese día. Si ya había uno en esa fecha, lo actualiza. */
    public void registrar(LocalDate fecha, double pesoKg) {
        if (pesoKg < 20 || pesoKg > 500) {
            throw new IllegalArgumentException("El peso debe estar entre 20 y 500 kg.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser futura.");
        }
        sf.inTransaction(s -> {
            RegistroPeso existente = s
                    .createQuery("from RegistroPeso r where r.fecha = :fecha", RegistroPeso.class)
                    .setParameter("fecha", fecha)
                    .uniqueResult();
            if (existente != null) {
                existente.setPesoKg(pesoKg);
            } else {
                s.persist(new RegistroPeso(fecha, pesoKg));
            }
        });
    }

    /** Del más reciente al más antiguo. */
    public List<RegistroPeso> historial() {
        return sf.fromTransaction(s -> s
                .createQuery("from RegistroPeso r order by r.fecha desc", RegistroPeso.class)
                .getResultList());
    }

    public void eliminar(Long id) {
        sf.inTransaction(s -> {
            RegistroPeso registro = s.get(RegistroPeso.class, id);
            if (registro != null) {
                s.remove(registro);
            }
        });
    }

    /** Vacío si todavía no hay ningún peso registrado. */
    public Optional<ResumenPeso> resumen() {
        List<RegistroPeso> historial = historial();
        if (historial.isEmpty()) {
            return Optional.empty();
        }
        double actual = historial.get(0).getPesoKg();
        double inicial = historial.get(historial.size() - 1).getPesoKg();
        return Optional.of(new ResumenPeso(inicial, actual));
    }
}
