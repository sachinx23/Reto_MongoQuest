package controlador;

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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controlador de Administrador basado en los casos de uso del diagrama BPMN.
 * @author eneko
 */
public class AdministradorController implements Initializable {

    @FXML private ComboBox<String> cbOpcionesAdmin;
    @FXML private StackPane contenidoDinamico;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Rellenar el ComboBox con las opciones del diagrama BPMN
        cbOpcionesAdmin.setItems(FXCollections.observableArrayList(
            "Crear Empleado",
            "Ascender Empleado a Admin",
            "Eliminar Cuenta Empleado",
            "Crear / Gestionar Sucursal",
            "Gestionar Tarifas / Plantilla",
            "Cerrar Sesión"
        ));

        // 2. Seleccionar por defecto la vista de sucursales e inyectarla en el StackPane
        cbOpcionesAdmin.getSelectionModel().select("Crear / Gestionar Sucursal");
        cargarSubvista("/vista/Sucursal.fxml");
    }

    @FXML
    public void cambiarSubVista(ActionEvent event) {
        String seleccion = cbOpcionesAdmin.getValue();
        if (seleccion == null) return;

        switch (seleccion) {
            case "Crear Empleado":
                cargarSubvista("/vista/AltaEmpleado.fxml");
                break;
            case "Ascender Empleado a Admin":
                cargarSubvista("/vista/AscenderEmpleado.fxml");
                break;
            case "Eliminar Cuenta Empleado":
                cargarSubvista("/vista/EliminarEmpleado.fxml");
                break;
            case "Crear / Gestionar Sucursal":
                cargarSubvista("/vista/Sucursal.fxml");
                break;
            case "Gestionar Tarifas / Plantilla":
                cargarSubvista("/vista/GestionTarifas.fxml");
                break;
            case "Cerrar Sesión":
                cerrarSesion(event);
                break;
        }
    }

    private void cerrarSesion(ActionEvent event) {
        try {
            URL url = getClass().getResource("/vista/IniciarSesion.fxml");
            Parent loginView = FXMLLoader.load(url);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al volver a la pantalla de login: " + e.getMessage());
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
                System.err.println("No se encontró el FXML en la ruta: " + rutaFXML);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar la subvista: " + e.getMessage());
            e.printStackTrace();
        }
    }
}