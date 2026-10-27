package controlador;

import dao.DAO;
import dao.DAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Empleado;

/**
 * Pantalla "Cambiar puesto": ascender un empleado a administrador.
 */
public class CambiarPuestoController implements Initializable {

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colCorreo;

    private final DAO dao = new DAOImplementacion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new PropertyValueFactory<Empleado, String>("nombre"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<Empleado, String>("correo"));
        cargarEmpleados();
    }

    private void cargarEmpleados() {
        try {
            tablaEmpleados.setItems(FXCollections.observableArrayList(dao.obtenerEmpleados()));
        } catch (OurException e) {
            ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void btnAscender(ActionEvent event) {
        Empleado empleado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (empleado == null) {
            ShowAlert.showAlert("Aviso", "Selecciona un empleado de la tabla primero.", Alert.AlertType.WARNING);
            return;
        }
        if (ShowAlert.confirmar("Ascender empleado", "¿Estás seguro de ascender a " + empleado.getNombre()
                + "? Pasará a tener privilegios totales de Administrador.")) {
            try {
                dao.ascenderEmpleado(empleado);
                ShowAlert.showAlert("Cambio exitoso", empleado.getNombre() + " ahora es administrador.",
                        Alert.AlertType.INFORMATION);
                cargarEmpleados();
            } catch (OurException e) {
                ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}
