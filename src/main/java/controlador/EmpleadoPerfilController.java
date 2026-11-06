package controlador;

import daoempleado.EmpleadoDAO;
import daoempleado.implementacion.EmpleadoDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.Empleado;

/**
 * Mi Perfil del empleado: ver y modificar sus datos.
 */
public class EmpleadoPerfilController {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoPerfilController.class.getName());

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOImplementacion();
    private Empleado empleado;

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;

    @FXML
    public void initialize() {
        empleado = (Empleado) Sesion.getUsuario();
        cargarPerfil();
    }

    private void cargarPerfil() {
        txtNombre.setText(empleado.getNombre());
        txtNombreUsuario.setText(empleado.getNombreUsuario());
        txtContrasena.setText(empleado.getContrasena());
        txtCorreo.setText(empleado.getCorreo());
        txtCuentaBancaria.setText(empleado.getCuentaBancaria());
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

        // Se crea un empleado nuevo con los cambios: si falla al guardar, el de la sesión no se toca
        Empleado modificado = new Empleado(empleado.getId(), txtNombre.getText().trim(), txtNombreUsuario.getText().trim(),
                txtContrasena.getText().trim(), txtCorreo.getText().trim(), cuenta, empleado.getIdSucursal());
        try {
            empleadoDAO.actualizarEmpleado(modificado);
            empleado = modificado;
            Sesion.setUsuario(modificado);
            EmpleadoController.actualizarBienvenida();
            ShowAlert.info("Modificación exitosa", "Tus datos se han actualizado correctamente.");
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
            cargarPerfil();
        }
    }
}
