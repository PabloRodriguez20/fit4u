package com.fit4u.servicio;

import com.fit4u.modelo.RegistroEjercicio;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/** Cómo se escriben fechas, números y marcas, tanto en pantalla como en los ficheros exportados. */
public final class Formato {

    public static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final DecimalFormat NUMERO =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES")));

    private Formato() {
    }

    public static String numero(double valor) {
        return NUMERO.format(valor);
    }

    public static String fecha(LocalDate fecha) {
        return FECHA.format(fecha);
    }

    /** Por ejemplo: 62,5 kg · 4 x 10 */
    public static String marca(RegistroEjercicio r) {
        return numero(r.getPesoKg()) + " kg · " + r.getSeries() + " x " + r.getRepeticiones();
    }
}
