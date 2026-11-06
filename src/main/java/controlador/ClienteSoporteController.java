package controlador;

import daocliente.TicketDAO;
import daocliente.implementacion.TicketDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.time.LocalDate;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Cliente;
import modelo.TicketSoporte;

/**
 * Soporte: enviar un ticket y ver los míos.
 */
public class ClienteSoporteController {

    private static final Logger LOGGER = Logger.getLogger(ClienteSoporteController.class.getName());

    private final TicketDAO ticketDAO = new TicketDAOImplementacion();
    private Cliente cliente;

    @FXML private TextField txtSoporteAsunto;
    @FXML private TextField txtSoporteMensaje;
    @FXML private TableView<TicketSoporte> tablaSoporte;
    @FXML private TableColumn<TicketSoporte, String> colSoporteAsunto;
    @FXML private TableColumn<TicketSoporte, String> colSoporteFecha;

    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        colSoporteAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colSoporteFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacionTexto"));
        cargarTickets();
    }

    private void cargarTickets() {
        try {
            tablaSoporte.setItems(FXCollections.observableArrayList(ticketDAO.obtenerTicketsCliente(cliente.getId())));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnEnviarTicket(ActionEvent event) {
        String asunto = txtSoporteAsunto.getText().trim();
        String descripcion = txtSoporteMensaje.getText().trim();
        if (asunto.isEmpty() || descripcion.isEmpty()) {
            ShowAlert.info("Campos incompletos", "Rellena el asunto y la descripción del ticket.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar envío", "¿Seguro que deseas enviar este ticket?")) {
            return;
        }
        TicketSoporte ticket = new TicketSoporte(null, asunto, descripcion, LocalDate.now().toString(), "pendiente",
                cliente.getId(), null);
        try {
            ticketDAO.insertarTicket(ticket);
            ShowAlert.info("Ticket enviado", "Tu consulta ha sido enviada al departamento de soporte con éxito.");
            txtSoporteAsunto.clear();
            txtSoporteMensaje.clear();
            cargarTickets();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
