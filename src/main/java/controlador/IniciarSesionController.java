package controlador;

import DAO.AdministradorDAO;
import DAO.ClienteDAO;
import DAO.EmpleadoDAO;
import DAO.implementacion.AdministradorDAOImpl;
import DAO.implementacion.ClienteDaoImplementacion;
import DAO.implementacion.EmpleadoDaoImplementacion;

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
 * Controlador de Iniciar Sesión con soporte para Admin, Empleado y Cliente
 * @author Eneko.Plaza
 */
public class IniciarSesionController implements Initializable {

    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDaoImplementacion();
    private final ClienteDAO clienteDAO = new ClienteDaoImplementacion();

    public static String idUsuarioActual;

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

        String rolDeducido = null;

        // 1. Busca en Administradores
        Document usuarioObtenido = administradorDAO.comprobarUsuarios(usuario, contrasena);
        if (usuarioObtenido != null) {
            rolDeducido = "Admin";
        }

        // 2. Si no lo encuentra, busca en Empleados
        if (usuarioObtenido == null) {
            usuarioObtenido = empleadoDAO.comprobarUsuarios(usuario, contrasena);
            if (usuarioObtenido != null) {
                rolDeducido = "Empleado";
            }
        }

        // 3. Si tampoco lo encuentra, busca en Clientes
        if (usuarioObtenido == null) {
            usuarioObtenido = clienteDAO.comprobarUsuarios(usuario, contrasena);
            if (usuarioObtenido != null) {
                rolDeducido = "Cliente";
            }
        }

        if (usuarioObtenido != null) {
            idUsuarioActual = usuarioObtenido.getObjectId("_id").toHexString();

            String nombre = usuarioObtenido.getString("nombre");
            if (nombre == null) {
                nombre = usuarioObtenido.getString("nombreUsuario");
            }

            // Si el documento de MongoDB tiene un campo "rol", lo usamos; si no, usamos el deducido por el DAO
            String rolBD = usuarioObtenido.getString("rol");
            String rolFinal = (rolBD != null && !rolBD.isEmpty()) ? rolBD : rolDeducido;

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Inicio de Sesión Correcto");
            alert.setHeaderText(null);
            alert.setContentText("¡Bienvenido/a " + (nombre != null ? nombre : usuario) + "!\nRol: " + rolFinal);
            alert.showAndWait();

            // Redirección según el rol correspondiente
            if (rolFinal != null) {
                if (rolFinal.equalsIgnoreCase("Admin")) {
                    cambiarPantalla("/vista/Admin.fxml");
                } else if (rolFinal.equalsIgnoreCase("Empleado")) {
                    cambiarPantalla("/vista/Empleado.fxml");
                } else if (rolFinal.equalsIgnoreCase("Cliente")) {
                    cambiarPantalla("/vista/Cliente.fxml");
                } else {
                    lblMensajeError.setText("El rol asignado no es válido.");
                }
            } else {
                lblMensajeError.setText("Error: El usuario no tiene rol asignado.");
            }

        } else {
            lblMensajeError.setText("Usuario o contraseña incorrectos.");
        }
    }

    @FXML
    public void cambiarPantalla(String ruta) {
        try {
            URL resource = getClass().getResource(ruta);
            if (resource == null) {
                lblMensajeError.setText("Error: No se encuentra el archivo FXML en " + ruta);
                System.err.println("No se encontró el archivo de vista en la ruta: " + ruta);
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblMensajeError.setText("Error al cargar la pantalla. Revisa la consola.");
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