package controlador;

import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Contrato;
import modelo.EstadoContrato;

/**
 * Gestión de contratos: filtrar, aceptar o rechazar peticiones y rescindir contratos activos.
 */
public class GestionarContratoController {

    private static final Logger LOGGER = Logger.getLogger(GestionarContratoController.class.getName());
    private static final String TODOS = "Todos";

    private final DAO dao = Sesion.getDao();

    private List<Contrato> todos = new ArrayList<>();

    @FXML private TextField txtFiltroTelefono;
    @FXML private ComboBox<String> cmbFiltroEstado;

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, String> colCliente;
    @FXML private TableColumn<Contrato, String> colTelefono;
    @FXML private TableColumn<Contrato, String> colTarifa;
    @FXML private TableColumn<Contrato, String> colPrecio;
    @FXML private TableColumn<Contrato, EstadoContrato> colEstado;
    @FXML private TableColumn<Contrato, String> colFechaInicio;
    @FXML private TableColumn<Contrato, String> colFechaFin;

    @FXML
    public void initialize() {
        cmbFiltroEstado.getItems().add(TODOS);
        for (EstadoContrato estado : EstadoContrato.values()) {
            cmbFiltroEstado.getItems().add(estado.toString());
        }
        cmbFiltroEstado.setValue(TODOS);
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTarifa.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaComienzoTexto"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFinalizacionTexto"));
        cargarContratos();
    }

    private void cargarContratos() {
        try {
            todos = dao.obtenerTodosLosContratos();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
        filtrar();
    }

    // Enseña solo los contratos que cumplen el filtro de teléfono y de estado
    private void filtrar() {
        String telefono = txtFiltroTelefono.getText().replace(" ", "");
        String estado = cmbFiltroEstado.getValue();
        List<Contrato> filtrados = new ArrayList<>();
        for (Contrato c : todos) {
            boolean telefonoOk = c.getTelefono().replace(" ", "").contains(telefono);
            boolean estadoOk = TODOS.equals(estado) || (c.getEstado() != null && c.getEstado().toString().equals(estado));
            if (telefonoOk && estadoOk) {
                filtrados.add(c);
            }
        }
        tablaContratos.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    public void btnFiltrar(ActionEvent event) {
        filtrar();
    }

    @FXML
    public void btnRefrescar(ActionEvent event) {
        txtFiltroTelefono.clear();
        cmbFiltroEstado.setValue(TODOS);
        cargarContratos();
    }

    // Devuelve el contrato seleccionado si está en el estado pedido; si no, avisa y devuelve null
    private Contrato seleccionadoEn(EstadoContrato estado, String aviso) {
        Contrato c = tablaContratos.getSelectionModel().getSelectedItem();
        if (c == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return null;
        }
        if (c.getEstado() != estado) {
            ShowAlert.info("Aviso", aviso);
            return null;
        }
        return c;
    }

    @FXML
    public void btnAceptar(ActionEvent event) {
        Contrato c = seleccionadoEn(EstadoContrato.PENDIENTE, "Solo se pueden aceptar peticiones pendientes.");
        if (c == null || !ShowAlert.confirmar("Validar tarifa", "¿Validar y activar esta tarifa para el cliente seleccionado?")) {
            return;
        }
        try {
            dao.validarContrato(c.getId(), Sesion.getUsuario().getId());
            ShowAlert.info("Tarifa validada", "La tarifa se ha validado y el contrato está activo.");
            cargarContratos();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnRechazar(ActionEvent event) {
        Contrato c = seleccionadoEn(EstadoContrato.PENDIENTE, "Solo se pueden rechazar peticiones pendientes.");
        if (c == null || !ShowAlert.confirmar("Rechazar petición", "¿Rechazar la petición de " + c.getNombreCliente() + "?")) {
            return;
        }
        try {
            dao.rechazarPeticion(c.getId(), Sesion.getUsuario().getId());
            ShowAlert.info("Petición rechazada", "La petición ha quedado cancelada.");
            cargarContratos();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnRescindir(ActionEvent event) {
        Contrato c = seleccionadoEn(EstadoContrato.ACTIVO, "Solo se pueden rescindir contratos activos.");
        if (c == null || !ShowAlert.confirmar("Rescindir contrato", "¿Seguro que deseas rescindir el contrato de "
                + c.getNombreCliente() + "?")) {
            return;
        }
        try {
            dao.rescindirContrato(c.getId(), Sesion.getUsuario().getId());
            ShowAlert.info("Contrato rescindido", "El contrato ha quedado cancelado.");
            cargarContratos();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
