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
import modelo.Contrato;

/**
 * Controlador para la gestión de contratos por parte de los empleados.
 *
 * @author eneko
 */
public class GestionarContratoController implements Initializable {

    // Filtros de Búsqueda
    @FXML
    private TextField txtFiltroTelefono;
    @FXML
    private ComboBox<String> comboFiltroEstado;

    // Tabla de Contratos y Solicitudes 
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

    // Botones de Acción 
    @FXML
    private Button btnAceptarPeticion;
    @FXML
    private Button btnRechazarPeticion;
    @FXML
    private Button btnRescindirContrato;
    @FXML
    private Button btnRefrescar;

    private Contrato contratoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración del ComboBox de filtro por estado
        comboFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos",
                "pendiente",
                "activo",
                "rescindido",
                "caducado",
                "rechazado"
        ));
        comboFiltroEstado.setValue("Todos");

        colIdContrato.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colIdTarifa.setCellValueFactory(new PropertyValueFactory<>("idTarifa"));
        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaComienzo"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));

        // Carga inicial de datos desde MongoDB
        cargarContratos();

        // Listener para capturar la selección del usuario en la tabla
        tablaContratos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            contratoSeleccionado = newSel;
            actualizarEstadoBotones();
        });
    }

    // Único método para cargar contratos en la tabla
    public void cargarContratos() {
        List<Contrato> lista = DAO.listarContratos();
        if (lista != null) {
            ObservableList<Contrato> datos = FXCollections.observableArrayList(lista);
            tablaContratos.setItems(datos);
        }
        limpiarSeleccion();
    }

    //filtrar contratos
    @FXML
    private void filtrarContratos(ActionEvent event) {
        String tel = txtFiltroTelefono.getText();
        String est = comboFiltroEstado.getValue();
        List<Contrato> filtrados = DAO.filtrarContratos(tel, est);
        tablaContratos.setItems(FXCollections.observableArrayList(filtrados));
        limpiarSeleccion();
    }
    
    
// aceptar peticiones
    @FXML
    private void aceptarPeticion(ActionEvent event) {
        if (contratoSeleccionado != null) {
            if (DAO.aprobarContrato(contratoSeleccionado, IniciarSesionController.idUsuarioActual)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contrato aprobado correctamente.");
                cargarContratos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo aprobar el contrato.");
            }
        }
    }

    // rechazar peticion
    @FXML
    private void rechazarPeticion(ActionEvent event) {
        if (contratoSeleccionado != null) {
            if (DAO.rechazarContratos(contratoSeleccionado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contrato rechazado.");
                cargarContratos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo rechazar el contrato.");
            }
        }
    }

    // rescindir contrato
    @FXML
    private void rescindirContrato(ActionEvent event) {
        if (contratoSeleccionado != null) {
            if (DAO.rescindirContrato(contratoSeleccionado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contrato rescindido correctamente.");
                cargarContratos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo rescindir el contrato.");
            }
        }
    }

    // refrescar filrtro
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

    //acrualizar estado
    private void actualizarEstadoBotones() {
        boolean haySeleccion = contratoSeleccionado != null;
        boolean pendiente = haySeleccion && "pendiente".equalsIgnoreCase(contratoSeleccionado.getEstado());
        boolean activo = haySeleccion && "activo".equalsIgnoreCase(contratoSeleccionado.getEstado());

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
