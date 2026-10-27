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
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.Empleado;
import modelo.Sucursal;

/**
 * Pantalla "Alta empleado" del administrador.
 */
public class AltaEmpleadoController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;
    @FXML private ComboBox<Sucursal> cmbSucursal;

    private final DAO dao = new DAOImplementacion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // El ComboBox muestra el nombre de cada sucursal (Sucursal.toString())
        try {
            cmbSucursal.getItems().setAll(dao.obtenerSucursales());
        } catch (OurException e) {
            ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void btnDarDeAlta(ActionEvent event) {
        String error = ValidarPerfil.comprobar(txtNombre.getText(), txtNombreUsuario.getText(),
                txtContrasena.getText(), txtCorreo.getText());
        if (error != null) {
            ShowAlert.showAlert("Campos incompletos", error, Alert.AlertType.WARNING);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar alta", "¿Seguro que deseas dar de alta a " + txtNombre.getText().trim() + "?")) {
            return;
        }
        Sucursal sucursal = cmbSucursal.getValue();
        Empleado empleado = new Empleado(null, txtNombre.getText().trim(), txtNombreUsuario.getText().trim(), "Empleado",
                txtContrasena.getText().trim(), txtCorreo.getText().trim(), txtCuentaBancaria.getText().trim(),
                sucursal != null ? sucursal.getId() : null);
        try {
            dao.insertarEmpleado(empleado);
            ShowAlert.showAlert("Empleado registrado", "La cuenta del nuevo trabajador ha sido generada en el sistema.",
                    Alert.AlertType.INFORMATION);
            limpiar();
        } catch (OurException e) {
            ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiar() {
        txtNombre.clear();
        txtNombreUsuario.clear();
        txtContrasena.clear();
        txtCorreo.clear();
        txtCuentaBancaria.clear();
        cmbSucursal.getSelectionModel().clearSelection();
    }
}
