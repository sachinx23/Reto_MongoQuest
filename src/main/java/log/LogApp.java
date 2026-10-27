package log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Bitácora (log) de la aplicación. Todas las operaciones contra la base de
 * datos se registran en el fichero {@link #FICHERO_LOG}, que el administrador
 * puede consultar desde la aplicación.
 */
public class LogApp {

    /** Ruta del fichero de log (se crea en la carpeta desde la que se ejecuta la app). */
    public static final String FICHERO_LOG = "telonda.log";

    private static final Logger LOGGER = Logger.getLogger("TelOnda");

    static {
        try {
            // true = añadir al final del fichero en vez de sobrescribirlo
            FileHandler fichero = new FileHandler(FICHERO_LOG, true);
            fichero.setFormatter(new SimpleFormatter());
            fichero.setEncoding("UTF-8");
            LOGGER.addHandler(fichero);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("No se pudo crear el fichero de log: " + e.getMessage());
        }
    }

    /**
     * Registra una operación que ha terminado correctamente.
     *
     * @param mensaje descripción de la operación
     */
    public static void info(String mensaje) {
        LOGGER.info(mensaje);
    }

    /**
     * Registra un error junto con la excepción que lo ha provocado.
     *
     * @param mensaje descripción del error
     * @param e excepción original
     */
    public static void error(String mensaje, Exception e) {
        LOGGER.log(Level.SEVERE, mensaje, e);
    }

    /**
     * Devuelve todo el contenido del fichero de log.
     *
     * @return texto del log, o cadena vacía si todavía no existe
     * @throws IOException si el fichero no se puede leer
     */
    public static String leerLog() throws IOException {
        Path ruta = Paths.get(FICHERO_LOG);
        if (!Files.exists(ruta)) {
            return "";
        }
        return new String(Files.readAllBytes(ruta), StandardCharsets.UTF_8);
    }
}
