package controlador;

import exception.ErrorMessages;
import exception.ShowAlert;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import log.LogApp;
import modelo.Administrador;

/**
 * Panel de administrador. Cada opción del menú carga su pantalla dentro de
 * panelContenido.
 *
 * @author Eneko.Plaza
 */
public class AdministradorController implements Initializable {

    private static Administrador usuarioActual;

    public static void setUsuario(Administrador a) {
        usuarioActual = a;
    }

    public static Administrador getUsuario() {
        return usuarioActual;
    }

    @FXML
    private AnchorPane panelContenido;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Al entrar se muestra el perfil
        cargarVista("/vista/PerfilAdmin.fxml");
    }

    private void cargarVista(String ruta) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(ruta));
            panelContenido.getChildren().setAll(vista);
        } catch (IOException e) {
            LogApp.error("Error cargando la vista " + ruta, e);
            ShowAlert.showAlert("Error", "No se pudo abrir la pantalla.", Alert.AlertType.ERROR);
        }
    }

    // Cargar la vista Mi Perfil
    @FXML
    public void miPerfil(ActionEvent event) {
        cargarVista("/vista/PerfilAdmin.fxml");
    }

    // Cargar la vista de Alta de Empleado
    @FXML
    public void altaEmpleado(ActionEvent event) {
        cargarVista("/vista/AltaEmpleado.fxml");
    }

    //Cargar Vista de Cambiar Puesto
    @FXML
    public void cambiarPuesto(ActionEvent event) {
        cargarVista("/vista/CambiarPuesto.fxml");
    }

    //Cambiar vista Borrar Empleado
    @FXML
    public void borrarEmpleado(ActionEvent event) {
        cargarVista("/vista/BorrarEmpleado.fxml");
    }

    // Cambiar vista gestion sucursal
    @FXML
    public void gestionSucursal(ActionEvent event) {
        cargarVista("/vista/GestionSucursal.fxml");
    }

    // Muestra el fichero de log de la aplicación en el panel central
    @FXML
    public void verLog(ActionEvent event) {
        try {
            TextArea areaLog = new TextArea(LogApp.leerLog());
            areaLog.setEditable(false);
            areaLog.setWrapText(true);
            areaLog.setPrefSize(panelContenido.getPrefWidth(), panelContenido.getPrefHeight());
            panelContenido.getChildren().setAll(areaLog);
        } catch (IOException e) {
            ShowAlert.showAlert("Error", ErrorMessages.READ_LOG, Alert.AlertType.ERROR);
        }
    }

    // metodo cerrar sesion.
    @FXML
    public void cerrarSesion(ActionEvent event) {
        if (!ShowAlert.confirmar("Salir", "¿Deseas salir del panel de administrador?")) {
            return;
        }
        usuarioActual = null;
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
            Stage stage = (Stage) panelContenido.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
