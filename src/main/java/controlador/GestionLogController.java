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
import javafx.scene.control.ListView;

/**
 * Controlador simplificado para mostrar el historial de logs en una lista sencilla.
 * 
 * @author Eneko.Plaza
 */
public class GestionLogController implements Initializable {

    /** Componente de lista simple en JavaFX. */
    @FXML
    private ListView<String> verListaLogs;

    /** Lista de texto observable para la interfaz. */
    private ObservableList<String> lineasLog = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vinculamos la lista de texto al ListView
        verListaLogs.setItems(lineasLog);
        
        // Cargamos los registros directamente
        cargarLogs();
    }

    /**
     * Lee el archivo app.log y añade cada línea como texto a la lista.
     */
    private void cargarLogs() {
        lineasLog.clear();

        
        File archivo = new File("log/app.log");
        if (!archivo.exists()) {
            archivo = new File("src/main/java/log/app.log");
        }

        if (!archivo.exists()) {
            lineasLog.add("No se encontró el archivo de log.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lineasLog.add(linea);
                }
            }
        } catch (IOException e) {
            lineasLog.add("Error al leer el archivo de registros: " + e.getMessage());
        }
    }
}