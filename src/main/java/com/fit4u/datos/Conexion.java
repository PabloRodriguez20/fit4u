package com.fit4u.datos;

import com.fit4u.modelo.Biceps;
import com.fit4u.modelo.Comida;
import com.fit4u.modelo.Espalda;
import com.fit4u.modelo.Hombro;
import com.fit4u.modelo.Pecho;
import com.fit4u.modelo.Perfil;
import com.fit4u.modelo.Pierna;
import com.fit4u.modelo.RegistroPeso;
import com.fit4u.modelo.Triceps;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/** Crea la conexión de Hibernate. La base de datos y sus tablas se crean solas si no existen. */
public final class Conexion {

    private Conexion() {
    }

    public static String urlMysql(String host, int puerto) {
        return "jdbc:mysql://" + host + ":" + puerto + "/fit4u"
                + "?createDatabaseIfNotExist=true"
                + "&allowPublicKeyRetrieval=true"
                + "&connectionTimeZone=LOCAL"
                + "&characterEncoding=UTF-8";
    }

    public static SessionFactory abrir(String url, String usuario, String contrasena) {
        Configuration cfg = new Configuration()
                .addAnnotatedClass(Perfil.class)
                .addAnnotatedClass(Comida.class)
                .addAnnotatedClass(RegistroPeso.class)
                .addAnnotatedClass(Hombro.class)
                .addAnnotatedClass(Pecho.class)
                .addAnnotatedClass(Triceps.class)
                .addAnnotatedClass(Espalda.class)
                .addAnnotatedClass(Biceps.class)
                .addAnnotatedClass(Pierna.class);

        cfg.setProperty("hibernate.connection.url", url);
        cfg.setProperty("hibernate.connection.username", usuario);
        cfg.setProperty("hibernate.connection.password", contrasena);
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");
        cfg.setProperty("hibernate.hikari.maximumPoolSize", "3");
        cfg.setProperty("hibernate.hikari.connectionTimeout", "5000");

        return cfg.buildSessionFactory();
    }
}
