package com.fit4u.ui;

import com.fit4u.modelo.Comida;
import com.fit4u.servicio.CaloriasService;
import com.fit4u.servicio.ResumenCalorias;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

class PanelCalorias extends JPanel {

    private final CaloriasService servicio;

    private final JLabel estado = Ui.texto(" ", "h1");
    private final JLabel detalle = new JLabel(" ");
    private final JProgressBar barra = new JProgressBar();

    private final JTextField nombre = new JTextField(22);
    private final JTextField kcal = new JTextField(7);

    private final ModeloTabla<Comida> modelo = new ModeloTabla<>(
            new String[]{"Comida", "Kcal"},
            List.<Function<Comida, Object>>of(Comida::getNombre, Comida::getKcal));
    private final JTable tabla = Ui.tabla(modelo);

    PanelCalorias(CaloriasService servicio) {
        super(new BorderLayout(0, 14));
        this.servicio = servicio;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton cambiarObjetivo = new JButton("Cambiar kcal diarias");
        cambiarObjetivo.addActionListener(e -> Ui.accion(this, this::cambiarObjetivo));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(estado, BorderLayout.CENTER);
        cabecera.add(cambiarObjetivo, BorderLayout.EAST);

        JPanel resumen = new JPanel(new GridLayout(0, 1, 0, 8));
        resumen.setOpaque(false);
        resumen.add(cabecera);
        resumen.add(barra);
        resumen.add(detalle);

        JButton anadir = Ui.botonPrincipal("Añadir comida");
        anadir.addActionListener(e -> Ui.accion(this, this::anadir));
        nombre.addActionListener(e -> kcal.requestFocusInWindow());
        kcal.addActionListener(e -> Ui.accion(this, this::anadir));

        JPanel filaBoton = new JPanel(new BorderLayout());
        filaBoton.setOpaque(false);
        filaBoton.add(new JLabel(" "), BorderLayout.NORTH);
        filaBoton.add(anadir, BorderLayout.CENTER);

        JPanel superior = new JPanel(new BorderLayout(0, 14));
        superior.setOpaque(false);
        superior.add(resumen, BorderLayout.NORTH);
        superior.add(Ui.fila(Ui.campo("Comida", nombre), Ui.campo("Kcal", kcal), filaBoton), BorderLayout.SOUTH);

        JButton eliminar = new JButton("Eliminar seleccionada");
        eliminar.addActionListener(e -> Ui.accion(this, this::eliminar));

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(Ui.fila(eliminar), BorderLayout.SOUTH);

        Ui.accion(this, this::refrescar);
    }

    final void refrescar() {
        LocalDate hoy = LocalDate.now();
        modelo.setFilas(servicio.comidasDe(hoy));
        pintar(servicio.resumen(hoy));
    }

    private void pintar(ResumenCalorias r) {
        Color color;
        if (r.excedido()) {
            color = Ui.ROJO;
            estado.setText("Te pasas " + (-r.restantes()) + " kcal");
        } else if (r.justoEnCero()) {
            color = Ui.NARANJA;
            estado.setText("0 kcal: has llegado a tu límite");
        } else {
            color = Ui.VERDE;
            estado.setText("Te quedan " + r.restantes() + " kcal");
        }
        estado.setForeground(color);
        barra.setForeground(color);
        barra.setMaximum(Math.max(r.objetivo(), 1));
        barra.setValue(Math.min(r.consumidas(), r.objetivo()));
        detalle.setText("Hoy llevas " + r.consumidas() + " de " + r.objetivo() + " kcal");
    }

    private void anadir() {
        int valor = Ui.leerEntero(kcal.getText(), "Kcal");
        LocalDate hoy = LocalDate.now();
        boolean yaExcedido = servicio.resumen(hoy).excedido();
        servicio.anadirComida(nombre.getText(), valor, hoy);
        nombre.setText("");
        kcal.setText("");
        nombre.requestFocusInWindow();
        refrescar();

        ResumenCalorias r = servicio.resumen(hoy);
        if (r.excedido()) {
            Ui.aviso(this, (yaExcedido ? "Sigues pasándote" : "Te has pasado")
                    + " de tu consumo diario: " + (-r.restantes()) + " kcal de más.");
        } else if (r.justoEnCero()) {
            JOptionPane.showMessageDialog(this, "Has llegado a 0: ya has consumido tus kcal de hoy.",
                    "Fit4U", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.aviso(this, "Selecciona primero una comida de la tabla.");
            return;
        }
        servicio.eliminarComida(modelo.fila(fila).getId());
        refrescar();
    }

    private void cambiarObjetivo() {
        String actual = servicio.objetivo().map(String::valueOf).orElse("");
        String respuesta = (String) JOptionPane.showInputDialog(this, "¿Cuántas kcal consumes al día?",
                "Cambiar kcal diarias", JOptionPane.QUESTION_MESSAGE, null, null, actual);
        if (respuesta != null) {
            servicio.guardarObjetivo(Ui.leerEntero(respuesta, "Kcal diarias"));
            refrescar();
        }
    }
}
