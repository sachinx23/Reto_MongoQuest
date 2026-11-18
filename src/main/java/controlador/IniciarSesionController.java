package controlador;

import log.LogApp;
import java.util.logging.Logger;
import java.util.logging.Level;
import exception.ShowAlert;
import exception.OurException;
import DAO.AdministradorDAO;
import DAO.ClienteDAO;
import DAO.EmpleadoDAO;
import DAO.implementacion.AdministradorDAOImpl;
import DAO.implementacion.ClienteDAOImplementacion;
import DAO.implementacion.EmpleadoDAOImplementacion;

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
 * Controlador de la pantalla de inicio de sesión en JavaFX.
 * Valida las credenciales introducidas consultando secuencialmente las colecciones de
 * Administradores, Empleados y Clientes en MongoDB, deduce o recupera el rol del usuario,
 * registra la sesión activa en {@link LogApp} y redirige a la vista principal correspondiente
 * o a la pantalla de registro de nuevos clientes.
 *
 * @author Eneko.Plaza
 */
public class IniciarSesionController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar inicios de sesión y errores de navegación.
     */
    private static final Logger LOGGER = Logger.getLogger(IniciarSesionController.class.getName());

    /** Objeto de acceso a datos para verificar credenciales de administradores. */
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();

    /** Objeto de acceso a datos para verificar credenciales de empleados. */
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOImplementacion();

    /** Objeto de acceso a datos para verificar credenciales de clientes. */
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    /** Identificador hexadecimal ({@code _id}) del usuario que ha iniciado sesión en la aplicación. */
    public static String idUsuarioActual;

    /** Campo de texto para introducir el nombre de usuario de acceso. */
    @FXML
    private TextField txtUsuario;

    /** Campo de contraseña enmascarada para introducir la clave de acceso. */
    @FXML
    private PasswordField txtContrasena;

    /** Etiqueta utilizada para mostrar mensajes de validación o error en el formulario. */
    @FXML
    private Label lblMensajeError;

    /** Botón principal que dispara la comprobación de credenciales e inicio de sesión. */
    @FXML
    private Button txtIniciarSesion;

    /**
     * Inicializa el controlador tras cargar su archivo FXML, limpiando cualquier
     * texto previo en la etiqueta de mensajes de error.
     *
     * @param url Ubicación utilizada para resolver rutas relativas del objeto raíz, o {@code null} si no se conoce.
     * @param rb  Recursos utilizados para localizar el objeto raíz, o {@code null} si no se localizó.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (lblMensajeError != null) {
            lblMensajeError.setText("");
        }
    }

    /**
     * Manejador del botón de inicio de sesión.
     * Comprueba que usuario y contraseña no estén vacíos y busca coincidencias en orden
     * en administradores, empleados y clientes. Si la autenticación tiene éxito, guarda
     * {@link #idUsuarioActual}, registra el usuario en {@link LogApp}, muestra un diálogo
     * de bienvenida y redirige a la interfaz correspondiente al rol ({@code Admin.fxml},
     * {@code Empleado.fxml} o {@code Cliente.fxml}).
     */
    @FXML
    public void btnIniciarSesion() {
        try {
            String usuario = txtUsuario.getText().trim();
            String contrasena = txtContrasena.getText().trim();

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                lblMensajeError.setText("Rellena todos los campos.");
                return;
            }

            lblMensajeError.setText("");

            String rolDeducido = null;

            Document usuarioObtenido = administradorDAO.comprobarUsuarios(usuario, contrasena);
            if (usuarioObtenido != null) {
                rolDeducido = "Admin";
            }

            if (usuarioObtenido == null) {
                usuarioObtenido = empleadoDAO.comprobarUsuarios(usuario, contrasena);
                if (usuarioObtenido != null) {
                    rolDeducido = "Empleado";
                }
            }

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

                String rolBD = usuarioObtenido.getString("rol");
                String rolFinal = (rolBD != null && !rolBD.isEmpty()) ? rolBD : rolDeducido;
                LogApp.setUsuario(usuario + " (" + rolFinal + ")");
                LOGGER.info("Sesión iniciada");

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Inicio de Sesión Correcto");
                alert.setHeaderText(null);
                alert.setContentText("¡Bienvenido/a " + (nombre != null ? nombre : usuario) + "!\nRol: " + rolFinal);
                alert.showAndWait();

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
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Sustituye la escena de la ventana ({@link Stage}) actual por la vista FXML indicada
     * en la ruta proporcionada.
     *
     * @param ruta Ruta relativa dentro de los recursos del proyecto hacia el archivo {@code .fxml}.
     */
    @FXML
    public void cambiarPantalla(String ruta) {
        try {
            URL resource = getClass().getResource(ruta);
            if (resource == null) {
                lblMensajeError.setText("Error: No se encuentra el archivo FXML en " + ruta);
                LOGGER.severe("No existe la pantalla " + ruta);
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir " + ruta, e);
            lblMensajeError.setText("Error al cargar la pantalla.");
        }
    }

    /**
     * Manejador del botón o enlace de registro que redirige al usuario a la vista
     * de alta de nuevos clientes ({@code /vista/Registrarse.fxml}).
     *
     * @param event Evento de acción disparado al pulsar el botón de registrarse.
     */
    @FXML
    public void btnRegistrarse(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vista/Registrarse.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir el registro", e);
            lblMensajeError.setText("No se pudo abrir la pantalla de registro.");
        }
    }
}