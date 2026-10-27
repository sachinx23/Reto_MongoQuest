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
import javafx.scene.control.Label;

/**
 * Controlador de Administrador adaptado para DAM.
 * @author eneko
 */
public class AdministradorController implements Initializable {

    @FXML
    private ComboBox<String> cbOpcionesAdmin;
    @FXML
    private StackPane contenidoDinamico;
    @FXML 
    private Label lblNombreAdmin;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Opciones del ComboBox
        cbOpcionesAdmin.setItems(FXCollections.observableArrayList(
                "Gestionar Plantilla",
                "Gestionar Sucursal",
                "Gestionar Tarifas",
                "Cerrar Sesión"
        ));
        
        // CORREGIDO: "Gestionar Plantilla" con P mayúscula
        cbOpcionesAdmin.getSelectionModel().select("Gestionar Plantilla");
        cargarSubvista("/vista/GestionarPlantilla.fxml");
    }

    @FXML
    public void cambiarSubVista(ActionEvent event) {
        String seleccion = cbOpcionesAdmin.getValue();
        if (seleccion == null) {
            return;
        }

        switch (seleccion) {
            case "Gestionar Plantilla":
                cargarSubvista("/vista/GestionarPlantilla.fxml");
                break;
            case "Gestionar Sucursal":
                cargarSubvista("/vista/Sucursal.fxml");
                break;
            case "Gestionar Tarifas":
                // Apunta exactamente a tu archivo GestionarTarifas.fxml
                cargarSubvista("/vista/GestionarTarifas.fxml");
                break;
            case "Cerrar Sesión":
                cerrarSesion(event);
                break;
        }
    }

    @FXML
    public void setNombreUsuario(String nombre) {
        if (lblNombreAdmin != null && nombre != null) {
            lblNombreAdmin.setText("Admin: " + nombre);
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