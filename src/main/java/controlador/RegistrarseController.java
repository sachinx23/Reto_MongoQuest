package controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class para la ventana Registrarse
 *
 * @author Eneko.Plaza
 */
public class RegistrarseController implements Initializable {

    @FXML
    private TextField txtUsuario;

    @FXML
    private TextField txtNombre;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtDni;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtDireccion;

    @FXML
    private Label lblMensajeError;

    @FXML
    private Button btnRegistrarse;

    @FXML
    private Button btnVolver;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (lblMensajeError != null) {
            lblMensajeError.setText("");
        }
    }

    @FXML
    public void btnRegistrarse(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String nombre = txtNombre.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        String correo = txtCorreo.getText().trim();
        String dni = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        // Validar que no haya campos vacíos
        if (usuario.isEmpty() || nombre.isEmpty() || contrasena.isEmpty()
                || correo.isEmpty() || dni.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
            lblMensajeError.setText("Rellena todos los campos.");
            return;
        }
        //Validar correo
        if (!correo.contains("@") || !correo.contains(".")) {
            lblMensajeError.setText("El correo no es válido.");
            return;
        }
        //Validar dni
        if (!dni.matches("[0-9]{8}[A-Z]")) {
            lblMensajeError.setText("DNI introducido incorrectamente.");
            return;
        }
        //validar contraseña
        if (contrasena.length() < 6) {
            lblMensajeError.setText("La contraseña debe tener mínimo 6 caracteres.");
            return;
        }

        lblMensajeError.setText("");

        boolean registrado = DAO.registarCliente(usuario, nombre, contrasena, correo, dni, telefono, direccion);

        if (registrado) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setContentText("¡Usuario registrado correctamente!");
            alert.showAndWait();

            //  Volver automáticamente a la pantalla de Inicio de Sesión
            volverAIniciarSesion(event);
        } else {
            lblMensajeError.setText("No se pudo registrar el usuario. Comprueba los datos.");
        }
    }

    //Metodo ipara que vuelva a iniciar sesion
    private void volverAIniciarSesion(ActionEvent event) {
        try {
            // Revisa que la ruta apunte al nombre y directorio exacto de tu FXML de login
            Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblMensajeError.setText("Error al cargar la pantalla de inicio de sesión.");
        }
    }
}
