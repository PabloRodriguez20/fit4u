package com.fit4u;

import com.fit4u.servicio.CaloriasService;
import com.fit4u.servicio.PesoService;
import com.fit4u.ui.DialogoBienvenida;
import com.fit4u.ui.DialogoConexion;
import com.fit4u.ui.VentanaPrincipal;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.hibernate.SessionFactory;

import javax.swing.SwingUtilities;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {

    // Se guarda en un campo para que el nivel de log no se pierda con el recolector de basura.
    private static final Logger LOG_HIBERNATE = Logger.getLogger("org.hibernate");

    private Main() {
    }

    public static void main(String[] args) {
        LOG_HIBERNATE.setLevel(Level.SEVERE);
        SwingUtilities.invokeLater(Main::arrancar);
    }

    private static void arrancar() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#E4572E"));
        FlatLightLaf.setup();

        SessionFactory sf = DialogoConexion.conectar();
        if (sf == null) {
            System.exit(0);
        }

        CaloriasService calorias = new CaloriasService(sf);
        if (calorias.objetivo().isEmpty()
                && !DialogoBienvenida.preguntar(calorias, new PesoService(sf))) {
            sf.close();
            System.exit(0);
        }

        new VentanaPrincipal(sf).setVisible(true);
    }
}
