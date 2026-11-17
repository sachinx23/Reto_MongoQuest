package controlador;

import java.util.logging.Logger;
import DAO.ClienteDAO;
import DAO.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import modelo.Cliente;
import org.bson.Document;

/**
 * Controlador de la subvista de perfil del cliente en JavaFX.
 * Muestra los datos personales y bancarios del cliente con sesión activa y gestiona
 * su validación, confirmación y actualización en la base de datos.
 *
 * @author Eneko.Plaza
 */
public class ClientePerfilController {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(ClientePerfilController.class.getName());

    /** Objeto de acceso a datos para persistir las modificaciones del perfil del cliente. */
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    /** Instancia del cliente actualmente autenticado cuyos datos se visualizan y editan. */
    private Cliente cliente;

    /** Campo de texto para el nombre de pila del cliente. */
    @FXML private TextField txtNombre;

    /** Campo de texto para el nombre de usuario de acceso. */
    @FXML private TextField txtNombreUsuario;

    /** Campo de texto para el Documento Nacional de Identidad (DNI). */
    @FXML private TextField txtDni;

    /** Campo de texto para la dirección de correo electrónico. */
    @FXML private TextField txtCorreo;

    /** Campo de texto para el número de teléfono de contacto. */
    @FXML private TextField txtTelefono;

    /** Campo de texto para el número de cuenta bancaria (IBAN). */
    @FXML private TextField txtCuentaBancaria;

    /** Campo de texto para la dirección postal del cliente. */
    @FXML private TextField txtDireccion;

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Recupera el cliente activo desde {@link ClienteController} y vuelca sus datos
     * en los campos del formulario.
     */
    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        cargarPerfil();
    }

    /**
     * Rellena todos los campos de texto de la interfaz con los valores actuales
     * del objeto {@code cliente}.
     */
    private void cargarPerfil() {
        txtNombre.setText(cliente.getNombre());
        txtNombreUsuario.setText(cliente.getNombreUsuario());
        txtDni.setText(cliente.getDni());
        txtCorreo.setText(cliente.getCorreo());
        txtTelefono.setText(cliente.getTelefono());
        txtCuentaBancaria.setText(cliente.getCuentaBancaria());
        
        txtDireccion.setText(cliente.toString());
    }

    /**
     * Manejador del botón para guardar los cambios realizados en el perfil.
     * Recoge y normaliza los valores introducidos, los valida mediante
     * {@link ValidarPerfil#comprobarCliente}, solicita confirmación al usuario y actualiza
     * tanto la base de datos como el estado de sesión en {@link ClienteController}.
     * Si ocurre un error durante la actualización, restaura los datos originales en el formulario.
     *
     * @param event Evento de acción disparado al pulsar el botón de modificar perfil.
     */
    @FXML
    public void btnModificar(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String dni = txtDni.getText().trim().toUpperCase();
        String correo = txtCorreo.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String cuenta = txtCuentaBancaria.getText().trim().toUpperCase();

        String error = ValidarPerfil.comprobarCliente(nombre, nombreUsuario, dni, correo, telefono, direccion, cuenta);
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            return;
        }

        Cliente modificado = new Cliente(cliente.getId(), cuenta, nombreUsuario, nombre, cliente.getApellidos(),
                cliente.getContrasena(), correo, dni, telefono, direccion);
        try {
            clienteDAO.actualizarCliente(modificado);
            cliente = modificado;
            ClienteController.setCliente(modificado);
            ClienteController.actualizarBienvenida();
            ShowAlert.info("Modificación exitosa", "Los datos se han actualizado correctamente.");
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
            cargarPerfil();
        }
    }
}