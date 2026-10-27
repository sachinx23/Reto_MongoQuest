package controlador;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Tarifa;

public class GestionTarifasController implements Initializable {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPrecio;
    @FXML
    private ComboBox<String> comboTipo;
    @FXML
    private TextArea txtDetalles;
    @FXML
    private CheckBox chkActiva;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Tarifa> tablaTarifas;
    @FXML
    private TableColumn<Tarifa, String> colNombre;
    @FXML
    private TableColumn<Tarifa, Double> colPrecio;
    @FXML
    private TableColumn<Tarifa, String> colTipo;
    @FXML
    private TableColumn<Tarifa, String> colDetalles;
    @FXML
    private TableColumn<Tarifa, Boolean> colActiva;

    private Tarifa tarifaSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboTipo.setItems(FXCollections.observableArrayList("Fibra", "Móvil", "Convergente", "Televisión"));

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoTarifa"));
        colDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));

        cargarTabla();

        tablaTarifas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tarifaSeleccionada = newSelection;
                mostrarDatosEnFormulario(tarifaSeleccionada);
            }
        });
    }

    // Carga los datos de MongoDB en la TableView
    private void cargarTabla() {
        List<Tarifa> lista = DAO.listarTarifas();
        ObservableList<Tarifa> datos = FXCollections.observableArrayList(lista);
        tablaTarifas.setItems(datos);
    }

    //  Pasa los datos de la fila seleccionada a los controles del formulario
    private void mostrarDatosEnFormulario(Tarifa t) {
        txtNombre.setText(t.getNombreTarifa());
        txtPrecio.setText(String.valueOf(t.getPrecio()));
        comboTipo.setValue(t.getTipoTarifa());
        txtDetalles.setText(t.getDetalles());
        chkActiva.setSelected(t.getActiva());
    }

    // guardar campos
    @FXML
    private void btnGuardarAccion() {
        if (!validarCampos()) return;

        String nombre = txtNombre.getText().trim();
        double precio = Double.parseDouble(txtPrecio.getText().trim());
        String tipo = comboTipo.getValue();
        String detalles = txtDetalles.getText().trim();
        boolean activa = chkActiva.isSelected();

        Tarifa nueva = new Tarifa(nombre, precio, detalles, tipo, activa);

        if (DAO.insertarTarifa(nueva)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa guardada en MongoDB.");
            cargarTabla();
            btnLimpiarAccion();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar la tarifa.");
        }
    }

    // catualizar tarifas
    @FXML
    private void btnActualizarAccion() {
        if (tarifaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona una tarifa de la tabla.");
            return;
        }

        if (!validarCampos()) return;

        tarifaSeleccionada.setNombreTarifa(txtNombre.getText().trim());
        tarifaSeleccionada.setPrecio(Double.parseDouble(txtPrecio.getText().trim()));
        tarifaSeleccionada.setTipoTarifa(comboTipo.getValue());
        tarifaSeleccionada.setDetalles(txtDetalles.getText().trim());
        tarifaSeleccionada.setActiva(chkActiva.isSelected());

        if (DAO.actualizarTarifa(tarifaSeleccionada)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa actualizada.");
            cargarTabla();
            btnLimpiarAccion();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la tarifa.");
        }
    }

    // eliminar tarifas
    @FXML
    private void btnEliminarAccion() {
        if (tarifaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona una tarifa para eliminar.");
            return;
        }

        if (DAO.eliminarTarifa(tarifaSeleccionada)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa eliminada.");
            cargarTabla();
            btnLimpiarAccion();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la tarifa.");
        }
    }

    // limpiar formualario
    @FXML
    private void btnLimpiarAccion() {
        txtNombre.clear();
        txtPrecio.clear();
        comboTipo.setValue(null);
        txtDetalles.clear();
        chkActiva.setSelected(true);
        tarifaSeleccionada = null;
        tablaTarifas.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Vacío", "Introduce el nombre.");
            return false;
        }
        if (comboTipo.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Vacío", "Selecciona un tipo.");
            return false;
        }
        try {
            Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Formato Incorrecto", "El precio debe ser un número decimal válido.");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}