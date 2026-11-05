package com.fit4u.datos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/** Datos de conexión a MySQL que el usuario decide recordar, guardados en su carpeta personal. */
public record AjustesConexion(String host, int puerto, String usuario, String contrasena) {

    private static final Path FICHERO = Path.of(System.getProperty("user.home"), ".fit4u", "conexion.properties");

    public String url() {
        return Conexion.urlMysql(host, puerto);
    }

    public static Optional<AjustesConexion> cargar() {
        if (!Files.exists(FICHERO)) {
            return Optional.empty();
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FICHERO)) {
            p.load(in);
            return Optional.of(new AjustesConexion(
                    p.getProperty("host", "localhost"),
                    Integer.parseInt(p.getProperty("puerto", "3306")),
                    p.getProperty("usuario", "root"),
                    p.getProperty("contrasena", "")));
        } catch (IOException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    public void guardar() throws IOException {
        Properties p = new Properties();
        p.setProperty("host", host);
        p.setProperty("puerto", String.valueOf(puerto));
        p.setProperty("usuario", usuario);
        p.setProperty("contrasena", contrasena);
        Files.createDirectories(FICHERO.getParent());
        try (OutputStream out = Files.newOutputStream(FICHERO)) {
            p.store(out, "Conexion de Fit4U a MySQL");
        }
    }

    public static void borrar() throws IOException {
        Files.deleteIfExists(FICHERO);
    }
}
