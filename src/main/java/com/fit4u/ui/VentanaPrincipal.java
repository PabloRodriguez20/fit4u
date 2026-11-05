package com.fit4u.ui;

import com.fit4u.servicio.CaloriasService;
import com.fit4u.servicio.EjercicioService;
import com.fit4u.servicio.Exportador;
import com.fit4u.servicio.Formato;
import com.fit4u.servicio.PesoService;
import com.fit4u.servicio.SemanaService;
import org.hibernate.SessionFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileSystemView;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.time.LocalDate;

public class VentanaPrincipal extends JFrame {

    private static final String ACCION_EXPORTAR = "exportar";

    private final JTabbedPane pestanas = new JTabbedPane();
    private final PanelSemanas semanas;
    private final Exportador exportador;

    private LocalDate dia = LocalDate.now();

    public VentanaPrincipal(SessionFactory sf) {
        super("Fit4U");

        EjercicioService ejercicios = new EjercicioService(sf);
        SemanaService servicioSemanas = new SemanaService(ejercicios);
        Path carpeta = FileSystemView.getFileSystemView().getDefaultDirectory().toPath().resolve("Fit4U");
        exportador = new Exportador(carpeta, servicioSemanas, ejercicios);

        PanelCalorias calorias = new PanelCalorias(new CaloriasService(sf));
        PanelEntrenos entrenos = new PanelEntrenos(ejercicios);
        PanelPeso peso = new PanelPeso(new PesoService(sf));
        semanas = new PanelSemanas(servicioSemanas, exportador);

        pestanas.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        pestanas.addTab("Calorías", calorias);
        pestanas.addTab("Entrenos", entrenos);
        pestanas.addTab("Semanas", semanas);
        pestanas.addTab("Kg perdidos", peso);
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() == semanas) {
                Ui.accion(this, semanas::refrescar);
            }
        });
        setContentPane(pestanas);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), ACCION_EXPORTAR);
        getRootPane().getActionMap().put(ACCION_EXPORTAR, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                semanas.exportarSemanaMostrada();
            }
        });

        setMinimumSize(new Dimension(820, 560));
        setSize(940, 640);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                comprobarSemanaTerminada();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // Si la app se queda abierta de un día para otro, el contador empieza de cero.
                LocalDate hoy = LocalDate.now();
                if (!hoy.equals(dia)) {
                    dia = hoy;
                    calorias.refrescar();
                    entrenos.ponerFechaDeHoy();
                    peso.ponerFechaDeHoy();
                    Ui.accion(VentanaPrincipal.this, () -> semanas.mostrar(SemanaService.lunesDe(hoy)));
                    comprobarSemanaTerminada();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                sf.close();
                System.exit(0);
            }
        });
    }

    /** Al empezar una semana nueva, exporta la que acaba de terminar y ofrece compararla. */
    private void comprobarSemanaTerminada() {
        Ui.accion(this, () -> exportador.exportarSemanaTerminadaSiFalta(LocalDate.now()).ifPresent(lunes -> {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "La semana del " + Formato.fecha(lunes) + " ha terminado y se ha guardado en:\n"
                            + exportador.fichero(lunes) + "\n\n¿Quieres ver la comparación?",
                    "Semana exportada", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (respuesta == JOptionPane.YES_OPTION) {
                semanas.mostrar(lunes);
                pestanas.setSelectedComponent(semanas);
            }
        }));
    }
}
