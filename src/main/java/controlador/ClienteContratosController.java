package controlador;

import java.util.logging.Logger;
import DAO.ContratoDAO;
import DAO.implementacion.ContratoDAOImplementacion;
import DAO.TarifaDAO;
import DAO.implementacion.TarifaDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Tarifa;

public class ClienteContratosController {

    private static final Logger LOGGER = Logger.getLogger(ClienteContratosController.class.getName());
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();
    private Cliente cliente;

    // Tarifa de cada contrato por su id
    private final Map<String, Tarifa> tarifas = new HashMap<>();

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, String> colContratoIdTarifa;
    @FXML private TableColumn<Contrato, String> colContratoTelefono;
    @FXML private TableColumn<Contrato, String> colContratoEstado;
    @FXML private TableColumn<Contrato, String> colContratoFecha;

    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        colContratoTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colContratoEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colContratoFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        // Nombre de la tarifa
        colContratoIdTarifa.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Contrato, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Contrato, String> fila) {
                return new SimpleStringProperty(nombreTarifa(fila.getValue()));
            }
        });
        cargarContratos();
    }

    private void cargarContratos() {
        try {
            List<Contrato> contratos = contratoDAO.obtenerContratosCliente(cliente.getId());
            for (Contrato c : contratos) {
                if (c.getIdTarifa() != null && !tarifas.containsKey(c.getIdTarifa())) {
                    tarifas.put(c.getIdTarifa(), tarifaDAO.buscarTarifa(c.getIdTarifa()));
                }
            }
            tablaContratos.setItems(FXCollections.observableArrayList(contratos));
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void btnFirmarContrato(ActionEvent event) {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();
        if (contrato == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        if ("activo".equals(contrato.getEstado())) {
            ShowAlert.info("Contrato firmado", "Este contrato ya está firmado y activo.");
        } else if ("pendiente".equals(contrato.getEstado())) {
            ShowAlert.info("Contrato pendiente", "Podrás firmar este contrato cuando un empleado valide la tarifa.");
        } else if ("pendiente de firma".equals(contrato.getEstado())) {
            firmar(contrato);
        } else {
            ShowAlert.info("Aviso", "Este contrato no se puede firmar porque está " + contrato.getEstado() + ".");
        }
    }

    private void firmar(Contrato contrato) {
        if (!ShowAlert.confirmar("Firmar contrato", "¿Firmar el contrato de la tarifa " + nombreTarifa(contrato)
                + "? Empieza hoy.")) {
            return;
        }
        try {
            contratoDAO.firmarContrato(contrato);
            ShowAlert.info("Contrato firmado", "Tu contrato ya está activo. Tienes los detalles de la compra en Soporte.");
            cargarContratos();
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void btnRescindirContrato(ActionEvent event) {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();
        if (contrato == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        if (!"activo".equals(contrato.getEstado())) {
            ShowAlert.info("Aviso", "Solo se puede pedir la baja de un contrato activo.");
            return;
        }
        if (!ShowAlert.confirmar("Baja de compañía", "¿Estás seguro de solicitar la baja definitiva y rescindir tu contrato?")) {
            return;
        }
        try {
            contratoDAO.solicitarBaja(contrato.getId());
            ShowAlert.info("Solicitud enviada", "Tu solicitud de baja está pendiente de aprobar por un empleado.");
            cargarContratos();
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private String nombreTarifa(Contrato contrato) {
        Tarifa tarifa = tarifas.get(contrato.getIdTarifa());
        if (tarifa == null) {
            return "";
        }
        return tarifa.getNombreTarifa();
    }
}
