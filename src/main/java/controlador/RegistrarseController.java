package controlador;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.ShowAlert;
import exception.OurException;
import DAO.ClienteDAO;
import DAO.implementacion.ClienteDAOImplementacion;
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
 * Controlador de la ventana de registro de nuevos clientes en JavaFX ({@code Registrarse.fxml}).
 * Valida el formato de los datos personales y credenciales introducidos en el formulario,
 * registra el nuevo cliente en MongoDB mediante {@link ClienteDAO} y redirige de vuelta
 * a la pantalla de inicio de sesión.
 *
 * @author Eneko.Plaza
 */
public class RegistrarseController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y errores de navegación del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(RegistrarseController.class.getName());

    /** Objeto de acceso a datos para persistir el alta del nuevo cliente en MongoDB. */
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    /** Campo de texto para el nombre de usuario elegido por el cliente. */
    @FXML
    private TextField txtUsuario;

    /** Campo de texto para el nombre de pila del cliente. */
    @FXML
    private TextField txtNombre;

    /** Campo de texto para los apellidos del cliente. */
    @FXML
    private TextField txtApellidos;

    /** Campo de contraseña enmascarada para la clave de acceso (mínimo 6 caracteres). */
    @FXML
    private PasswordField txtContrasena;

    /** Campo de texto para la dirección de correo electrónico del cliente. */
    @FXML
    private TextField txtCorreo;

    /** Campo de texto para el Documento Nacional de Identidad (8 dígitos y letra mayúscula). */
    @FXML
    private TextField txtDni;

    /** Campo de texto para el número de teléfono de contacto del cliente. */
    @FXML
    private TextField txtTelefono;

    /** Campo de texto para la dirección postal del cliente. */
    @FXML
    private TextField txtDireccion;

    /** Etiqueta utilizada para mostrar mensajes de validación o error en el formulario. */
    @FXML
    private Label lblMensajeError;

    /** Botón principal que dispara el proceso de validación y registro del cliente. */
    @FXML
    private Button btnRegistrarse;

    /** Botón para cancelar el registro y regresar a la vista de inicio de sesión. */
    @FXML
    private Button btnVolver;

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
     * Manejador del botón de registro.
     * Comprueba que ningún campo esté vacío y valida el formato del correo electrónico,
     * el DNI ({@code [0-9]{8}[A-Z]}) y la longitud mínima de la contraseña (6 caracteres).
     * Si la validación es correcta, registra al cliente mediante {@link ClienteDAO#registrarCliente},
     * muestra una confirmación y redirige a la pantalla de inicio de sesión.
     *
     * @param event Evento de acción disparado al pulsar el botón de registrarse.
     */
    @FXML
    public void btnRegistrarse(ActionEvent event) {
        try {
            String usuario = txtUsuario.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String contrasena = txtContrasena.getText().trim();
            String correo = txtCorreo.getText().trim();
            String dni = txtDni.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();

            if (usuario.isEmpty() || nombre.isEmpty() || apellidos.isEmpty() || contrasena.isEmpty()
                    || correo.isEmpty() || dni.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                lblMensajeError.setText("Rellena todos los campos.");
                return;
            }
            if (!correo.contains("@") || !correo.contains(".")) {
                lblMensajeError.setText("El correo no es válido.");
                return;
            }
            if (!dni.matches("[0-9]{8}[A-Z]")) {
                lblMensajeError.setText("DNI introducido incorrectamente.");
                return;
            }
            if (contrasena.length() < 6) {
                lblMensajeError.setText("La contraseña debe tener mínimo 6 caracteres.");
                return;
            }

            lblMensajeError.setText("");

            boolean registrado = clienteDAO.registrarCliente(usuario, nombre, apellidos, contrasena, correo, dni, telefono, direccion);

            if (registrado) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("¡Usuario registrado correctamente!");
                alert.showAndWait();

                volverAIniciarSesion(event);
            } else {
                lblMensajeError.setText("No se pudo registrar el usuario. Comprueba los datos.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Sustituye la escena de la ventana ({@link Stage}) actual por la vista de inicio
     * de sesión ({@code /vista/IniciarSesion.fxml}).
     *
     * @param event Evento de acción utilizado para obtener la ventana actual.
     */
    @FXML
    private void volverAIniciarSesion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver al login", e);
            lblMensajeError.setText("Error al cargar la pantalla de inicio de sesión.");
        }
    }
}