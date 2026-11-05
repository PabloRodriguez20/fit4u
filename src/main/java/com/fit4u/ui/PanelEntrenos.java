package com.fit4u.ui;

import com.fit4u.modelo.GrupoMuscular;
import com.fit4u.modelo.RegistroEjercicio;
import com.fit4u.servicio.EjercicioService;
import com.fit4u.servicio.Formato;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

class PanelEntrenos extends JPanel {

    private final EjercicioService servicio;

    private final JList<GrupoMuscular> grupos = new JList<>(GrupoMuscular.values());
    private final JLabel titulo = Ui.texto(" ", "h2");

    private final JTextField fecha = new JTextField(9);
    private final JComboBox<String> ejercicio = new JComboBox<>();
    private final JTextField peso = new JTextField(6);
    private final JTextField series = new JTextField(4);
    private final JTextField repeticiones = new JTextField(4);
    private final JLabel ultimaVez = new JLabel(" ");
    private String ejercicioRellenado = "";

    private final ModeloTabla<RegistroEjercicio> modelo = new ModeloTabla<>(
            new String[]{"Fecha", "Ejercicio", "Peso (kg)", "Series", "Reps"},
            List.<Function<RegistroEjercicio, Object>>of(
                    r -> Ui.fecha(r.getFecha()),
                    RegistroEjercicio::getEjercicio,
                    r -> Ui.numero(r.getPesoKg()),
                    RegistroEjercicio::getSeries,
                    RegistroEjercicio::getRepeticiones));
    private final JTable tabla = Ui.tabla(modelo);

    PanelEntrenos(EjercicioService servicio) {
        super(new BorderLayout(14, 0));
        this.servicio = servicio;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        grupos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        grupos.setFixedCellHeight(38);
        grupos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Ui.accion(this, this::refrescar);
            }
        });
        JScrollPane listaGrupos = new JScrollPane(grupos);
        listaGrupos.setPreferredSize(new Dimension(130, 0));

        ejercicio.setEditable(true);
        ejercicio.setPreferredSize(new Dimension(190, ejercicio.getPreferredSize().height));
        ejercicio.addActionListener(e -> Ui.accion(this, this::recordarUltimaMarca));
        ponerFechaDeHoy();

        JButton anadir = Ui.botonPrincipal("Añadir");
        anadir.addActionListener(e -> Ui.accion(this, this::anadir));
        repeticiones.addActionListener(e -> Ui.accion(this, this::anadir));

        JPanel filaBoton = new JPanel(new BorderLayout());
        filaBoton.setOpaque(false);
        filaBoton.add(new JLabel(" "), BorderLayout.NORTH);
        filaBoton.add(anadir, BorderLayout.CENTER);

        JPanel superior = new JPanel(new BorderLayout(0, 12));
        superior.setOpaque(false);
        superior.add(titulo, BorderLayout.NORTH);
        superior.add(Ui.fila(
                Ui.campo("Fecha", fecha),
                Ui.campo("Ejercicio", ejercicio),
                Ui.campo("Peso (kg)", peso),
                Ui.campo("Series", series),
                Ui.campo("Reps", repeticiones),
                filaBoton), BorderLayout.CENTER);
        ultimaVez.setForeground(Ui.ACENTO);
        superior.add(Ui.fila(ultimaVez), BorderLayout.SOUTH);

        JButton eliminar = new JButton("Eliminar seleccionado");
        eliminar.addActionListener(e -> Ui.accion(this, this::eliminar));

        JPanel derecha = new JPanel(new BorderLayout(0, 12));
        derecha.setOpaque(false);
        derecha.add(superior, BorderLayout.NORTH);
        derecha.add(new JScrollPane(tabla), BorderLayout.CENTER);
        derecha.add(Ui.fila(eliminar), BorderLayout.SOUTH);

        add(listaGrupos, BorderLayout.WEST);
        add(derecha, BorderLayout.CENTER);

        grupos.setSelectedIndex(0);
    }

    final void ponerFechaDeHoy() {
        fecha.setText(Ui.fecha(LocalDate.now()));
    }

    private void refrescar() {
        GrupoMuscular grupo = grupos.getSelectedValue();
        titulo.setText(grupo.toString());
        modelo.setFilas(servicio.historial(grupo));
        ejercicio.setModel(new DefaultComboBoxModel<>(servicio.nombresUsados(grupo).toArray(new String[0])));
        ejercicio.setSelectedItem("");
        peso.setText("");
        series.setText("");
        repeticiones.setText("");
    }

    /**
     * Al elegir un ejercicio ya hecho, rellena el formulario con lo último que levantaste.
     * Solo lo hace al cambiar de ejercicio, para no pisar lo que hayas corregido a mano.
     */
    private void recordarUltimaMarca() {
        String nombre = String.valueOf(ejercicio.getEditor().getItem()).trim();
        if (nombre.equalsIgnoreCase(ejercicioRellenado)) {
            return;
        }
        ejercicioRellenado = nombre;
        ultimaVez.setText(" ");
        if (nombre.isEmpty()) {
            return;
        }
        servicio.ultimo(grupos.getSelectedValue(), nombre).ifPresent(r -> {
            peso.setText(Ui.numero(r.getPesoKg()));
            series.setText(String.valueOf(r.getSeries()));
            repeticiones.setText(String.valueOf(r.getRepeticiones()));
            ultimaVez.setText("Última vez (" + Ui.fecha(r.getFecha()) + "): " + Formato.marca(r));
        });
    }

    private void anadir() {
        LocalDate dia = Ui.leerFecha(fecha.getText());
        double kg = Ui.leerDecimal(peso.getText(), "Peso");
        int numSeries = Ui.leerEntero(series.getText(), "Series");
        int numReps = Ui.leerEntero(repeticiones.getText(), "Reps");
        String nombreEjercicio = String.valueOf(ejercicio.getEditor().getItem());

        servicio.anadir(grupos.getSelectedValue(), dia, nombreEjercicio, kg, numSeries, numReps);
        refrescar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.aviso(this, "Selecciona primero un ejercicio de la tabla.");
            return;
        }
        servicio.eliminar(grupos.getSelectedValue(), modelo.fila(fila).getId());
        refrescar();
    }
}
