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
 * FXML Controller class para Tickets de Soporte
 * @author Eneko.Plaza
 */
public class TicketsSoporteController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TicketsSoporteController.class.getName());

    private final TicketDAO ticketDAO = new TikcetDAOImpl();

    @FXML
    private ComboBox<String> comboFiltroEstado;

    @FXML
    private TableView<TicketSoporte> tablaTickets;

    @FXML
    private TableColumn<TicketSoporte, String> colIdTicket;

    @FXML
    private TableColumn<TicketSoporte, String> colAsunto;

    @FXML
    private TableColumn<TicketSoporte, String> colFecha;

    @FXML
    private TableColumn<TicketSoporte, String> colEstado;

    @FXML
    private TableColumn<TicketSoporte, String> colIdCliente;

    @FXML
    private TableColumn<TicketSoporte, String> colIdEmpleado;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private ComboBox<String> comboNuevoEstado;

    private final ObservableList<TicketSoporte> listaTickets = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Enlace de columnas con la clase modelo TicketSoporte
        colIdTicket.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));

        tablaTickets.setItems(listaTickets);

        // Inicializar ComboBoxes
        comboFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "pendiente", "en proceso", "cerrado"));
        comboFiltroEstado.getSelectionModel().select("Todos");

        comboNuevoEstado.setItems(FXCollections.observableArrayList("pendiente", "en proceso", "cerrado"));

        cargarTickets();
    }

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

    @FXML
    public void filtrarTickets(ActionEvent event) {
        cargarTickets();
    }

    @FXML
    public void refrescar(ActionEvent event) {
        cargarTickets();
    }

    @FXML
    public void seleccionarTicket(MouseEvent event) {
        TicketSoporte seleccionado = tablaTickets.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtDescripcion.setText(seleccionado.getDescripcion());
            comboNuevoEstado.getSelectionModel().select(seleccionado.getEstado());
        }
    }

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
                } else {
                    mostrarAlerta("Error", "No se pudo asignar el ticket.");
                }
            } else {
                mostrarAlerta("Atención", "Selecciona primero un ticket de la tabla.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

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

    private void mostrarAlerta(String titulo, String texto) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}