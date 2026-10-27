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
 * Pantalla "Borrar cuenta empleado" del administrador.
 */
public class BorrarEmpleadoController implements Initializable {

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colNombreUsuario;
    @FXML private TableColumn<Empleado, String> colCorreo;

    private final DAO dao = new DAOImplementacion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new PropertyValueFactory<Empleado, String>("nombre"));
        colNombreUsuario.setCellValueFactory(new PropertyValueFactory<Empleado, String>("nombreUsuario"));
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
    public void btnBorrar(ActionEvent event) {
        Empleado empleado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (empleado == null) {
            ShowAlert.showAlert("Aviso", "Selecciona un empleado de la tabla primero.", Alert.AlertType.WARNING);
            return;
        }
        if (ShowAlert.confirmar("Eliminar cuenta de empleado", "¿Seguro que quieres eliminar al empleado "
                + empleado.getNombre() + "? Perderá su acceso al sistema.")) {
            try {
                dao.borrarEmpleado(empleado.getId());
                ShowAlert.showAlert("Borrado exitoso", "El empleado ha sido eliminado.", Alert.AlertType.INFORMATION);
                cargarEmpleados();
            } catch (OurException e) {
                ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}
