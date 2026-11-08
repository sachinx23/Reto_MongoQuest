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

public class LogApp {

    // Usuario con sesión iniciada
    private static String usuario;

    // Mismo paquete log, se ve en NetBeans
    private static final Path CARPETA = Paths.get("src", "main", "java", "log");

    private static class Formato extends Formatter {

        @Override
        public String format(LogRecord r) {
            StringBuilder sb = new StringBuilder();
            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(r.getMillis())))
                    .append(" [").append(r.getLevel().getName()).append("] ")
                    .append("[").append(usuario == null ? "sin sesión" : usuario).append("] ")
                    .append(r.getSourceClassName()).append(".").append(r.getSourceMethodName()).append(": ")
                    .append(formatMessage(r))
                    .append(System.lineSeparator());
            // Traza solo si hay error
            if (r.getThrown() != null) {
                StringWriter sw = new StringWriter();
                r.getThrown().printStackTrace(new PrintWriter(sw));
                sb.append(sw);
            }
            return sb.toString();
        }
    }

    public static void setUsuario(String nuevo) {
        usuario = nuevo;
    }

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
