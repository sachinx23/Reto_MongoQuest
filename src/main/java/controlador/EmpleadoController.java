package controlador;

import log.LogApp;
import exception.ShowAlert;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controlador de Empleado (subconjunto de las funciones del Administrador).
 *
 * @author eneko
 */
public class EmpleadoController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoController.class.getName());

    @FXML
    private ComboBox<String> cbOpcionesEmpleado;
    @FXML
    private StackPane contenidoDinamico;
    @FXML
    private Label lblNombreEmpleado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbOpcionesEmpleado.setItems(FXCollections.observableArrayList(
                "Crear Cliente",
                "Gestionar Contratos",
                "Tickets de Soporte",
                "Ver Sucursales",
                "Ver Valoraciones",
                "Cerrar Sesión"
        ));

        cbOpcionesEmpleado.getSelectionModel().select("Gestionar Contratos");
        cargarSubvista("/vista/GestionarContrato.fxml");
    }

    @FXML
    public void cambiarSubVista(ActionEvent event) {
        String seleccion = cbOpcionesEmpleado.getValue();
        if (seleccion == null) {
            return;
        }

        switch (seleccion) {
            case "Crear Cliente":
                cargarSubvista("/vista/Registrarse.fxml");
                break;
            case "Gestionar Contratos":
                cargarSubvista("/vista/GestionarContrato.fxml");
                break;
            case "Tickets de Soporte":
                cargarSubvista("/vista/GestionarTickets.fxml");
                break;
            case "Ver Sucursales":
                cargarSubvista("/vista/Sucursal.fxml");
                break;
            case "Ver Valoraciones":
                cargarSubvista("/vista/GestionarValoraciones.fxml");
                break;
            case "Cerrar Sesión":
                cerrarSesion(event);
                break;
        }
    }

    public void setNombreUsuario(String nombre) {
        if (lblNombreEmpleado != null && nombre != null) {
            lblNombreEmpleado.setText("Empleado: " + nombre);
        }
    }

    private void cerrarSesion(ActionEvent event) {
        try {
            LOGGER.info("Sesión cerrada");
            LogApp.setUsuario(null);
            URL url = getClass().getResource("/vista/IniciarSesion.fxml");
            Parent loginView = FXMLLoader.load(url);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver al login", e);
            ShowAlert.error("No se pudo volver a la pantalla de inicio de sesión.");
        }
    }

    private void cargarSubvista(String rutaFXML) {
        try {
            URL url = getClass().getResource(rutaFXML);
            if (url != null) {
                Parent vista = FXMLLoader.load(url);
                contenidoDinamico.getChildren().clear();
                contenidoDinamico.getChildren().add(vista);
            } else {
                LOGGER.severe("No existe la pantalla " + rutaFXML);
                ShowAlert.error("No se encontró la pantalla " + rutaFXML);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir " + rutaFXML, e);
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }
}
