package controlador;

import java.util.logging.Logger;
import exception.ShowAlert;
import exception.OurException;
import DAO.TicketDAO;
import DAO.implementacion.TikcetDAOImpl;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import modelo.TicketSoporte;

/**
 * Controlador de la vista de gestión de tickets de soporte para empleados y administradores en JavaFX.
 * Permite listar y filtrar las incidencias registradas por estado, consultar su descripción
 * detallada, autoasignarse tickets pendientes y actualizar el estado de resolución de los mismos.
 *
 * @author Eneko.Plaza
 */
public class TicketsSoporteController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(TicketsSoporteController.class.getName());

    /** Objeto de acceso a datos para consultar, asignar y actualizar tickets de soporte en MongoDB. */
    private final TicketDAO ticketDAO = new TikcetDAOImpl();

    /** Selector desplegable para filtrar los tickets mostrados según su estado. */
    @FXML
    private ComboBox<String> comboFiltroEstado;

    /** Tabla principal donde se muestra el listado de tickets de soporte. */
    @FXML
    private TableView<TicketSoporte> tablaTickets;

    /** Columna de la tabla que muestra el identificador único del ticket. */
    @FXML
    private TableColumn<TicketSoporte, String> colIdTicket;

    /** Columna de la tabla que muestra el asunto o título breve de la incidencia. */
    @FXML
    private TableColumn<TicketSoporte, String> colAsunto;

    /** Columna de la tabla que muestra la fecha de creación del ticket. */
    @FXML
    private TableColumn<TicketSoporte, String> colFecha;

    /** Columna de la tabla que refleja el estado actual del ticket ({@code pendiente}, {@code en proceso}, {@code cerrado}). */
    @FXML
    private TableColumn<TicketSoporte, String> colEstado;

    /** Columna de la tabla que muestra el identificador del cliente que abrió el ticket. */
    @FXML
    private TableColumn<TicketSoporte, String> colIdCliente;

    /** Columna de la tabla que muestra el identificador del empleado asignado al ticket. */
    @FXML
    private TableColumn<TicketSoporte, String> colIdEmpleado;

    /** Área de texto donde se visualiza la descripción completa del ticket seleccionado. */
    @FXML
    private TextArea txtDescripcion;

    /** Selector desplegable utilizado para elegir el nuevo estado a aplicar sobre el ticket seleccionado. */
    @FXML
    private ComboBox<String> comboNuevoEstado;

    /** Lista observable que respalda los elementos mostrados en {@code tablaTickets}. */
    private final ObservableList<TicketSoporte> listaTickets = FXCollections.observableArrayList();

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Vincula las columnas de {@code tablaTickets} con las propiedades de {@link TicketSoporte},
     * rellena los desplegables de filtrado y cambio de estado, y carga el listado inicial de tickets.
     *
     * @param url Ubicación utilizada para resolver rutas relativas del objeto raíz, o {@code null} si no se conoce.
     * @param rb  Recursos utilizados para localizar el objeto raíz, o {@code null} si no se localizó.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colIdTicket.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));

        tablaTickets.setItems(listaTickets);

        comboFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "pendiente", "en proceso", "cerrado"));
        comboFiltroEstado.getSelectionModel().select("Todos");

        comboNuevoEstado.setItems(FXCollections.observableArrayList("pendiente", "en proceso", "cerrado"));

        cargarTickets();
    }

    /**
     * Consulta los tickets de soporte desde MongoDB aplicando el filtro de estado activo
     * en {@code comboFiltroEstado} (por defecto {@code "Todos"}) y actualiza {@code listaTickets}.
     */
    private void cargarTickets() {
        try {
            listaTickets.clear();
            String estadoFiltro = comboFiltroEstado.getValue() != null ? comboFiltroEstado.getValue() : "Todos";

            List<TicketSoporte> tickets = ticketDAO.listarTicket(estadoFiltro);

            if (tickets != null) {
                listaTickets.addAll(tickets);
            }

        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador de eventos del selector {@code comboFiltroEstado} que recarga la tabla
     * filtrando los tickets por el estado elegido.
     *
     * @param event Evento de acción disparado al cambiar la selección del filtro de estado.
     */
    @FXML
    public void filtrarTickets(ActionEvent event) {
        cargarTickets();
    }

    /**
     * Manejador del botón de refrescar que vuelve a consultar y actualizar el listado de tickets.
     *
     * @param event Evento de acción disparado al pulsar el botón de refrescar.
     */
    @FXML
    public void btnRefrescar(ActionEvent event) {
        cargarTickets();
    }

    /**
     * Manejador del evento de clic sobre {@code tablaTickets} que muestra la descripción
     * del ticket seleccionado en {@code txtDescripcion} y sincroniza su estado actual en
     * {@code comboNuevoEstado}.
     *
     * @param event Evento de ratón disparado al seleccionar una fila de la tabla.
     */
    @FXML
    public void seleccionarTicket(MouseEvent event) {
        TicketSoporte seleccionado = tablaTickets.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtDescripcion.setText(seleccionado.getDescripcion());
            comboNuevoEstado.getSelectionModel().select(seleccionado.getEstado());
        }
    }

    /**
     * Manejador del botón para asignar el ticket seleccionado en la tabla al empleado o
     * administrador autenticado ({@link IniciarSesionController#idUsuarioActual}), cambiando
     * su estado a {@code "en proceso"} y recargando la tabla.
     *
     * @param event Evento de acción disparado al pulsar el botón de asignar ticket.
     */
    @FXML
    public void asignarTicket(ActionEvent event) {
        try {
            TicketSoporte seleccionado = tablaTickets.getSelectionModel().getSelectedItem();
            String idEmpleadoActual = IniciarSesionController.idUsuarioActual;

            if (seleccionado != null) {
                boolean asignado = ticketDAO.asignarTicket(seleccionado, idEmpleadoActual);
                if (asignado) {
                    mostrarAlerta("Éxito", "Te has asignado el ticket correctamente.");
                    cargarTickets();
                }
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador del botón para actualizar el estado del ticket seleccionado utilizando
     * el valor elegido en {@code comboNuevoEstado}. Valida que exista tanto una fila
     * seleccionada como un estado válido antes de persistir el cambio.
     *
     * @param event Evento de acción disparado al pulsar el botón de cambiar estado.
     */
    @FXML
    public void cambiarEstado(ActionEvent event) {
        try {
            TicketSoporte seleccionado = tablaTickets.getSelectionModel().getSelectedItem();
            String nuevoEstado = comboNuevoEstado.getValue();

            if (seleccionado == null) {
                mostrarAlerta("Atención", "Selecciona primero un ticket de la tabla.");
                return;
            }

            if (nuevoEstado == null || nuevoEstado.isEmpty()) {
                mostrarAlerta("Atención", "Selecciona un nuevo estado en el desplegable.");
                return;
            }

            boolean cambiado = ticketDAO.cambiarEstadoTicket(seleccionado, nuevoEstado);
            if (cambiado) {
                mostrarAlerta("Éxito", "El estado del ticket ha sido actualizado a: " + nuevoEstado);
                cargarTickets();
            } else {
                mostrarAlerta("Error", "No se pudo cambiar el estado del ticket.");
            }

        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Muestra un cuadro de diálogo modal informativo ({@link AlertType#INFORMATION})
     * con el título y mensaje especificados.
     *
     * @param titulo Título de la ventana de diálogo.
     * @param texto  Contenido textual que se mostrará al usuario.
     */
    private void mostrarAlerta(String titulo, String texto) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}