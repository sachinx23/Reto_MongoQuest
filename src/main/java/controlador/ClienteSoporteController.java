package controlador;

import java.util.logging.Logger;
import DAO.TicketDAO;
import DAO.implementacion.TikcetDAOImpl;
import exception.OurException;
import exception.ShowAlert;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Cliente;
import modelo.TicketSoporte;

public class ClienteSoporteController {

    private static final Logger LOGGER = Logger.getLogger(ClienteSoporteController.class.getName());
    private final TicketDAO ticketDAO = new TikcetDAOImpl();
    private Cliente cliente;

    @FXML private TextField txtSoporteAsunto;
    @FXML private TextField txtSoporteMensaje;
    @FXML private TableView<TicketSoporte> tablaSoporte;
    @FXML private TableColumn<TicketSoporte, String> colSoporteAsunto;
    @FXML private TableColumn<TicketSoporte, String> colSoporteFecha;

    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        colSoporteAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colSoporteFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        cargarTickets();
    }

    private void cargarTickets() {
        try {
            tablaSoporte.setItems(FXCollections.observableArrayList(ticketDAO.listarTicketsCliente(cliente.getId())));
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
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
        try {
            ticketDAO.crearTickets(cliente.getId(), asunto, descripcion);
            ShowAlert.info("Ticket enviado", "Tu consulta ha sido enviada al departamento de soporte con éxito.");
            txtSoporteAsunto.clear();
            txtSoporteMensaje.clear();
            cargarTickets();
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }
}
