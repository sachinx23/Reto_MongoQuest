package controlador;

import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.Administrador;

/**
 * Mi Perfil del administrador.
 */
public class PerfilAdminController {

    private static final Logger LOGGER = Logger.getLogger(PerfilAdminController.class.getName());

    private final DAO dao = Sesion.getDao();

    private Administrador admin;

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;

    @FXML
    public void initialize() {
        admin = (Administrador) Sesion.getUsuario();
        cargarPerfil();
    }

    private void cargarPerfil() {
        txtNombre.setText(admin.getNombre());
        txtNombreUsuario.setText(admin.getNombreUsuario());
        txtContrasena.setText(admin.getContrasena());
        txtCorreo.setText(admin.getCorreo());
        txtCuentaBancaria.setText(admin.getCuentaBancaria());
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        String cuenta = txtCuentaBancaria.getText().trim().toUpperCase();
        String error = ValidarPerfil.comprobarEmpleado(txtNombre.getText(), txtNombreUsuario.getText(),
                txtContrasena.getText(), txtCorreo.getText(), cuenta);
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            return;
        }
        // Se crea un administrador nuevo con los cambios: si falla al guardar, el de la sesión no se toca
        Administrador modificado = new Administrador(admin.getId(), txtNombre.getText().trim(), txtNombreUsuario.getText().trim(),
                txtContrasena.getText().trim(), txtCorreo.getText().trim(), cuenta);
        try {
            dao.actualizarEmpleado(modificado);
            admin = modificado;
            Sesion.setUsuario(modificado);
            ShowAlert.info("Modificación exitosa", "Tus datos se han actualizado correctamente.");
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
            cargarPerfil();
        }
    }
}
