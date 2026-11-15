package controlador;

import java.util.logging.Logger;
import DAO.ValoracionDAO;
import DAO.implementacion.ValoracionDAOImplementacion;
import DAO.TarifaDAO;
import DAO.implementacion.TarifaDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import modelo.Cliente;
import modelo.Valoracion;
import modelo.Tarifa;

/**
 * Controlador de la subvista de valoraciones del cliente en JavaFX.
 * Permite al cliente autenticado publicar nuevas valoraciones sobre las tarifas activas,
 * consultar su historial de opiniones y editar (tanto desde el formulario como directamente
 * en la celda de la tabla) o eliminar sus valoraciones existentes.
 *
 * @author Eneko.Plaza
 */
public class ClienteValoracionesController {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(ClienteValoracionesController.class.getName());

    /** Objeto de acceso a datos para gestionar las operaciones CRUD sobre las valoraciones. */
    private final ValoracionDAO valoracionDAO = new ValoracionDAOImplementacion();

    /** Objeto de acceso a datos para recuperar el listado de tarifas disponibles a valorar. */
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();

    /** Instancia del cliente que tiene la sesión iniciada actualmente. */
    private Cliente cliente;

    /** Lista de tarifas activas mantenida en el mismo orden que los elementos de {@code cmbTarifa}. */
    private List<Tarifa> tarifas = new ArrayList<>();

    /** Selector desplegable con los nombres de las tarifas activas que se pueden valorar. */
    @FXML private ComboBox<String> cmbTarifa;

    /** Selector desplegable con las puntuaciones numéricas permitidas (del 1 al 5). */
    @FXML private ComboBox<Integer> cmbPuntuacion;

    /** Campo de texto para escribir o modificar el comentario de la valoración. */
    @FXML private TextField txtValoracion;

    /** Tabla donde se muestran las valoraciones realizadas por el cliente. */
    @FXML private TableView<Valoracion> tablaValoraciones;

    /** Columna de la tabla que muestra la puntuación otorgada en cada valoración. */
    @FXML private TableColumn<Valoracion, Integer> colValoracionPuntuacion;

    /** Columna editable de la tabla que muestra el comentario textual de cada valoración. */
    @FXML private TableColumn<Valoracion, String> colValoracionTexto;

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Obtiene el cliente activo desde {@link ClienteController}, configura las opciones
     * de puntuación (1-5), carga las tarifas activas en el desplegable, habilita la edición
     * en línea sobre la columna de texto de la tabla y carga las valoraciones del cliente.
     */
    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        cmbPuntuacion.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        cargarTarifas();
        colValoracionPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colValoracionTexto.setCellValueFactory(new PropertyValueFactory<>("texto"));
        colValoracionTexto.setCellFactory(TextFieldTableCell.forTableColumn());
        cargarValoraciones();
    }

    /**
     * Recupera las tarifas activas desde la base de datos, las almacena en la lista local
     * {@code tarifas} y vuelca sus nombres en el selector {@code cmbTarifa}.
     */
    private void cargarTarifas() {
        try {
            tarifas = tarifaDAO.tarifasActivas();
            List<String> nombres = new ArrayList<>();
            for (Tarifa t : tarifas) {
                nombres.add(t.getNombreTarifa());
            }
            cmbTarifa.setItems(FXCollections.observableArrayList(nombres));
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Consulta las valoraciones realizadas por el cliente autenticado y actualiza
     * los datos mostrados en {@code tablaValoraciones}.
     */
    private void cargarValoraciones() {
        try {
            tablaValoraciones.setItems(FXCollections.observableArrayList(valoracionDAO.obtenerValoracionesCliente(cliente.getId())));
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador del evento de clic sobre la tabla que carga la puntuación y el texto
     * de la valoración seleccionada en los controles del formulario para facilitar su edición.
     *
     * @param event Evento de ratón disparado al hacer clic sobre una fila de {@code tablaValoraciones}.
     */
    @FXML
    public void seleccionarValoracion(MouseEvent event) {
        Valoracion valoracion = tablaValoraciones.getSelectionModel().getSelectedItem();
        if (valoracion != null) {
            cmbPuntuacion.setValue(valoracion.getPuntuacion());
            txtValoracion.setText(valoracion.getTexto());
        }
    }

    /**
     * Valida que el usuario haya seleccionado una puntuación y escrito un comentario no vacío.
     * Si falta algún dato, muestra un aviso informativo en pantalla.
     *
     * @return {@code true} si la puntuación y el texto son válidos; {@code false} en caso contrario.
     */
    private boolean valoracionValida() {
        if (cmbPuntuacion.getValue() == null || txtValoracion.getText().trim().isEmpty()) {
            ShowAlert.info("Aviso", "Elige una puntuación y escribe tu opinión.");
            return false;
        }
        return true;
    }

    /**
     * Manejador del botón para publicar una nueva valoración.
     * Verifica que se haya elegido una tarifa y que los datos sean válidos, solicita
     * confirmación al cliente, registra la valoración mediante {@link ValoracionDAO#insertarValoracion(Valoracion)},
     * limpia el formulario y refresca la tabla.
     *
     * @param event Evento de acción disparado al pulsar el botón de publicar valoración.
     */
    @FXML
    public void btnValorar(ActionEvent event) {
        int posicion = cmbTarifa.getSelectionModel().getSelectedIndex();
        if (posicion < 0) {
            ShowAlert.info("Aviso", "Elige la tarifa que quieres valorar.");
            return;
        }
        if (!valoracionValida()) {
            return;
        }
        Tarifa tarifa = tarifas.get(posicion);
        if (!ShowAlert.confirmar("Confirmar valoración", "¿Seguro que deseas publicar esta valoración?")) {
            return;
        }
        Valoracion valoracion = new Valoracion(null, cmbPuntuacion.getValue(), txtValoracion.getText().trim(), cliente.getId(), tarifa.getId(),
                "", tarifa.getNombreTarifa());
        try {
            valoracionDAO.insertarValoracion(valoracion);
            ShowAlert.info("Valoración publicada", "Tu valoración ha sido registrada correctamente. ¡Gracias por tu opinión!");
            limpiarValoracion();
            cargarValoraciones();
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador del botón para editar una valoración seleccionada en la tabla utilizando
     * los valores introducidos en los controles del formulario.
     *
     * @param event Evento de acción disparado al pulsar el botón de editar valoración.
     */
    @FXML
    public void btnEditarValoracion(ActionEvent event) {
        Valoracion valoracion = tablaValoraciones.getSelectionModel().getSelectedItem();
        if (valoracion == null) {
            ShowAlert.info("Aviso", "Selecciona una valoración de la tabla primero.");
            return;
        }
        if (!valoracionValida()) {
            return;
        }
        if (!ShowAlert.confirmar("Confirmar cambios", "¿Seguro que deseas guardar los cambios de tu valoración?")) {
            return;
        }
        valoracion.setPuntuacion(cmbPuntuacion.getValue());
        valoracion.setTexto(txtValoracion.getText().trim());
        guardarValoracion(valoracion);
    }

    /**
     * Manejador del evento de confirmación de edición en línea sobre la columna de texto
     * de la tabla. Valida que el nuevo texto no esté vacío y persiste el cambio en la base de datos.
     *
     * @param event Evento de edición de celda que contiene la fila afectada y el nuevo texto introducido.
     */
    @FXML
    public void editarTextoValoracion(TableColumn.CellEditEvent<Valoracion, String> event) {
        Valoracion valoracion = event.getRowValue();
        String nuevoTexto = event.getNewValue().trim();
        if (nuevoTexto.isEmpty()) {
            ShowAlert.info("Aviso", "La opinión no puede estar vacía.");
            cargarValoraciones();
            return;
        }
        valoracion.setTexto(nuevoTexto);
        guardarValoracion(valoracion);
    }

    /**
     * Persiste las modificaciones de una valoración en MongoDB mediante
     * {@link ValoracionDAO#actualizarValoracion(Valoracion)}, limpia los campos del formulario
     * y recarga el listado de valoraciones en la tabla.
     *
     * @param valoracion Objeto {@link Valoracion} con los cambios ya aplicados.
     */
    private void guardarValoracion(Valoracion valoracion) {
        try {
            valoracionDAO.actualizarValoracion(valoracion);
            ShowAlert.info("Modificación exitosa", "Tu valoración se ha actualizado.");
            limpiarValoracion();
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
        cargarValoraciones();
    }

    /**
     * Manejador del botón para eliminar la valoración seleccionada en la tabla tras
     * solicitar confirmación al cliente.
     *
     * @param event Evento de acción disparado al pulsar el botón de eliminar valoración.
     */
    @FXML
    public void btnEliminarValoracion(ActionEvent event) {
        Valoracion valoracion = tablaValoraciones.getSelectionModel().getSelectedItem();
        if (valoracion == null) {
            ShowAlert.info("Aviso", "Selecciona una valoración de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar eliminación", "¿Seguro que deseas eliminar esta valoración?")) {
            return;
        }
        try {
            valoracionDAO.eliminarValoracion(valoracion);
            ShowAlert.info("Valoración eliminada", "La valoración ha sido eliminada.");
            limpiarValoracion();
            cargarValoraciones();
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Restablece los controles del formulario de valoración dejando sin selección los
     * desplegables de tarifa y puntuación y vaciando el campo de texto.
     */
    private void limpiarValoracion() {
        cmbTarifa.getSelectionModel().clearSelection();
        cmbPuntuacion.setValue(null);
        txtValoracion.clear();
    }
}