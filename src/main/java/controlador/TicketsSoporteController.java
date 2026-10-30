package controlador;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import modelo.TicketSoporte;

public class TicketsSoporteController implements Initializable {

    @FXML
    private ComboBox<String> comboFiltroEstado;
    @FXML
    private ComboBox<String> comboNuevoEstado;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Estados del filtro
        comboFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "pendiente", "en proceso", "resuelto", "cerrado"));
        comboFiltroEstado.setValue("pendiente");

        // Estados a los que se puede cambiar
        comboNuevoEstado.setItems(FXCollections.observableArrayList(
                "en proceso", "resuelto", "cerrado"));

        // Columnas
        colIdTicket.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));

        txtDescripcion.setEditable(false);

        cargarTickets();
    }

    // Carga los tickets en la tabla según el filtro
    private void cargarTickets() {
        String estado = comboFiltroEstado.getValue();
        List<TicketSoporte> lista = DAO.listarTicket(estado);
        tablaTickets.setItems(FXCollections.observableArrayList(lista));
        txtDescripcion.clear();
    }

    // Se ejecuta al elegir un estado en el filtro
    @FXML
    private void filtrarTickets(ActionEvent event) {
        cargarTickets();
    }

    // Se ejecuta al hacer clic en la tabla: enseña la descripción
    @FXML
    private void seleccionarTicket(MouseEvent event) {
        TicketSoporte t = tablaTickets.getSelectionModel().getSelectedItem();
        if (t != null) {
            txtDescripcion.setText(t.getDescripcion());
        }
    }

    // Asignar el empleado se hace cargo del ticket
    @FXML
    private void asignarTicket(ActionEvent event) {
        TicketSoporte t = tablaTickets.getSelectionModel().getSelectedItem();

        if (t == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecciona un ticket.");
            return;
        }

        boolean ok = DAO.asignarTicket(t, IniciarSesionController.idUsuarioActual);
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Te has hecho cargo del ticket.");
            cargarTickets();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo asignar el ticket.");
        }
    }

    // Botón cambiar estado
    @FXML
    private void cambiarEstado(ActionEvent event) {
        TicketSoporte t = tablaTickets.getSelectionModel().getSelectedItem();
        String nuevoEstado = comboNuevoEstado.getValue();

        if (t == null || nuevoEstado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecciona un ticket y un estado.");
            return;
        }

        boolean ok = DAO.cambiarEstadoTicket(t, nuevoEstado);
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Estado cambiado.");
            cargarTickets();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cambiar el estado.");
        }
    }

    // Botón refrescar
    @FXML
    private void refrescar(ActionEvent event) {
        comboFiltroEstado.setValue("pendiente");
        cargarTickets();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
