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
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import modelo.TicketSoporte;

/**
 * Tickets de soporte de todos los clientes, filtrados por estado.
 */
public class TicketsSoporteController {

    private static final Logger LOGGER = Logger.getLogger(TicketsSoporteController.class.getName());
    private static final String TODOS = "Todos";

    private final DAO dao = Sesion.getDao();

    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<TicketSoporte> tablaTickets;
    @FXML private TableColumn<TicketSoporte, String> colCliente;
    @FXML private TableColumn<TicketSoporte, String> colAsunto;
    @FXML private TableColumn<TicketSoporte, String> colFecha;
    @FXML private TableColumn<TicketSoporte, String> colEstado;
    @FXML private TextArea txtDescripcion;

    @FXML
    public void initialize() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(TODOS, "pendiente", "resuelto"));
        cmbFiltroEstado.setValue("pendiente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacionTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cargarTickets();
    }

    private void cargarTickets() {
        String estado = cmbFiltroEstado.getValue();
        List<TicketSoporte> filtrados = new ArrayList<>();
        try {
            for (TicketSoporte t : dao.obtenerTodosLosTickets()) {
                if (TODOS.equals(estado) || t.getEstado().equals(estado)) {
                    filtrados.add(t);
                }
            }
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
        tablaTickets.setItems(FXCollections.observableArrayList(filtrados));
        txtDescripcion.clear();
    }

    @FXML
    public void cmbFiltrar(ActionEvent event) {
        cargarTickets();
    }

    @FXML
    public void seleccionarTicket(MouseEvent event) {
        TicketSoporte t = tablaTickets.getSelectionModel().getSelectedItem();
        if (t != null) {
            txtDescripcion.setText(t.getDescripcion());
        }
    }

    @FXML
    public void btnResolver(ActionEvent event) {
        TicketSoporte t = tablaTickets.getSelectionModel().getSelectedItem();
        if (t == null) {
            ShowAlert.info("Aviso", "Selecciona un ticket de la tabla primero.");
            return;
        }
        if (!"pendiente".equals(t.getEstado())) {
            ShowAlert.info("Aviso", "Ese ticket ya está resuelto.");
            return;
        }
        if (!ShowAlert.confirmar("Actualizar ticket", "¿Deseas cambiar el estado de este ticket a 'Resuelto'?")) {
            return;
        }
        try {
            dao.resolverTicket(t.getId(), Sesion.getUsuario().getId());
            ShowAlert.info("Ticket resuelto", "El ticket se ha marcado como resuelto.");
            cargarTickets();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
