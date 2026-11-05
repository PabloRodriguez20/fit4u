package com.fit4u.ui;

import com.fit4u.servicio.Formato;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Utilidades compartidas por las pantallas: lectura de campos, formato y componentes comunes. */
final class Ui {

    static final Color VERDE = new Color(0x2E, 0x9E, 0x5B);
    static final Color NARANJA = new Color(0xE0, 0x8A, 0x00);
    static final Color ROJO = new Color(0xD6, 0x32, 0x2F);
    static final Color ACENTO = new Color(0xE4, 0x57, 0x2E);

    private Ui() {
    }

    /** Ejecuta una acción y muestra un mensaje si algo falla, en lugar de dejar la app en silencio. */
    static void accion(Component padre, Runnable accion) {
        try {
            accion.run();
        } catch (IllegalArgumentException e) {
            aviso(padre, e.getMessage());
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(padre,
                    "No se ha podido completar la operación:\n" + causa(e),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Fit4U", JOptionPane.WARNING_MESSAGE);
    }

    static String causa(Throwable e) {
        Throwable raiz = e;
        while (raiz.getCause() != null && raiz.getCause() != raiz) {
            raiz = raiz.getCause();
        }
        return raiz.getMessage() != null ? raiz.getMessage() : raiz.toString();
    }

    static int leerEntero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + ": escribe un número entero.");
        }
    }

    /** Acepta tanto coma como punto decimal (72,5 o 72.5). */
    static double leerDecimal(String texto, String campo) {
        try {
            double valor = Double.parseDouble(texto.trim().replace(',', '.'));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + ": escribe un número, por ejemplo 72,5.");
        }
    }

    static LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim(), Formato.FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha: usa el formato dd/mm/aaaa, por ejemplo 05/11/2026.");
        }
    }

    static String numero(double valor) {
        return Formato.numero(valor);
    }

    static String fecha(LocalDate fecha) {
        return Formato.fecha(fecha);
    }

    /** Un campo de formulario con su etiqueta encima. */
    static JPanel campo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    static JPanel fila(Component... componentes) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fila.setOpaque(false);
        for (Component c : componentes) {
            fila.add(c);
        }
        return fila;
    }

    static JButton botonPrincipal(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(ACENTO);
        boton.setForeground(Color.WHITE);
        return boton;
    }

    static JLabel texto(String contenido, String estilo) {
        JLabel etiqueta = new JLabel(contenido);
        etiqueta.putClientProperty("FlatLaf.styleClass", estilo);
        return etiqueta;
    }

    static JPanel tarjeta(JLabel titulo, JLabel valor) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 6));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(valor, BorderLayout.CENTER);
        return tarjeta;
    }

    static JTable tabla(ModeloTabla<?> modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(30);
        tabla.setShowHorizontalLines(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }
}
