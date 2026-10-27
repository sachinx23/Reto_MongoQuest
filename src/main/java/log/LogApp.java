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
 * Fichero de log (telonda.log) donde se apuntan todas las operaciones con la base de datos.
 */
public class LogApp {

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

    public static void info(String mensaje) {
        LOGGER.info(mensaje);
    }

    public static void error(String mensaje, Exception e) {
        LOGGER.log(Level.SEVERE, mensaje, e);
    }

    public static String leerLog() throws IOException {
        Path ruta = Paths.get(FICHERO_LOG);
        if (!Files.exists(ruta)) {
            return "";
        }
        return new String(Files.readAllBytes(ruta), StandardCharsets.UTF_8);
    }
}
