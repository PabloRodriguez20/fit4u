package com.fit4u.ui;

import com.fit4u.servicio.CaloriasService;
import com.fit4u.servicio.PesoService;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.time.LocalDate;

/** Preguntas de la primera vez que se abre la aplicación. */
public final class DialogoBienvenida {

    private DialogoBienvenida() {
    }

    /** Devuelve false si el usuario cancela sin contestar. */
    public static boolean preguntar(CaloriasService calorias, PesoService peso) {
        JTextField kcal = new JTextField();
        JTextField pesoActual = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 8));
        formulario.add(Ui.texto("¡Bienvenido a Fit4U!", "h2"));
        formulario.add(Ui.campo("¿Cuántas kcal consumes al día?", kcal));
        formulario.add(Ui.campo("¿Cuánto pesas ahora, en kg? (opcional)", pesoActual));

        while (true) {
            int respuesta = JOptionPane.showConfirmDialog(null, formulario, "Fit4U",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (respuesta != JOptionPane.OK_OPTION) {
                return false;
            }
            try {
                int objetivo = Ui.leerEntero(kcal.getText(), "Kcal diarias");
                String textoPeso = pesoActual.getText().trim();
                Double kg = textoPeso.isEmpty() ? null : Ui.leerDecimal(textoPeso, "Peso");
                if (kg != null) {
                    peso.registrar(LocalDate.now(), kg);
                }
                calorias.guardarObjetivo(objetivo);
                return true;
            } catch (IllegalArgumentException e) {
                Ui.aviso(null, e.getMessage());
            } catch (RuntimeException e) {
                Ui.accion(null, () -> {
                    throw e;
                });
            }
        }
    }
}
