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

/**
 * Controlador de la subvista de soporte técnico del cliente en JavaFX.
 * Permite al cliente autenticado consultar el historial de sus tickets de soporte
 * y crear nuevas consultas o incidencias dirigidas al departamento de atención.
 *
 * @author Eneko.Plaza
 */
public class ClienteSoporteController {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(ClienteSoporteController.class.getName());

    /** Objeto de acceso a datos para gestionar la creación y consulta de tickets de soporte. */
    private final TicketDAO ticketDAO = new TikcetDAOImpl();

    /** Instancia del cliente que tiene la sesión iniciada actualmente. */
    private Cliente cliente;

    /** Campo de texto para introducir el asunto o título del ticket de soporte. */
    @FXML private TextField txtSoporteAsunto;

    /** Campo de texto para redactar la descripción detallada del mensaje o incidencia. */
    @FXML private TextField txtSoporteMensaje;

    /** Tabla donde se muestra el listado de tickets creados por el cliente. */
    @FXML private TableView<TicketSoporte> tablaSoporte;

    /** Columna de la tabla que muestra el asunto de cada ticket. */
    @FXML private TableColumn<TicketSoporte, String> colSoporteAsunto;

    /** Columna de la tabla que muestra la fecha de creación de cada ticket. */
    @FXML private TableColumn<TicketSoporte, String> colSoporteFecha;

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Obtiene el cliente activo desde {@link ClienteController}, vincula las columnas
     * de la tabla con las propiedades de {@link TicketSoporte} y carga los tickets del usuario.
     */
    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        colSoporteAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colSoporteFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        cargarTickets();
    }

    /**
     * Consulta los tickets de soporte pertenecientes al cliente autenticado y actualiza
     * los elementos visibles en {@code tablaSoporte}.
     */
    private void cargarTickets() {
        try {
            tablaSoporte.setItems(FXCollections.observableArrayList(ticketDAO.listarTicketsCliente(cliente.getId())));
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador del botón para enviar un nuevo ticket de soporte.
     * Valida que los campos de asunto y descripción no estén vacíos, solicita confirmación
     * al cliente, registra el ticket mediante {@link TicketDAO#crearTickets(String, String, String)},
     * limpia el formulario y refresca la tabla de tickets.
     *
     * @param event Evento de acción disparado al pulsar el botón de enviar ticket.
     */
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