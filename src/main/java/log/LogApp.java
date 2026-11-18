package log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Clase de utilidad encargada de configurar y gestionar el sistema global de registro
 * de eventos (logging) de la aplicación mediante {@link java.util.logging}.
 * Centraliza la escritura de trazas en el archivo {@code app.log}, incluyendo marca de
 * tiempo, nivel de severidad, usuario con sesión activa, clase y método de origen, así
 * como la traza completa de pila ({@code stack trace}) en caso de excepciones.
 *
 * @author Eneko.Plaza
 */
public class LogApp {

    /**
     * Identificador y rol del usuario autenticado actualmente en la sesión,
     * utilizado para contextualizar cada entrada del registro de eventos.
     */
    private static String usuario;

    /**
     * Ruta del directorio donde se crea y almacena el fichero de registro {@code app.log}.
     */
    private static final Path CARPETA = Paths.get("src", "main", "java", "log");

    /**
     * Formateador personalizado de registros de log que estructura cada línea con el patrón:
     * {@code yyyy-MM-dd HH:mm:ss [NIVEL] [usuario] Clase.metodo: mensaje}
     * seguido de la traza de excepción si existiera.
     */
    private static class Formato extends Formatter {

        /**
         * Da formato legible al registro de evento proporcionado por {@link Logger}.
         *
         * @param r Registro {@link LogRecord} con la información del evento a escribir.
         * @return Cadena formateada lista para volcarse en el archivo de log.
         */
        @Override
        public String format(LogRecord r) {
            StringBuilder sb = new StringBuilder();
            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(r.getMillis())))
                    .append(" [").append(r.getLevel().getName()).append("] ")
                    .append("[").append(usuario == null ? "sin sesión" : usuario).append("] ")
                    .append(r.getSourceClassName()).append(".").append(r.getSourceMethodName()).append(": ")
                    .append(formatMessage(r))
                    .append(System.lineSeparator());
            if (r.getThrown() != null) {
                StringWriter sw = new StringWriter();
                r.getThrown().printStackTrace(new PrintWriter(sw));
                sb.append(sw);
            }
            return sb.toString();
        }
    }

    /**
     * Establece o actualiza el identificador del usuario activo que aparecerá en las
     * siguientes líneas registradas en el archivo de log. Debe pasarse {@code null}
     * al cerrar sesión.
     *
     * @param nuevo Nombre y rol del usuario autenticado, o {@code null} si no hay sesión activa.
     */
    public static void setUsuario(String nuevo) {
        usuario = nuevo;
    }

    /**
     * Inicializa el manejador de archivos ({@link FileHandler}) sobre el {@link Logger} raíz
     * de la aplicación. Crea el directorio destino si no existe, configura la codificación
     * {@code UTF-8} en modo adición ({@code append = true}) y aplica {@link Formato}.
     */
    public static void configurar() {
        try {
            Files.createDirectories(CARPETA);
            String base = CARPETA.toString().replace('\\', '/');
            FileHandler fichero = new FileHandler(base + "/app.log", true);
            fichero.setEncoding("UTF-8");
            fichero.setFormatter(new Formato());
            Logger.getLogger("").addHandler(fichero);
        } catch (IOException e) {
            System.err.println("No se pudo crear el archivo de log: " + e.getMessage());
        }
    }
}