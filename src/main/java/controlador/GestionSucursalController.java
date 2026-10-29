package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import modelo.Sucursal;

public class GestionSucursalController {

    @FXML private TableView<Sucursal> tablaSucursales;
    @FXML private TableColumn<Sucursal, String> colNombre;
    @FXML private TableColumn<Sucursal, String> colDireccion;
    @FXML private TableColumn<Sucursal, String> colLocalidad;
    @FXML private TableColumn<Sucursal, String> colTelefono;
    @FXML private TableColumn<Sucursal, String> colHorario;

    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtLocalidad;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtHorario;

    @FXML
    public void initialize() {
        // TODO parte del administrador
    }

    @FXML
    public void seleccionarSucursal(MouseEvent event) {
        // TODO parte del administrador
    }

    @FXML
    public void btnCrear(ActionEvent event) {
        // TODO parte del administrador
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        // TODO parte del administrador
    }

    @FXML
    public void btnEliminar(ActionEvent event) {
        // TODO parte del administrador
    }

    @FXML
    public void btnLimpiar(ActionEvent event) {
        // TODO parte del administrador
    }
}
