package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.Sucursal;

public class AltaEmpleadoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;
    @FXML private ComboBox<Sucursal> cmbSucursal;

    @FXML
    public void initialize() {
        // TODO parte del administrador
    }

    @FXML
    public void btnDarDeAlta(ActionEvent event) {
        // TODO parte del administrador
    }
}
