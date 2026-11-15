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

/**
 * Controlador de la vista de contratos del cliente en JavaFX.
 * Permite al cliente autenticado consultar el listado de sus contratos, visualizar
 * el estado y detalles de cada tarifa contratada, firmar contratos aprobados
 * ({@code "pendiente de firma"}) y solicitar la baja de contratos activos.
 *
 * @author Eneko.Plaza
 */
public class ClienteContratosController {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(ClienteContratosController.class.getName());

    /** Objeto de acceso a datos para gestionar las operaciones sobre los contratos. */
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();

    /** Objeto de acceso a datos para consultar la información de las tarifas asociadas. */
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();

    /** Cliente que tiene la sesión iniciada actualmente en la aplicación. */
    private Cliente cliente;

    /** Caché en memoria que asocia cada identificador de tarifa con su objeto {@link Tarifa}. */
    private final Map<String, Tarifa> tarifas = new HashMap<>();

    /** Tabla principal donde se muestran los contratos del cliente. */
    @FXML private TableView<Contrato> tablaContratos;

    /** Columna que muestra el nombre de la tarifa contratada. */
    @FXML private TableColumn<Contrato, String> colContratoIdTarifa;

    /** Columna que muestra el número de teléfono asociado al contrato. */
    @FXML private TableColumn<Contrato, String> colContratoTelefono;

    /** Columna que indica el estado actual del contrato. */
    @FXML private TableColumn<Contrato, String> colContratoEstado;

    /** Columna que refleja la fecha de finalización del contrato. */
    @FXML private TableColumn<Contrato, String> colContratoFecha;

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Obtiene el cliente activo desde {@link ClienteController}, configura las factorías
     * de celdas de las columnas de la tabla (resolviendo el nombre de la tarifa mediante
     * caché) y carga los contratos del usuario.
     */
    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        colContratoTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colContratoEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colContratoFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colContratoIdTarifa.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Contrato, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Contrato, String> fila) {
                return new SimpleStringProperty(nombreTarifa(fila.getValue()));
            }
        });
        cargarContratos();
    }

    /**
     * Recupera los contratos del cliente desde la base de datos, precarga en el mapa
     * {@code tarifas} los datos de las tarifas vinculadas que aún no estén en caché
     * y actualiza la tabla de la interfaz.
     */
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

    /**
     * Manejador del botón para firmar el contrato seleccionado en la tabla.
     * Valida que exista una selección y que su estado sea {@code "pendiente de firma"};
     * en caso contrario, informa al usuario del motivo por el cual no puede firmarse.
     *
     * @param event Evento de acción disparado al pulsar el botón de firmar contrato.
     */
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

    /**
     * Solicita confirmación al cliente y ejecuta la firma del contrato a través de
     * {@link ContratoDAO#firmarContrato(Contrato)}, activándolo y recargando la tabla.
     *
     * @param contrato Objeto {@link Contrato} que se va a firmar y activar.
     */
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

    /**
     * Manejador del botón para solicitar la baja o rescisión del contrato seleccionado.
     * Comprueba que el contrato esté en estado {@code "activo"}, pide confirmación al cliente
     * y registra la solicitud de baja pendiente de revisión.
     *
     * @param event Evento de acción disparado al pulsar el botón de rescindir contrato.
     */
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

    /**
     * Obtiene el nombre legible de la tarifa asociada a un contrato consultando el mapa
     * local {@code tarifas}.
     *
     * @param contrato Objeto {@link Contrato} del cual se desea conocer el nombre de su tarifa.
     * @return El nombre de la tarifa, o una cadena vacía ({@code ""}) si no se encuentra.
     */
    private String nombreTarifa(Contrato contrato) {
        Tarifa tarifa = tarifas.get(contrato.getIdTarifa());
        if (tarifa == null) {
            return "";
        }
        return tarifa.getNombreTarifa();
    }
}