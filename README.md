# Fit4U

Aplicación de escritorio en Java (Swing) para llevar las calorías del día, los ejercicios
y pesos del gimnasio y el peso corporal. Guarda los datos en MySQL mediante Hibernate.

## Cómo ejecutarla

**Desde IntelliJ IDEA:** abre esta carpeta y ejecuta `com.fit4u.Main` (JDK 17 o superior).

**Con el JAR:** genéralo con la opción `package` de Maven y lánzalo así:

```bash
java -jar target/fit4u.jar
```

MySQL tiene que estar arrancado. La primera vez la app pide tu usuario y contraseña de MySQL;
la base de datos `fit4u` y sus tablas se crean solas.

## Semanas y exportación

Al elegir un ejercicio ya hecho, el formulario se rellena con su último peso, series y repeticiones.

La pestaña **Semanas** compara la mejor marca de cada ejercicio con la de la semana anterior
(las semanas van de lunes a domingo). Con el botón de exportar o `Ctrl+E` se guarda la semana
que estás viendo en `Documentos\Fit4U\semana_AAAA-MM-DD.txt` y se abre el fichero.

La exportación automática se hace al abrir la app después de terminar una semana.

## Tablas

| Tabla | Contenido |
| --- | --- |
| `hombro`, `pecho`, `triceps`, `espalda`, `biceps`, `pierna` | Ejercicios: fecha, ejercicio, peso, series y repeticiones |
| `comida` | Comidas y sus kcal por día |
| `peso_corporal` | Tu peso por fecha (uno por día) |
| `perfil` | Tus kcal diarias |

Si marcas "Recordar", la conexión se guarda sin cifrar en `~/.fit4u/conexion.properties`.
Borra ese fichero para que vuelva a pedírtela.
