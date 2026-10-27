package controlador;

import dao.DAO;
import dao.DAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import modelo.Sucursal;

/**
 * Pantalla "Gestión sucursal": crear, modificar y eliminar sucursales.
 * Al pulsar una fila de la tabla sus datos pasan al formulario.
 */
public class GestionSucursalController implements Initializable {

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

    private final DAO dao = new DAOImplementacion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new PropertyValueFactory<Sucursal, String>("nombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<Sucursal, String>("direccion"));
        colLocalidad.setCellValueFactory(new PropertyValueFactory<Sucursal, String>("localidad"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<Sucursal, String>("telefono"));
        colHorario.setCellValueFactory(new PropertyValueFactory<Sucursal, String>("horario"));
        cargarSucursales();
    }

    private void cargarSucursales() {
        try {
            tablaSucursales.setItems(FXCollections.observableArrayList(dao.obtenerSucursales()));
        } catch (OurException e) {
            ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void seleccionarSucursal(MouseEvent event) {
        Sucursal s = tablaSucursales.getSelectionModel().getSelectedItem();
        if (s != null) {
            txtNombre.setText(s.getNombre());
            txtTelefono.setText(s.getTelefono());
            txtDireccion.setText(s.getDireccion());
            txtLocalidad.setText(s.getLocalidad());
            txtCodigoPostal.setText(s.getCodigoPostal() != 0 ? String.valueOf(s.getCodigoPostal()) : "");
            txtHorario.setText(s.getHorario());
        }
    }

    /** Comprueba el formulario. Devuelve el mensaje de error o null si es correcto. */
    private String validar() {
        if (txtNombre.getText().trim().isEmpty() || txtDireccion.getText().trim().isEmpty()
                || txtTelefono.getText().trim().isEmpty()) {
            return "Nombre, dirección y teléfono son obligatorios.";
        }
        if (!txtCodigoPostal.getText().trim().isEmpty() && !txtCodigoPostal.getText().trim().matches("[0-9]{5}")) {
            return "El código postal debe tener 5 números.";
        }
        return null;
    }

    /** Copia los datos del formulario a la sucursal. */
    private void leerFormulario(Sucursal s) {
        s.setNombre(txtNombre.getText().trim());
        s.setTelefono(txtTelefono.getText().trim());
        s.setDireccion(txtDireccion.getText().trim());
        s.setLocalidad(txtLocalidad.getText().trim());
        String cp = txtCodigoPostal.getText().trim();
        s.setCodigoPostal(cp.isEmpty() ? 0 : Integer.parseInt(cp));
        s.setHorario(txtHorario.getText().trim());
    }

    @FXML
    public void btnCrear(ActionEvent event) {
        String error = validar();
        if (error != null) {
            ShowAlert.showAlert("Campos incompletos", error, Alert.AlertType.WARNING);
            return;
        }
        if (ShowAlert.confirmar("Confirmar creación", "¿Seguro que deseas crear la sucursal " + txtNombre.getText().trim() + "?")) {
            Sucursal s = new Sucursal(null, "", "", "", "", "", 0, new ArrayList<String>());
            leerFormulario(s);
            try {
                dao.insertarSucursal(s);
                ShowAlert.showAlert("Sucursal registrada", "La nueva sucursal ha sido dada de alta.", Alert.AlertType.INFORMATION);
                btnLimpiar(event);
                cargarSucursales();
            } catch (OurException e) {
                ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        Sucursal s = tablaSucursales.getSelectionModel().getSelectedItem();
        if (s == null) {
            ShowAlert.showAlert("Aviso", "Selecciona una sucursal de la tabla primero.", Alert.AlertType.WARNING);
            return;
        }
        String error = validar();
        if (error != null) {
            ShowAlert.showAlert("Datos inválidos", error, Alert.AlertType.WARNING);
            return;
        }
        if (ShowAlert.confirmar("Confirmar modificación", "¿Seguro que deseas guardar los cambios de " + s.getNombre() + "?")) {
            leerFormulario(s);
            try {
                dao.actualizarSucursal(s);
                ShowAlert.showAlert("Modificación exitosa", "La sucursal se ha actualizado.", Alert.AlertType.INFORMATION);
                cargarSucursales();
            } catch (OurException e) {
                ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                cargarSucursales();
            }
        }
    }

    @FXML
    public void btnEliminar(ActionEvent event) {
        Sucursal s = tablaSucursales.getSelectionModel().getSelectedItem();
        if (s == null) {
            ShowAlert.showAlert("Aviso", "Selecciona una sucursal de la tabla primero.", Alert.AlertType.WARNING);
            return;
        }
        if (ShowAlert.confirmar("Confirmar eliminación", "¿Seguro que deseas eliminar la sucursal " + s.getNombre() + "?")) {
            try {
                dao.borrarSucursal(s.getId());
                ShowAlert.showAlert("Sucursal eliminada", "La sucursal ha sido eliminada.", Alert.AlertType.INFORMATION);
                btnLimpiar(event);
                cargarSucursales();
            } catch (OurException e) {
                ShowAlert.showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void btnLimpiar(ActionEvent event) {
        txtNombre.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        txtLocalidad.clear();
        txtCodigoPostal.clear();
        txtHorario.clear();
        tablaSucursales.getSelectionModel().clearSelection();
    }
}
