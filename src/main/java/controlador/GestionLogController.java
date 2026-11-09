package controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Log;

/**
 * Controlador simplificado de la vista GestionLogs.fxml para mostrar
 * la lista de registros del archivo de log en pantalla[cite: 54, 59].
 * 
 * @author Eneko.Plaza
 */
public class GestionLogController implements Initializable {

    /** Tabla JavaFX para mostrar las líneas del log parseadas[cite: 84]. */
    @FXML
    private TableView<Log> tablaLogs;

    /** Columna para la fecha y hora del evento[cite: 86]. */
    @FXML
    private TableColumn<Log, String> colFecha;

    /** Columna para el nivel del log[cite: 86]. */
    @FXML
    private TableColumn<Log, String> colNivel;

    /** Columna para el usuario y rol asociado[cite: 86]. */
    @FXML
    private TableColumn<Log, String> colUsuario;

    /** Columna para la clase o método de origen[cite: 86]. */
    @FXML
    private TableColumn<Log, String> colOrigen;

    /** Columna para el mensaje del log[cite: 86]. */
    @FXML
    private TableColumn<Log, String> colMensaje;

    /** Lista observable con los logs leídos del archivo[cite: 63, 88]. */
    private ObservableList<Log> listaLogs = FXCollections.observableArrayList();

    /**
     * Constructor por defecto.
     */
    public GestionLogController() {
    }

    /**
     * Inicializa la tabla y carga los registros directamente al abrir la pantalla[cite: 59].
     * 
     * @param url Ubicación relativa del FXML o null[cite: 49].
     * @param rb Recursos del sistema o null[cite: 49].
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colFecha.setCellValueFactory(new PropertyValueFactory<Log, String>("fecha"));
        colNivel.setCellValueFactory(new PropertyValueFactory<Log, String>("nivel"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<Log, String>("usuario"));
        colOrigen.setCellValueFactory(new PropertyValueFactory<Log, String>("origen"));
        colMensaje.setCellValueFactory(new PropertyValueFactory<Log, String>("mensaje"));

        tablaLogs.setItems(listaLogs);
        cargarLogs();
    }

    /**
     * Lee el archivo app.log y añade cada registro a la lista de la tabla[cite: 50].
     */
    private void cargarLogs() {
        listaLogs.clear();
        File archivo = new File("src/main/java/log/app.log");

        if (!archivo.exists()) {
            archivo = new File("log/app.log");
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = reader.readLine()) != null) {
                Log log = convertirTextoALog(linea);
                if (log != null) {
                    listaLogs.add(log);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   /**
     * Convierte una línea de texto del archivo de log en un objeto Log estructurado[cite: 49].
     * 
     * @param linea Texto completo leído del archivo.
     * @return Objeto Log estructurado o null si no coincide el formato.
     */
    private Log convertirTextoALog(String linea) {
        try {
            if (linea == null || !linea.contains("[")) {
                return null;
            }

            String fecha = linea.substring(0, 19);

            String contenido = linea.substring(19).replace("[", "").replace("]", "");
            String[] partes = contenido.trim().split(" ", 4);

            String nivel = partes[0];
            String usuario = partes[1];
            String origen = partes[2];
            
            String mensaje = "";
            if (partes.length > 3) {
                mensaje = partes[3];
            }

            return new Log(fecha, nivel, usuario, origen, mensaje);
        } catch (Exception e) {
            return null;
        }
    }
}