package controlador;

import daoempleado.ContratoDAO;
import daoempleado.implementacion.ContratoDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;
import modelo.Empleado;

/**
 * Validar tarifas: aceptar las peticiones pendientes.
 */
public class EmpleadoValidarController {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoValidarController.class.getName());

    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();
    private Empleado empleado;

    @FXML private TableView<Contrato> tablaPeticiones;
    @FXML private TableColumn<Contrato, String> colPeticionCliente;
    @FXML private TableColumn<Contrato, String> colPeticionTarifa;
    @FXML private TableColumn<Contrato, String> colPeticionPrecio;

    @FXML
    public void initialize() {
        empleado = (Empleado) Sesion.getUsuario();
        colPeticionCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPeticionTarifa.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colPeticionPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTexto"));
        cargarPeticiones();
    }

    private void cargarPeticiones() {
        try {
            tablaPeticiones.setItems(FXCollections.observableArrayList(contratoDAO.obtenerContratosPorEstado(EstadoContrato.PENDIENTE)));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnValidarTarifa(ActionEvent event) {
        Contrato peticion = tablaPeticiones.getSelectionModel().getSelectedItem();
        if (peticion == null) {
            ShowAlert.info("Aviso", "Selecciona una petición de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Validar tarifa", "¿Validar y activar esta tarifa para el cliente seleccionado?")) {
            return;
        }
        try {
            contratoDAO.validarContrato(peticion.getId(), empleado.getId());
            ShowAlert.info("Tarifa validada", "La tarifa se ha validado y el contrato está activo.");
            cargarPeticiones();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
