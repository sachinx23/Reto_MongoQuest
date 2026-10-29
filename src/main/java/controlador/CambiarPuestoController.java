package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import modelo.Empleado;

public class CambiarPuestoController {

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colCorreo;

    @FXML
    public void initialize() {
        // TODO parte del administrador
    }

    @FXML
    public void btnAscender(ActionEvent event) {
        // TODO parte del administrador
    }
}
