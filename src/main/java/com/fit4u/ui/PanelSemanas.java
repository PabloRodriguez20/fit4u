package com.fit4u.ui;

import com.fit4u.servicio.ComparacionEjercicio;
import com.fit4u.servicio.Exportador;
import com.fit4u.servicio.Formato;
import com.fit4u.servicio.SemanaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

class PanelSemanas extends JPanel {

    private final SemanaService semanas;
    private final Exportador exportador;

    private final JLabel titulo = Ui.texto(" ", "h2");
    private final JButton siguiente = new JButton("Siguiente →");

    private final ModeloTabla<ComparacionEjercicio> modelo = new ModeloTabla<>(
            new String[]{"Grupo", "Ejercicio", "Semana anterior", "Semana elegida", "Cambio"},
            List.<Function<ComparacionEjercicio, Object>>of(
                    ComparacionEjercicio::grupo,
                    ComparacionEjercicio::ejercicio,
                    c -> c.anterior() != null ? Formato.marca(c.anterior()) : "—",
                    c -> c.actual() != null ? Formato.marca(c.actual()) : "—",
                    ComparacionEjercicio::cambio));

    private LocalDate lunes = SemanaService.lunesDe(LocalDate.now());

    PanelSemanas(SemanaService semanas, Exportador exportador) {
        super(new BorderLayout(0, 12));
        this.semanas = semanas;
        this.exportador = exportador;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton anterior = new JButton("← Anterior");
        anterior.addActionListener(e -> Ui.accion(this, () -> mostrar(lunes.minusWeeks(1))));
        siguiente.addActionListener(e -> Ui.accion(this, () -> mostrar(lunes.plusWeeks(1))));
        JButton hoy = new JButton("Esta semana");
        hoy.addActionListener(e -> Ui.accion(this, () -> mostrar(SemanaService.lunesDe(LocalDate.now()))));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(titulo, BorderLayout.CENTER);
        cabecera.add(Ui.fila(anterior, hoy, siguiente), BorderLayout.EAST);

        JPanel superior = new JPanel(new BorderLayout(0, 6));
        superior.setOpaque(false);
        superior.add(cabecera, BorderLayout.NORTH);
        superior.add(new JLabel("Mejor marca de cada ejercicio: peso · series x repeticiones"), BorderLayout.SOUTH);

        JTable tabla = Ui.tabla(modelo);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new RendererCambio());

        JButton exportar = Ui.botonPrincipal("Exportar al Bloc de notas (Ctrl+E)");
        exportar.addActionListener(e -> exportarSemanaMostrada());

        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.setOpaque(false);
        inferior.add(Ui.fila(exportar), BorderLayout.NORTH);
        inferior.add(new JLabel("Los ficheros se guardan en " + exportador.carpeta()), BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);

        Ui.accion(this, this::refrescar);
    }

    final void mostrar(LocalDate lunesSemana) {
        lunes = lunesSemana;
        refrescar();
    }

    final void refrescar() {
        titulo.setText("Semana del " + Formato.fecha(lunes) + " al " + Formato.fecha(lunes.plusDays(6)));
        siguiente.setEnabled(lunes.isBefore(SemanaService.lunesDe(LocalDate.now())));
        modelo.setFilas(semanas.comparar(lunes));
    }

    final void exportarSemanaMostrada() {
        Ui.accion(this, () -> {
            refrescar();
            if (modelo.getRowCount() == 0) {
                Ui.aviso(this, "No hay ejercicios en esta semana ni en la anterior.");
                return;
            }
            abrir(exportador.exportar(lunes));
        });
    }

    private void abrir(Path fichero) {
        try {
            Desktop.getDesktop().open(fichero.toFile());
        } catch (IOException | UnsupportedOperationException e) {
            Ui.aviso(this, "Exportado en " + fichero + ", pero no se ha podido abrir automáticamente.");
        }
    }

    /** Verde si mejoras, rojo si bajas. */
    private static final class RendererCambio extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            String texto = String.valueOf(valor);
            if (!seleccionada) {
                c.setForeground(texto.startsWith("+") || texto.equals("Nuevo") ? Ui.VERDE
                        : texto.startsWith("-") ? Ui.ROJO
                        : tabla.getForeground());
            }
            return c;
        }
    }
}
