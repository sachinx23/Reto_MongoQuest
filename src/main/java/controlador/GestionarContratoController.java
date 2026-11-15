package controlador;

import java.util.logging.Logger;
import exception.ShowAlert;
import exception.OurException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import modelo.Contrato;
import DAO.ContratoDAO;
import DAO.implementacion.ContratoDAOImplementacion;

/**
 * Controlador de la vista de gestión de contratos para empleados y administradores en JavaFX.
 * Permite consultar y filtrar los contratos registrados por teléfono o estado, así como
 * aprobar peticiones pendientes (pasándolas a {@code "pendiente de firma"}), rechazarlas
 * o rescindir contratos actualmente activos.
 *
 * @author Eneko.Plaza
 */
public class GestionarContratoController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(GestionarContratoController.class.getName());

    /** Campo de texto utilizado para filtrar los contratos por número de teléfono. */
    @FXML
    private TextField txtFiltroTelefono;

    /** Selector desplegable utilizado para filtrar los contratos según su estado. */
    @FXML
    private ComboBox<String> comboFiltroEstado;

    /** Tabla principal donde se muestran los contratos del sistema. */
    @FXML
    private TableView<Contrato> tablaContratos;

    /** Columna de la tabla que muestra el identificador único del contrato. */
    @FXML
    private TableColumn<Contrato, String> colIdContrato;

    /** Columna de la tabla que muestra el teléfono asociado al contrato. */
    @FXML
    private TableColumn<Contrato, String> colTelefono;

    /** Columna de la tabla que muestra el identificador del cliente titular. */
    @FXML
    private TableColumn<Contrato, String> colIdCliente;

    /** Columna de la tabla que muestra el identificador de la tarifa contratada. */
    @FXML
    private TableColumn<Contrato, String> colIdTarifa;

    /** Columna de la tabla que muestra el identificador del empleado que gestionó el contrato. */
    @FXML
    private TableColumn<Contrato, String> colIdEmpleado;

    /** Columna de la tabla que refleja el estado actual del contrato. */
    @FXML
    private TableColumn<Contrato, String> colEstado;

    /** Columna de la tabla que muestra la fecha de inicio de vigencia del contrato. */
    @FXML
    private TableColumn<Contrato, String> colFechaInicio;

    /** Columna de la tabla que muestra la fecha de finalización del contrato. */
    @FXML
    private TableColumn<Contrato, String> colFechaFin;

    /** Botón para aprobar una solicitud de contrato en estado {@code "pendiente"}. */
    @FXML
    private Button btnAceptarPeticion;

    /** Botón para denegar una solicitud de contrato en estado {@code "pendiente"}. */
    @FXML
    private Button btnRechazarPeticion;

    /** Botón para dar de baja o rescindir un contrato que se encuentra {@code "activo"}. */
    @FXML
    private Button btnRescindirContrato;

    /** Botón para restablecer los filtros de búsqueda y recargar la tabla completa. */
    @FXML
    private Button btnRefrescar;

    /** Referencia al {@link Contrato} actualmente seleccionado en {@code tablaContratos}. */
    private Contrato contratoSeleccionado;

    /** Objeto de acceso a datos para realizar consultas y cambios de estado sobre los contratos. */
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Carga las opciones de estado en el filtro desplegable, configura las factorías
     * de celdas de las columnas de {@code tablaContratos}, carga el listado inicial
     * y registra un listener de selección para habilitar o deshabilitar los botones de acción.
     *
     * @param url Ubicación utilizada para resolver rutas relativas del objeto raíz, o {@code null} si no se conoce.
     * @param rb  Recursos utilizados para localizar el objeto raíz, o {@code null} si no se localizó.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> estados = FXCollections.observableArrayList(
                "Todos",
                "pendiente",
                "pendiente de firma",
                "activo",
                "rescindido",
                "caducado",
                "rechazado"
        );
        comboFiltroEstado.setItems(estados);
        comboFiltroEstado.setValue("Todos");

        colIdContrato.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colIdTarifa.setCellValueFactory(new PropertyValueFactory<>("idTarifa"));
        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaComienzo"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));

        cargarContratos();

        tablaContratos.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Contrato>() {
            @Override
            public void changed(ObservableValue<? extends Contrato> obs, Contrato oldSel, Contrato newSel) {
                contratoSeleccionado = newSel;
                actualizarEstadoBotones();
            }
        });
    }

    /**
     * Recupera todos los contratos desde la base de datos mediante {@link ContratoDAO#listarContratos()},
     * los carga en {@code tablaContratos} y reinicia la selección actual.
     */
    public void cargarContratos() {
        try {
            List<Contrato> lista = contratoDAO.listarContratos();
            if (lista != null) {
                ObservableList<Contrato> datos = FXCollections.observableArrayList(lista);
                tablaContratos.setItems(datos);
            }
            limpiarSeleccion();
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Filtra los contratos mostrados en la tabla en función del teléfono introducido
     * y el estado seleccionado en {@code comboFiltroEstado}.
     *
     * @param event Evento de acción disparado al ejecutar el filtrado.
     */
    @FXML
    private void filtrarContratos(ActionEvent event) {
        try {
            String tel = txtFiltroTelefono.getText();
            String est = comboFiltroEstado.getValue();
            List<Contrato> filtrados = contratoDAO.filtrarContratos(tel, est);
            tablaContratos.setItems(FXCollections.observableArrayList(filtrados));
            limpiarSeleccion();
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Aprueba la solicitud del contrato seleccionado asignándole el identificador del usuario
     * autenticado ({@link IniciarSesionController#idUsuarioActual}) y cambiando su estado
     * a {@code "pendiente de firma"}.
     *
     * @param event Evento de acción disparado al pulsar el botón de aceptar petición.
     */
    @FXML
    private void aceptarPeticion(ActionEvent event) {
        try {
            if (contratoSeleccionado != null) {
                if (contratoDAO.aprobarContrato(contratoSeleccionado, IniciarSesionController.idUsuarioActual)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa confirmada. Contrato pendiente de firma por el cliente.");
                    cargarContratos();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo confirmar la tarifa.");
                }
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Rechaza la solicitud del contrato seleccionado mediante {@link ContratoDAO#rechazarContratos(Contrato)}
     * y recarga el listado de contratos en la tabla.
     *
     * @param event Evento de acción disparado al pulsar el botón de rechazar petición.
     */
    @FXML
    private void rechazarPeticion(ActionEvent event) {
        try {
            if (contratoSeleccionado != null) {
                if (contratoDAO.rechazarContratos(contratoSeleccionado)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contrato rechazado.");
                    cargarContratos();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo rechazar el contrato.");
                }
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Rescinde el contrato activo seleccionado a través de {@link ContratoDAO#rescindirContrato(Contrato)}
     * y actualiza los datos visibles en la tabla.
     *
     * @param event Evento de acción disparado al pulsar el botón de rescindir contrato.
     */
    @FXML
    private void rescindirContrato(ActionEvent event) {
        try {
            if (contratoSeleccionado != null) {
                if (contratoDAO.rescindirContrato(contratoSeleccionado)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Contrato rescindido correctamente.");
                    cargarContratos();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo rescindir el contrato.");
                }
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Limpia el campo de búsqueda por teléfono, restablece el filtro de estado a {@code "Todos"}
     * y vuelve a cargar todos los contratos desde la base de datos.
     *
     * @param event Evento de acción disparado al pulsar el botón de refrescar.
     */
    @FXML
    private void refrescarFiltro(ActionEvent event) {
        txtFiltroTelefono.clear();
        comboFiltroEstado.setValue("Todos");
        cargarContratos();
    }

    /**
     * Deselecciona cualquier fila marcada en {@code tablaContratos}, limpia la referencia
     * {@code contratoSeleccionado} y actualiza la disponibilidad de los botones de acción.
     */
    private void limpiarSeleccion() {
        tablaContratos.getSelectionModel().clearSelection();
        contratoSeleccionado = null;
        actualizarEstadoBotones();
    }

    /**
     * Habilita o deshabilita los botones de aceptar, rechazar y rescindir en función
     * de si hay un contrato seleccionado y de cuál sea su estado ({@code "pendiente"} o {@code "activo"}).
     */
    private void actualizarEstadoBotones() {
        boolean haySeleccion = contratoSeleccionado != null;
        boolean pendiente = false;
        boolean activo = false;

        if (haySeleccion) {
            if ("pendiente".equalsIgnoreCase(contratoSeleccionado.getEstado())) {
                pendiente = true;
            }
            if ("activo".equalsIgnoreCase(contratoSeleccionado.getEstado())) {
                activo = true;
            }
        }

        btnAceptarPeticion.setDisable(!pendiente);
        btnRechazarPeticion.setDisable(!pendiente);
        btnRescindirContrato.setDisable(!activo);
    }

    /**
     * Muestra un cuadro de diálogo modal de tipo {@link Alert} con la severidad, título y mensaje indicados.
     *
     * @param tipo    Tipo de alerta JavaFX ({@link Alert.AlertType}).
     * @param titulo  Título de la ventana de diálogo.
     * @param mensaje Contenido textual que se mostrará al usuario.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}