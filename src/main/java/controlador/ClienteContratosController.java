package controlador;

import daocliente.ContratoDAO;
import daocliente.implementacion.ContratoDAOImplementacion;
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
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;

/**
 * Mis Contratos: firmar o pedir la baja.
 */
public class ClienteContratosController {

    private static final Logger LOGGER = Logger.getLogger(ClienteContratosController.class.getName());

    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();
    private Cliente cliente;

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, String> colContratoIdTarifa;
    @FXML private TableColumn<Contrato, String> colContratoTelefono;
    @FXML private TableColumn<Contrato, EstadoContrato> colContratoEstado;
    @FXML private TableColumn<Contrato, String> colContratoFecha;

    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        colContratoTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colContratoEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colContratoFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFinalizacionTexto"));
        colContratoIdTarifa.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        cargarContratos();
    }

    private void cargarContratos() {
        try {
            tablaContratos.setItems(FXCollections.observableArrayList(contratoDAO.obtenerContratosCliente(cliente.getId())));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnFirmarContrato(ActionEvent event) {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();
        if (contrato == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        // El contrato queda firmado cuando un empleado valida la tarifa (pasa a "activo")
        if (contrato.getEstado() == EstadoContrato.ACTIVO) {
            ShowAlert.info("Contrato firmado", "Este contrato ya está firmado y activo.");
        } else if (contrato.getEstado() == EstadoContrato.PENDIENTE) {
            ShowAlert.info("Contrato pendiente", "Este contrato se firmará cuando un empleado valide la tarifa.");
        } else {
            ShowAlert.info("Aviso", "Este contrato no se puede firmar porque está " + contrato.getEstado() + ".");
        }
    }

    @FXML
    public void btnRescindirContrato(ActionEvent event) {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();
        if (contrato == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        if (contrato.getEstado() != EstadoContrato.ACTIVO) {
            ShowAlert.info("Aviso", "Solo se puede pedir la baja de un contrato activo.");
            return;
        }
        if (!ShowAlert.confirmar("Baja de compañía", "¿Estás seguro de solicitar la baja definitiva y rescindir tu contrato?")) {
            return;
        }
        try {
            // Queda en "baja pendiente" hasta que un empleado la apruebe
            contratoDAO.solicitarBaja(contrato.getId());
            ShowAlert.info("Solicitud enviada", "Tu solicitud de baja está pendiente de aprobar por un empleado.");
            cargarContratos();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
