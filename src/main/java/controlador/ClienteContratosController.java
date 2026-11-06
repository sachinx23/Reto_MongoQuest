package controlador;

import daocliente.ContratoDAO;
import daocliente.implementacion.ContratoDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelocliente.Cliente;
import modelocliente.Contrato;
import modelocliente.Contrato.EstadoContrato;

/**
 * Mis Contratos: firmar o pedir la baja.
 */
public class ClienteContratosController {
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();
    private Cliente cliente;

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, String> colContratoIdTarifa;
    @FXML private TableColumn<Contrato, String> colContratoTelefono;
    @FXML private TableColumn<Contrato, EstadoContrato> colContratoEstado;
    @FXML private TableColumn<Contrato, String> colContratoFecha;

    /**
     * Se ejecuta al abrir la pantalla: prepara la tabla y carga los datos.
     */
    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        colContratoTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colContratoEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colContratoFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFinalizacionTexto"));
        colContratoIdTarifa.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        cargarContratos();
    }

    /**
     * Recarga la tabla con los contratos del cliente.
     */
    private void cargarContratos() {
        try {
            tablaContratos.setItems(FXCollections.observableArrayList(contratoDAO.obtenerContratosCliente(cliente.getId())));
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Informa del estado del contrato elegido (firmado, pendiente de validar o no se puede firmar).
     *
     * @param event evento de la acción (botón, menú o desplegable)
     */
    @FXML
    public void btnFirmarContrato(ActionEvent event) {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();
        if (contrato == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        // Solo se firma cuando un empleado o administrador ha validado la tarifa ("pendiente de firma")
        if (contrato.getEstado() == EstadoContrato.ACTIVO) {
            ShowAlert.info("Contrato firmado", "Este contrato ya está firmado y activo.");
        } else if (contrato.getEstado() == EstadoContrato.PENDIENTE) {
            ShowAlert.info("Contrato pendiente", "Podrás firmar este contrato cuando un empleado valide la tarifa.");
        } else if (contrato.getEstado() == EstadoContrato.PENDIENTE_FIRMA) {
            firmar(contrato);
        } else {
            ShowAlert.info("Aviso", "Este contrato no se puede firmar porque está " + contrato.getEstado() + ".");
        }
    }

    /**
     * Pide confirmación y firma el contrato (pasa a activo).
     *
     * @param contrato contrato pendiente de firma
     */
    private void firmar(Contrato contrato) {
        if (!ShowAlert.confirmar("Firmar contrato", "¿Firmar el contrato de la tarifa " + contrato.getNombreTarifa()
                + "? Empieza hoy y dura un año.")) {
            return;
        }
        try {
            contratoDAO.firmarContrato(contrato);
            ShowAlert.info("Contrato firmado", "Tu contrato ya está activo. Tienes los detalles de la compra en Soporte.");
            cargarContratos();
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Pide la baja del contrato activo elegido, después de confirmarlo.
     *
     * @param event evento de la acción (botón, menú o desplegable)
     */
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
            ShowAlert.error(e.getMessage());
        }
    }
}
