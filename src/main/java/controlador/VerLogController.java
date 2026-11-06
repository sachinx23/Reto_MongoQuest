package controlador;

import exception.ErrorMessages;
import exception.ShowAlert;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import log.LogApp;

/**
 * El administrador ve el fichero de log (telonda.log) desde la aplicación.
 */
public class VerLogController {

    private static final Logger LOGGER = Logger.getLogger(VerLogController.class.getName());

    @FXML private TextArea txtLog;

    @FXML
    public void initialize() {
        cargarLog();
    }

    private void cargarLog() {
        try {
            txtLog.setText(LogApp.leerLog());
            // Se baja hasta el final para ver lo último
            txtLog.positionCaret(txtLog.getLength());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error leyendo el fichero de log", e);
            ShowAlert.error(ErrorMessages.READ_LOG);
        }
    }

    @FXML
    public void btnRefrescar(ActionEvent event) {
        cargarLog();
    }
}
