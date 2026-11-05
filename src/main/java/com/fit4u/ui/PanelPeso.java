package com.fit4u.ui;

import com.fit4u.modelo.RegistroPeso;
import com.fit4u.servicio.PesoService;
import com.fit4u.servicio.ResumenPeso;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class PanelPeso extends JPanel {

    private final PesoService servicio;

    private final JLabel inicial = Ui.texto("—", "h2");
    private final JLabel actual = Ui.texto("—", "h2");
    private final JLabel perdidos = Ui.texto("—", "h2");
    private final JLabel tituloPerdidos = Ui.texto("Kg perdidos", "medium");

    private final JTextField fecha = new JTextField(9);
    private final JTextField peso = new JTextField(7);

    private final ModeloTabla<RegistroPeso> modelo = new ModeloTabla<>(
            new String[]{"Fecha", "Peso (kg)"},
            List.<Function<RegistroPeso, Object>>of(
                    r -> Ui.fecha(r.getFecha()),
                    r -> Ui.numero(r.getPesoKg())));
    private final JTable tabla = Ui.tabla(modelo);

    PanelPeso(PesoService servicio) {
        super(new BorderLayout(0, 14));
        this.servicio = servicio;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(Ui.tarjeta(Ui.texto("Peso inicial", "medium"), inicial));
        tarjetas.add(Ui.tarjeta(Ui.texto("Peso actual", "medium"), actual));
        tarjetas.add(Ui.tarjeta(tituloPerdidos, perdidos));

        ponerFechaDeHoy();
        JButton guardar = Ui.botonPrincipal("Guardar peso");
        guardar.addActionListener(e -> Ui.accion(this, this::guardar));
        peso.addActionListener(e -> Ui.accion(this, this::guardar));

        JPanel filaBoton = new JPanel(new BorderLayout());
        filaBoton.setOpaque(false);
        filaBoton.add(new JLabel(" "), BorderLayout.NORTH);
        filaBoton.add(guardar, BorderLayout.CENTER);

        JPanel superior = new JPanel(new BorderLayout(0, 14));
        superior.setOpaque(false);
        superior.add(tarjetas, BorderLayout.NORTH);
        superior.add(Ui.fila(Ui.campo("Fecha", fecha), Ui.campo("Peso (kg)", peso), filaBoton), BorderLayout.SOUTH);

        JButton eliminar = new JButton("Eliminar seleccionado");
        eliminar.addActionListener(e -> Ui.accion(this, this::eliminar));

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(Ui.fila(eliminar), BorderLayout.SOUTH);

        Ui.accion(this, this::refrescar);
    }

    final void ponerFechaDeHoy() {
        fecha.setText(Ui.fecha(LocalDate.now()));
    }

    private void refrescar() {
        modelo.setFilas(servicio.historial());
        Optional<ResumenPeso> resumen = servicio.resumen();
        if (resumen.isEmpty()) {
            inicial.setText("—");
            actual.setText("—");
            perdidos.setText("—");
            perdidos.setForeground(inicial.getForeground());
            tituloPerdidos.setText("Kg perdidos");
            return;
        }
        ResumenPeso r = resumen.get();
        inicial.setText(Ui.numero(r.inicial()) + " kg");
        actual.setText(Ui.numero(r.actual()) + " kg");
        double kg = r.kgPerdidos();
        tituloPerdidos.setText(kg < 0 ? "Kg ganados" : "Kg perdidos");
        perdidos.setText(Ui.numero(Math.abs(kg)) + " kg");
        perdidos.setForeground(kg > 0 ? Ui.VERDE : kg < 0 ? Ui.ROJO : inicial.getForeground());
    }

    private void guardar() {
        LocalDate dia = Ui.leerFecha(fecha.getText());
        servicio.registrar(dia, Ui.leerDecimal(peso.getText(), "Peso"));
        peso.setText("");
        refrescar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.aviso(this, "Selecciona primero un peso de la tabla.");
            return;
        }
        servicio.eliminar(modelo.fila(fila).getId());
        refrescar();
    }
}
