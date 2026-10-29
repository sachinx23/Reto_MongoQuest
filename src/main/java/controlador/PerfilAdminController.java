package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PerfilAdminController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;

    @FXML
    public void initialize() {
        // TODO parte del administrador
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        // TODO parte del administrador
    }
}
