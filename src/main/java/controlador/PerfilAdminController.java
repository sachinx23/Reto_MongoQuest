package controlador;

import dao.DAO;
import dao.DAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.Administrador;

/**
 * Pantalla "Mi Perfil" del administrador: ver y modificar sus datos.
 */
public class PerfilAdminController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;

    private final DAO dao = new DAOImplementacion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Administrador admin = AdministradorController.getUsuario();
        if (admin != null) {
            txtNombre.setText(admin.getNombre());
            txtNombreUsuario.setText(admin.getNombreUsuario());
            txtContrasena.setText(admin.getContrasena());
            txtCorreo.setText(admin.getCorreo());
            txtCuentaBancaria.setText(admin.getCuentaBancaria());
        }
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        Administrador admin = AdministradorController.getUsuario();
        if (admin == null) {
            return;
        }
        String error = ValidarPerfil.comprobar(txtNombre.getText(), txtNombreUsuario.getText(),
                txtContrasena.getText(), txtCorreo.getText());
        if (error != null) {
            ShowAlert.showAlert("Datos inválidos", error, Alert.AlertType.WARNING);
            return;
        }
        if (ShowAlert.confirmar("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            admin.setNombre(txtNombre.getText().trim());
            admin.setNombreUsuario(txtNombreUsuario.getText().trim());
            admin.setContrasena(txtContrasena.getText().trim());
            admin.setCorreo(txtCorreo.getText().trim());
            admin.setCuentaBancaria(txtCuentaBancaria.getText().trim());
            try {
                dao.actualizarEmpleado(admin);
                ShowAlert.showAlert("Modificación exitosa", "Tus datos se han actualizado correctamente.", Alert.AlertType.INFORMATION);
            } catch (OurException e) {
                ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}
