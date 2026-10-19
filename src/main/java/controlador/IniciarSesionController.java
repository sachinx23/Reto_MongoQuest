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
        
        // Llamada al método estático del DAO
        Document usuarioObtenido = DAO.comprobarUsuarios(usuario, contrasena);
        
        if (usuarioObtenido != null) {
            // Se comprueba "nombre" o "nombreUsuario"
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
            
        } else {
            lblMensajeError.setText("Usuario o contraseña incorrectos.");
        }
    }
}