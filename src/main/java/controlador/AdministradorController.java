package controlador;

import com.mycompany.reto_1.Reto_1;
import exception.ShowAlert;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Panel de administrador: cada opción del desplegable carga su pantalla dentro de panelContenido.
 */
public class AdministradorController {

    private static final Logger LOGGER = Logger.getLogger(AdministradorController.class.getName());

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<OpcionAdmin> cmbOpciones;
    @FXML private StackPane panelContenido;

    @FXML
    public void initialize() {
        lblBienvenida.setText("Admin: " + Sesion.getUsuario().getNombre());
        cmbOpciones.setItems(FXCollections.observableArrayList(OpcionAdmin.values()));
        // Al entrar se muestra el perfil; el desplegable siempre indica en qué apartado estás
        cmbOpciones.setValue(OpcionAdmin.PERFIL);
        mostrar(OpcionAdmin.PERFIL);
    }

    private void mostrar(OpcionAdmin opcion) {
        try {
            panelContenido.getChildren().setAll(Reto_1.loadFXML(opcion.getVista()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir la pantalla " + opcion.getVista(), e);
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }

    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        if (cmbOpciones.getValue() != null) {
            mostrar(cmbOpciones.getValue());
        }
    }

    @FXML
    public void btnMiPerfil(ActionEvent event) {
        cmbOpciones.setValue(OpcionAdmin.PERFIL);
        mostrar(OpcionAdmin.PERFIL);
    }

    @FXML
    public void btnCerrarSesion(ActionEvent event) {
        if (ShowAlert.confirmar("Salir", "¿Deseas salir del panel de administrador?")) {
            Sesion.cerrar();
            Reto_1.setRoot("IniciarSesion");
        }
    }

    // Cierra la aplicación entera después de confirmar
    @FXML
    public void menuSalir(ActionEvent event) {
        if (ShowAlert.confirmar("Salir", "¿Deseas cerrar la aplicación?")) {
            LOGGER.info("Aplicación cerrada desde el menú");
            Platform.exit();
        }
    }

    @FXML
    public void menuAcercaDe(ActionEvent event) {
        ShowAlert.info("Acerca de", "Tel-ONDA 1.0\nReto MongoQuest\nSachin, Iñigo y Eneko Plaza");
    }
}
