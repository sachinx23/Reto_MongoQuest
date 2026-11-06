package controlador;

import daoempleado.TicketDAO;
import daoempleado.implementacion.TicketDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Empleado;
import modelo.TicketSoporte;

/**
 * Tickets pendientes: resolverlos.
 */
public class EmpleadoTicketsController {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoTicketsController.class.getName());

    private final TicketDAO ticketDAO = new TicketDAOImplementacion();
    private Empleado empleado;

    @FXML private TableView<TicketSoporte> tablaTickets;
    @FXML private TableColumn<TicketSoporte, String> colTicketCliente;
    @FXML private TableColumn<TicketSoporte, String> colTicketAsunto;
    @FXML private TableColumn<TicketSoporte, String> colTicketDescripcion;
    @FXML private TableColumn<TicketSoporte, String> colTicketFecha;

    @FXML
    public void initialize() {
        empleado = (Empleado) Sesion.getUsuario();
        colTicketCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colTicketAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colTicketDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colTicketFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacionTexto"));
        cargarTickets();
    }

    private void cargarTickets() {
        try {
            tablaTickets.setItems(FXCollections.observableArrayList(ticketDAO.obtenerTicketsPendientes()));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnResolverTicket(ActionEvent event) {
        TicketSoporte ticket = tablaTickets.getSelectionModel().getSelectedItem();
        if (ticket == null) {
            ShowAlert.info("Aviso", "Selecciona un ticket de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Actualizar ticket", "¿Deseas cambiar el estado de este ticket a 'Resuelto'?")) {
            return;
        }
        try {
            ticketDAO.resolverTicket(ticket.getId(), empleado.getId());
            ShowAlert.info("Ticket resuelto", "El ticket se ha marcado como resuelto.");
            cargarTickets();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
