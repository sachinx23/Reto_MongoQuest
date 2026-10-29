package controlador;

import com.mycompany.reto_1.Reto_1;
import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.Cliente;

public class RegistrarseController {

    private static final Logger LOGGER = Logger.getLogger(RegistrarseController.class.getName());

    private final DAO dao = Sesion.getDao();

    @FXML private TextField txtUsuario;
    @FXML private TextField txtNombre;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private Label lblMensajeError;

    @FXML
    public void btnRegistrarse(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String nombre = txtNombre.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        String correo = txtCorreo.getText().trim();
        String dni = txtDni.getText().trim().toUpperCase();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        String error = ValidarPerfil.comprobarCliente(nombre, usuario, dni, correo, telefono, direccion, "");
        if (error != null) {
            lblMensajeError.setText(error);
            return;
        }
        if (contrasena.length() < 4) {
            lblMensajeError.setText("La contraseña debe tener mínimo 4 caracteres.");
            return;
        }
        lblMensajeError.setText("");

        Cliente cliente = new Cliente(null, nombre, usuario, contrasena, correo, "", dni, telefono, direccion);
        try {
            dao.insertarCliente(cliente);
            ShowAlert.info("Usuario registrado", "Tu cuenta ha sido creada correctamente.");
            Reto_1.setRoot("IniciarSesion");
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            lblMensajeError.setText(e.getMessage());
        }
    }

    @FXML
    public void btnSalir(ActionEvent event) {
        if (ShowAlert.confirmar("Cancelar registro", "¿Seguro que deseas cancelar el registro?")) {
            Reto_1.setRoot("IniciarSesion");
        }
    }
}
