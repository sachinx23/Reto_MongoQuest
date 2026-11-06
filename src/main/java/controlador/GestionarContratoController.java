package controlador;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import modelo.Contrato;
import DAO.ContratoDAO;
import DAO.implementacion.ContratoDaoImpl;

public class GestionarContratoController implements Initializable {

    @FXML
    private TextField txtFiltroTelefono;
    @FXML
    private ComboBox<String> comboFiltroEstado;

    @FXML
    private TableView<Contrato> tablaContratos;
    @FXML
    private TableColumn<Contrato, String> colIdContrato;
    @FXML
    private TableColumn<Contrato, String> colTelefono;
    @FXML
    private TableColumn<Contrato, String> colIdCliente;
    @FXML
    private TableColumn<Contrato, String> colIdTarifa;
    @FXML
    private TableColumn<Contrato, String> colIdEmpleado;
    @FXML
    private TableColumn<Contrato, String> colEstado;
    @FXML
    private TableColumn<Contrato, String> colFechaInicio;
    @FXML
    private TableColumn<Contrato, String> colFechaFin;

    @FXML
    private Button btnAceptarPeticion;
    @FXML
    private Button btnRechazarPeticion;
    @FXML
    private Button btnRescindirContrato;
    @FXML
    private Button btnRefrescar;

    private Contrato contratoSeleccionado;
    private final ContratoDAO contratoDAO = new ContratoDaoImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> estados = FXCollections.observableArrayList(
                "Todos",
                "pendiente",
                "pendiente de firma",
                "activo",
                "rescindido",
                "caducado",
                "rechazado"
        );
        comboFiltroEstado.setItems(estados);
        comboFiltroEstado.setValue("Todos");

        colIdContrato.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colIdTarifa.setCellValueFactory(new PropertyValueFactory<>("idTarifa"));
        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaComienzo"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));

        cargarContratos();

        tablaContratos.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Contrato>() {
            @Override
            public void changed(ObservableValue<? extends Contrato> obs, Contrato oldSel, Contrato newSel) {
                contratoSeleccionado = newSel;
                actualizarEstadoBotones();
            }
        });
    }

    public void cargarContratos() {
        List<Contrato> lista = contratoDAO.listarContratos();
        if (lista != null) {
            ObservableList<Contrato> datos = FXCollections.observableArrayList(lista);
            tablaContratos.setItems(datos);
        }
        limpiarSeleccion();
    }

    @FXML
    private void filtrarContratos(ActionEvent event) {
        String tel = txtFiltroTelefono.getText();
        String est = comboFiltroEstado.getValue();
        List<Contrato> filtrados = contratoDAO.filtrarContratos(tel, est);
        tablaContratos.setItems(FXCollections.observableArrayList(filtrados));
        limpiarSeleccion();
    }

    @FXML
    private void aceptarPeticion(ActionEvent event) {
        if (contratoSeleccionado != null) {
            if (contratoDAO.aprobarContrato(contratoSeleccionado, IniciarSesionController.idUsuarioActual)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa confirmada. Contrato pendiente de firma por el cliente.");
                cargarContratos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo confirmar la tarifa.");
            }
        }
    }

    @FXML
    private void rechazarPeticion(ActionEvent event) {
        if (contratoSeleccionado != null) {
            if (contratoDAO.rechazarContratos(contratoSeleccionado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contrato rechazado.");
                cargarContratos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo rechazar el contrato.");
            }
        }
    }

    @FXML
    private void rescindirContrato(ActionEvent event) {
        if (contratoSeleccionado != null) {
            if (contratoDAO.rescindirContrato(contratoSeleccionado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contrato rescindido correctamente.");
                cargarContratos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo rescindir el contrato.");
            }
        }
    }

    @FXML
    private void refrescarFiltro(ActionEvent event) {
        txtFiltroTelefono.clear();
        comboFiltroEstado.setValue("Todos");
        cargarContratos();
    }

    private void limpiarSeleccion() {
        tablaContratos.getSelectionModel().clearSelection();
        contratoSeleccionado = null;
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        boolean haySeleccion = contratoSeleccionado != null;
        boolean pendiente = false;
        boolean activo = false;

        if (haySeleccion) {
            if ("pendiente".equalsIgnoreCase(contratoSeleccionado.getEstado())) {
                pendiente = true;
            }
            if ("activo".equalsIgnoreCase(contratoSeleccionado.getEstado())) {
                activo = true;
            }
        }

        btnAceptarPeticion.setDisable(!pendiente);
        btnRechazarPeticion.setDisable(!pendiente);
        btnRescindirContrato.setDisable(!activo);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}