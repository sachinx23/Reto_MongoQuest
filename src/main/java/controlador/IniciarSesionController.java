package controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.bson.Document;

/**
 * FXML Controller class
 *
 * @author Eneko.Plaza
 */
public class IniciarSesionController implements Initializable {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensajeError;

    @FXML
    private Button txtIniciarSesion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (lblMensajeError != null) {
            lblMensajeError.setText("");
        }
    }

    @FXML
    public void btnIniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            lblMensajeError.setText("Rellena todos los campos.");
            return;
        }

        lblMensajeError.setText("");

        // Llamada al método estático del DAO comprobar login
        Document usuarioObtenido = DAO.comprobarUsuarios(usuario, contrasena);

        if (usuarioObtenido != null) {
            String nombre = usuarioObtenido.getString("nombre");
            if (nombre == null) {
                nombre = usuarioObtenido.getString("nombreUsuario");
            }

            String rol = usuarioObtenido.getString("rol");

            // Alerta de bienvenida
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Inicio de Sesión Correcto");
            alert.setHeaderText(null);
            alert.setContentText("¡Bienvenido/a " + nombre + "!\nRol: " + rol);
            alert.showAndWait();
            if ("Admin".equals(rol)) {
                abrirPanelAdmin(rol);
            } else if ("Empleado".equals(rol)) {
                 abrirPanelEmpleado("/vista/Empleado.fxml");
            } else if ("Cliente".equals(rol)) {
                // cambiarPantalla("/vista/Cliente.fxml");
            }

        } else {
            lblMensajeError.setText("Usuario o contraseña incorrectos.");
        }
    }
    
    //metodo cambiar pantalla
    @FXML
    public void cambiarPantalla(String ruta) {
    try {
       Parent root = FXMLLoader.load(getClass().getResource(ruta));
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
    } catch (IOException e) {
        e.printStackTrace();
        lblMensajeError.setText("Error al cargar la pantalla.");
    }
}
    private void abrirPanelAdmin(String nombreUsuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Admin.fxml"));
            Parent root = loader.load();

            // Obtenemos la instancia del AdministradorController e inyectamos el nombre
            AdministradorController adminController = loader.getController();
           adminController.setNombreUsuario(nombreUsuario);

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblMensajeError.setText("Error al cargar la pantalla de administración.");
        }
    }
    @FXML
    public void abrirPanelEmpleado(String nombreUsuario){
        try {
            FXMLLoader loader = new FXMLLoader (getClass().getResource("/vista/Empleado.fxml"));
            Parent root = loader.load();
            
            EmpleadoController empleController = loader.getController();
            empleController.setNombreUsuario(nombreUsuario);
            
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            lblMensajeError.setText("Error al cargar la pantalla de empleado.");
        }
    }
    // metodo para registrarse
    @FXML
    public void btnRegistrarse(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vista/Registrarse.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            lblMensajeError.setText("No se pudo abrir la pantalla de registro.");
            e.printStackTrace();
        }
    }
}
