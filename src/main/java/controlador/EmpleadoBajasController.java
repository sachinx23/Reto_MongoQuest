package controlador;

import daoempleado.ClienteDAO;
import daoempleado.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Cliente;
import modelo.Empleado;

/**
 * Solicitudes de baja: aprobarlas.
 */
public class EmpleadoBajasController {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoBajasController.class.getName());

    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();
    private Empleado empleado;

    @FXML private TableView<Cliente> tablaBajas;
    @FXML private TableColumn<Cliente, String> colBajaCliente;
    @FXML private TableColumn<Cliente, String> colBajaDni;

    @FXML
    public void initialize() {
        empleado = (Empleado) Sesion.getUsuario();
        colBajaCliente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colBajaDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        cargarBajas();
    }

    private void cargarBajas() {
        try {
            tablaBajas.setItems(FXCollections.observableArrayList(clienteDAO.obtenerClientesConBajaPendiente()));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnAprobarBaja(ActionEvent event) {
        Cliente cliente = tablaBajas.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            ShowAlert.info("Aviso", "Selecciona una solicitud de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Tramitar baja", "¿Confirmas la rescisión de contrato y la baja definitiva de este cliente?")) {
            return;
        }
        try {
            clienteDAO.aprobarBaja(cliente.getId(), empleado.getId());
            ShowAlert.info("Gestión completada", "La operación sobre la cuenta del cliente se ha procesado con éxito.");
            cargarBajas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
