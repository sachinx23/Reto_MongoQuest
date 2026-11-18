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
import DAO.MongoDBConexion;
import DAO.implementacion.ContratoDAOImplementacion;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Controlador de la vista de gestión de contratos en JavaFX.
 * Permite listar, filtrar, aceptar, rechazar y rescindir contratos,
 * mostrando los nombres de clientes, tarifas y gestores en lugar de sus IDs.
 *
 * @author Eneko.Plaza
 */
public class GestionarContratoController implements Initializable {

    /** Registrador de eventos para trazas y errores. */
    private static final Logger LOGGER = Logger.getLogger(GestionarContratoController.class.getName());

    /** Campo de texto para filtrar por teléfono. */
    @FXML
    private TextField txtFiltroTelefono;

    /** Desplegable para filtrar por estado del contrato. */
    @FXML
    private ComboBox<String> comboFiltroEstado;

    /** Tabla principal de contratos. */
    @FXML
    private TableView<Contrato> tablaContratos;

    /** Columna con el ID del contrato. */
    @FXML
    private TableColumn<Contrato, String> colIdContrato;

    /** Columna con el teléfono del contrato. */
    @FXML
    private TableColumn<Contrato, String> colTelefono;

    /** Columna con el nombre del cliente. */
    @FXML
    private TableColumn<Contrato, String> colIdCliente;

    /** Columna con el nombre/tipo de la tarifa. */
    @FXML
    private TableColumn<Contrato, String> colIdTarifa;

    /** Columna con el empleado o admin que aceptó el contrato. */
    @FXML
    private TableColumn<Contrato, String> colIdEmpleado;

    /** Columna con el estado del contrato. */
    @FXML
    private TableColumn<Contrato, String> colEstado;

    /** Columna con la fecha de inicio. */
    @FXML
    private TableColumn<Contrato, String> colFechaInicio;

    /** Columna con la fecha de fin. */
    @FXML
    private TableColumn<Contrato, String> colFechaFin;

    /** Botón para aceptar una petición pendiente. */
    @FXML
    private Button btnAceptarPeticion;

    /** Botón para rechazar una petición pendiente. */
    @FXML
    private Button btnRechazarPeticion;

    /** Botón para rescindir un contrato activo. */
    @FXML
    private Button btnRescindirContrato;

    /** Botón para limpiar los filtros y recargar la tabla. */
    @FXML
    private Button btnRefrescar;

    /** Contrato seleccionado actualmente en la tabla. */
    private Contrato contratoSeleccionado;

    /** DAO para acceder a los contratos en MongoDB. */
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();

    /**
     * Inicializa la vista, rellena el desplegable de estados, enlaza las
     * columnas de la tabla y carga los contratos iniciales.
     *
     * @param url Ubicación del FXML.
     * @param rb Recursos de internacionalización.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> listaEstados = FXCollections.observableArrayList(
                "Todos",
                "pendiente",
                "pendiente de firma",
                "activo",
                "rescindido",
                "caducado",
                "rechazado"
        );
        comboFiltroEstado.setItems(listaEstados);
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
            public void changed(ObservableValue<? extends Contrato> obs, Contrato anterior, Contrato nuevo) {
                contratoSeleccionado = nuevo;
                actualizarEstadoBotones();
            }
        });
    }

    /**
     * Recorre la lista de contratos y sustituye los IDs de cliente, tarifa y
     * empleado/admin por sus nombres reales obtenidos de MongoDB.
     *
     * @param listaContratos Lista de contratos a traducir.
     */
    private void nombresVisibles(List<Contrato> listaContratos) {
        if (listaContratos == null) {
            return;
        }

        try {
            MongoDatabase baseDatos = MongoDBConexion.getDatabase();

            for (int i = 0; i < listaContratos.size(); i++) {
                Contrato contrato = listaContratos.get(i);

                String nombreCliente = buscarNombreCliente(baseDatos, contrato.getIdCliente(), contrato.getTelefono());
                contrato.setIdCliente(nombreCliente);

                String nombreTarifa = buscarNombreTarifa(baseDatos, contrato.getIdTarifa());
                contrato.setIdTarifa(nombreTarifa);

                String nombreGestor = buscarNombreGestor(baseDatos, contrato.getIdEmpleado());
                contrato.setIdEmpleado(nombreGestor);
            }
        } catch (Exception e) {
            LOGGER.warning("Error al traducir los nombres de los contratos: " + e.getMessage());
        }
    }

    /**
     * Busca el nombre de un cliente en la colección Clientes por su ID o, si
     * no lo encuentra, por su número de teléfono.
     *
     * @param baseDatos Conexión a la base de datos MongoDB.
     * @param idCliente ID del cliente en el contrato.
     * @param telefono Teléfono del contrato por si el ID no existe.
     * @return Nombre completo del cliente o "Cliente no encontrado".
     */
    private String buscarNombreCliente(MongoDatabase baseDatos, String idCliente, String telefono) {
        MongoCollection<Document> coleccionClientes = baseDatos.getCollection("Clientes");
        Document clienteDoc = null;

        if (idCliente != null && ObjectId.isValid(idCliente)) {
            Document filtroId = new Document("_id", new ObjectId(idCliente));
            clienteDoc = coleccionClientes.find(filtroId).first();
        }

        if (clienteDoc == null && telefono != null && !telefono.trim().isEmpty()) {
            Document filtroTel = new Document("telefono", telefono.trim());
            clienteDoc = coleccionClientes.find(filtroTel).first();
        }

        if (clienteDoc != null) {
            String nombre = clienteDoc.getString("nombre");
            String apellidos = clienteDoc.getString("apellidos");
            if (nombre != null && apellidos != null) {
                return nombre + " " + apellidos;
            } else if (nombre != null) {
                return nombre;
            }
        }

        return "Cliente no encontrado";
    }

    /**
     * Busca el tipo y nombre de una tarifa en la colección Tarifas.
     *
     * @param baseDatos Conexión a la base de datos MongoDB.
     * @param idTarifa ID de la tarifa.
     * @return Tipo o nombre de la tarifa.
     */
    private String buscarNombreTarifa(MongoDatabase baseDatos, String idTarifa) {
        if (idTarifa == null || !ObjectId.isValid(idTarifa)) {
            return "Sin tarifa";
        }

        MongoCollection<Document> coleccionTarifas = baseDatos.getCollection("Tarifas");
        Document filtro = new Document("_id", new ObjectId(idTarifa));
        Document tarifaDoc = coleccionTarifas.find(filtro).first();

        if (tarifaDoc != null) {
            String tipo = tarifaDoc.getString("tipo");
            String nombreTarifa = tarifaDoc.getString("nombreTarifa");

            if (tipo != null && nombreTarifa != null) {
                return tipo + " - " + nombreTarifa;
            } else if (tipo != null) {
                return tipo;
            } else if (nombreTarifa != null) {
                return nombreTarifa;
            }
        }

        return idTarifa;
    }

    /**
     * Busca quién aceptó el contrato mirando primero en Empleados y luego en
     * Administradores.
     *
     * @param baseDatos Conexión a la base de datos MongoDB.
     * @param idGestor ID del empleado o administrador.
     * @return Nombre y rol del gestor, o "Sin asignar" si está vacío.
     */
    private String buscarNombreGestor(MongoDatabase baseDatos, String idGestor) {
        if (idGestor == null || idGestor.trim().isEmpty()) {
            return "Sin asignar";
        }

        if (ObjectId.isValid(idGestor)) {
            Document filtro = new Document("_id", new ObjectId(idGestor));

            Document empleadoDoc = baseDatos.getCollection("Empleados").find(filtro).first();
            if (empleadoDoc != null) {
                String nombre = empleadoDoc.getString("nombre");
                String apellidos = empleadoDoc.getString("apellidos");
                return nombre + " " + apellidos + " (Empleado)";
            }

            Document adminDoc = baseDatos.getCollection("Administradores").find(filtro).first();
            if (adminDoc != null) {
                String nombre = adminDoc.getString("nombre");
                String apellidos = adminDoc.getString("apellidos");
                return nombre + " " + apellidos + " (Admin)";
            }
        }

        return idGestor;
    }

    /**
     * Carga todos los contratos desde MongoDB, traduce sus IDs a nombres y los
     * muestra en la tabla.
     */
    public void cargarContratos() {
        try {
            List<Contrato> lista = contratoDAO.listarContratos();
            if (lista != null) {
                nombresVisibles(lista);
                ObservableList<Contrato> datosTabla = FXCollections.observableArrayList(lista);
                tablaContratos.setItems(datosTabla);
            }
            limpiarSeleccion();

        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Filtra los contratos por teléfono y estado, y traduce los IDs a nombres.
     *
     * @param event Evento del botón Filtrar.
     */
    @FXML
    private void filtrarContratos(ActionEvent event) {
        try {
            String telefono = txtFiltroTelefono.getText();
            String estado = comboFiltroEstado.getValue();
            List<Contrato> listaFiltrada = contratoDAO.filtrarContratos(telefono, estado);
            nombresVisibles(listaFiltrada);
            tablaContratos.setItems(FXCollections.observableArrayList(listaFiltrada));
            limpiarSeleccion();

        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Aprueba la petición seleccionada guardando el ID del admin o empleado
     * que tiene la sesión iniciada.
     *
     * @param event Evento del botón Aceptar Petición.
     */
    @FXML
    private void aceptarPeticion(ActionEvent event) {
        try {
            if (contratoSeleccionado != null) {
                if (IniciarSesionController.idUsuarioActual == null || IniciarSesionController.idUsuarioActual.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Sesión no iniciada", "Debes iniciar sesión con un empleado o administrador real para aceptar peticiones.");
                    return;
                }

                if (contratoDAO.aprobarContrato(contratoSeleccionado, IniciarSesionController.idUsuarioActual)) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa confirmada. Contrato actualizado.");
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
     * Rechaza la petición seleccionada y recarga la tabla.
     *
     * @param event Evento del botón Rechazar Petición.
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
     * Rescinde el contrato activo seleccionado y recarga la tabla.
     *
     * @param event Evento del botón Rescindir Contrato.
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
     * Limpia los filtros de búsqueda y vuelve a cargar todos los contratos.
     *
     * @param event Evento del botón Refrescar.
     */
    @FXML
    private void refrescarFiltro(ActionEvent event) {
        txtFiltroTelefono.clear();
        comboFiltroEstado.setValue("Todos");
        cargarContratos();
    }

    /**
     * Quita la selección actual de la tabla y desactiva los botones de acción.
     */
    private void limpiarSeleccion() {
        tablaContratos.getSelectionModel().clearSelection();
        contratoSeleccionado = null;
        actualizarEstadoBotones();
    }

    /**
     * Activa o desactiva los botones según el estado del contrato seleccionado.
     */
    private void actualizarEstadoBotones() {
        boolean haySeleccion = contratoSeleccionado != null;
        boolean esPendiente = false;
        boolean esActivo = false;

        if (haySeleccion) {
            if ("pendiente".equalsIgnoreCase(contratoSeleccionado.getEstado())) {
                esPendiente = true;
            }
            if ("activo".equalsIgnoreCase(contratoSeleccionado.getEstado())) {
                esActivo = true;
            }
        }

        btnAceptarPeticion.setDisable(!esPendiente);
        btnRechazarPeticion.setDisable(!esPendiente);
        btnRescindirContrato.setDisable(!esActivo);
    }

    /**
     * Muestra una ventana emergente con un mensaje al usuario.
     *
     * @param tipo Tipo de alerta (información, aviso o error).
     * @param titulo Título de la ventana.
     * @param mensaje Texto que se mostrará en la alerta.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}