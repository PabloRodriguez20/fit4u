package com.fit4u.ui;

import com.fit4u.datos.AjustesConexion;
import com.fit4u.datos.Conexion;
import org.hibernate.SessionFactory;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Optional;

/** Conecta con MySQL: usa los datos recordados y, si no hay o fallan, se los pide al usuario. */
public final class DialogoConexion {

    private DialogoConexion() {
    }

    /** Devuelve la conexión abierta, o null si el usuario cancela. */
    public static SessionFactory conectar() {
        Optional<AjustesConexion> guardados = AjustesConexion.cargar();
        String error = null;
        if (guardados.isPresent()) {
            try {
                AjustesConexion a = guardados.get();
                return Conexion.abrir(a.url(), a.usuario(), a.contrasena());
            } catch (RuntimeException e) {
                error = Ui.causa(e);
            }
        }

        JTextField host = new JTextField(guardados.map(AjustesConexion::host).orElse("localhost"));
        JTextField puerto = new JTextField(guardados.map(a -> String.valueOf(a.puerto())).orElse("3306"));
        JTextField usuario = new JTextField(guardados.map(AjustesConexion::usuario).orElse("root"));
        JPasswordField contrasena = new JPasswordField(20);
        JCheckBox recordar = new JCheckBox("Recordar en este ordenador (sin cifrar)");

        JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 6));
        formulario.add(Ui.texto("Conexión a MySQL", "h3"));
        formulario.add(Ui.campo("Servidor", host));
        formulario.add(Ui.campo("Puerto", puerto));
        formulario.add(Ui.campo("Usuario", usuario));
        formulario.add(Ui.campo("Contraseña", contrasena));
        formulario.add(recordar);
        JLabel mensaje = new JLabel();
        mensaje.setForeground(Ui.ROJO);
        formulario.add(mensaje);

        while (true) {
            mensaje.setText(error == null ? " " : "<html><body style='width:260px'>No se pudo conectar: "
                    + escapar(error) + "</body></html>");
            int respuesta = JOptionPane.showConfirmDialog(null, formulario, "Fit4U",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (respuesta != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                AjustesConexion a = new AjustesConexion(host.getText().trim(),
                        Ui.leerEntero(puerto.getText(), "Puerto"),
                        usuario.getText().trim(),
                        new String(contrasena.getPassword()));
                SessionFactory sf = Conexion.abrir(a.url(), a.usuario(), a.contrasena());
                actualizarRecordados(a, recordar.isSelected());
                return sf;
            } catch (RuntimeException e) {
                error = Ui.causa(e);
            }
        }
    }

    private static void actualizarRecordados(AjustesConexion a, boolean recordar) {
        try {
            if (recordar) {
                a.guardar();
            } else {
                AjustesConexion.borrar();
            }
        } catch (IOException e) {
            Ui.aviso(null, "Conectado, pero no se pudieron guardar los datos de conexión: " + e.getMessage());
        }
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
