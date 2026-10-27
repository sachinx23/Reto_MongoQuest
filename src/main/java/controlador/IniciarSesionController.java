package controlador;

import dao.DAO;
import dao.DAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Empleado;

public class IniciarSesionController implements Initializable {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensajeError;

    @FXML
    private Button txtIniciarSesion;

    private final DAO dao = new DAOImplementacion();

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

        try {
            // Se busca primero en Administradores, luego en Empleados y por último en Clientes
            Administrador admin = dao.loginAdministrador(usuario, contrasena);
            if (admin != null) {
                ShowAlert.showAlert("Inicio de Sesión Correcto", "¡Bienvenido/a " + admin.getNombre() + "!\nRol: Administrador", AlertType.INFORMATION);
                cambiarPantalla("/vista/Admin.fxml");
                return;
            }

            Empleado empleado = dao.loginEmpleado(usuario, contrasena);
            if (empleado != null) {
                ShowAlert.showAlert("Inicio de Sesión Correcto", "¡Bienvenido/a " + empleado.getNombre() + "!\nRol: Empleado", AlertType.INFORMATION);
                EmpleadoController.setUsuario(empleado);
                cambiarPantalla("/vista/Empleado.fxml");
                return;
            }

            Cliente cliente = dao.loginCliente(usuario, contrasena);
            if (cliente != null) {
                ShowAlert.showAlert("Inicio de Sesión Correcto", "¡Bienvenido/a " + cliente.getNombre() + "!\nRol: Cliente", AlertType.INFORMATION);
                ClienteController.setUsuario(cliente);
                cambiarPantalla("/vista/Cliente.fxml");
                return;
            }

            lblMensajeError.setText("Usuario o contraseña incorrectos.");
        } catch (OurException e) {
            ShowAlert.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

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
