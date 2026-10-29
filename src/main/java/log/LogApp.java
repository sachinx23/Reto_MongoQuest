package log;

import controlador.Sesion;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Configura el log: lo que cada clase apunta con su LOGGER se guarda también en el fichero telonda.log.
 * Cada línea lleva: fecha | usuario | tipo (INFO, WARNING, SEVERE) | clase | operación | error (si lo hay)
 */
public class LogApp extends Formatter {

    public static final String FICHERO_LOG = "telonda.log";

    // Paquetes de la aplicación: solo sus mensajes van al fichero (los avisos internos de JavaFX no)
    private static final Logger[] PAQUETES = {
        Logger.getLogger("com.mycompany.reto_1"),
        Logger.getLogger("controlador"),
        Logger.getLogger("dao")
    };

    // Se llama una vez al arrancar la aplicación
    public static void configurar() {
        try {
            // true = se añade al final del fichero en vez de borrar lo que había
            FileHandler fichero = new FileHandler(FICHERO_LOG, true);
            fichero.setEncoding("UTF-8");
            fichero.setFormatter(new LogApp());
            // El LOGGER de cada clase pasa sus mensajes al de su paquete (dao.DAOImplementacion -> dao)
            for (Logger paquete : PAQUETES) {
                paquete.addHandler(fichero);
            }
        } catch (IOException e) {
            System.out.println("No se pudo abrir el fichero de log: " + e.getMessage());
        }
    }

    // Forma que tiene cada línea del fichero
    @Override
    public String format(LogRecord registro) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String usuario = "sin sesión";
        if (Sesion.getUsuario() != null) {
            usuario = Sesion.getUsuario().getNombreUsuario();
        }
        String linea = fecha + " | " + usuario + " | " + registro.getLevel() + " | " + registro.getLoggerName()
                + " | " + registro.getMessage();
        if (registro.getThrown() != null) {
            linea = linea + " | ERROR: " + registro.getThrown();
        }
        return linea + "\n";
    }
}
