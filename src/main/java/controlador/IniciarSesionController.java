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
import modelo.Administrador;
import modelo.Empleado;
import modelo.Usuario;

public class IniciarSesionController {

    private static final Logger LOGGER = Logger.getLogger(IniciarSesionController.class.getName());

    private final DAO dao = Sesion.getDao();

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblMensajeError;

    @FXML
    public void btnIniciarSesion(ActionEvent event) {
        String nombreUsuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            lblMensajeError.setText("Por favor, rellena todos los campos.");
            return;
        }
        lblMensajeError.setText("");

        try {
            Usuario usuario = dao.login(nombreUsuario, contrasena);
            if (usuario == null) {
                lblMensajeError.setText("Usuario o contraseña incorrectos.");
                return;
            }
            Sesion.setUsuario(usuario);

            // Administrador va antes que Empleado porque un Administrador también es un Empleado
            if (usuario instanceof Administrador) {
                ShowAlert.info("Inicio de sesión", "¡Bienvenido/a " + usuario.getNombre() + "!\nRol: Administrador");
                Reto_1.setRoot("Admin");
            } else if (usuario instanceof Empleado) {
                ShowAlert.info("Inicio de sesión", "¡Bienvenido/a " + usuario.getNombre() + "!\nRol: Empleado");
                Reto_1.setRoot("Empleado");
            } else {
                ShowAlert.info("Inicio de sesión", "¡Bienvenido/a " + usuario.getNombre() + "!\nRol: Cliente");
                Reto_1.setRoot("Cliente");
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void btnRegistrarse(ActionEvent event) {
        Reto_1.setRoot("Registrarse");
    }
}
