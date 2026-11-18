package controlador;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import java.util.logging.Logger;
import exception.ShowAlert;
import exception.OurException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Tarifa;
import DAO.TarifaDAO;
import DAO.implementacion.TarifaDAOImplementacion;

/**
 * Controlador de la vista de administración de tarifas en JavaFX.
 * Gestiona las operaciones CRUD (creación, consulta, actualización y eliminación)
 * sobre el catálogo de tarifas, así como la activación o desactivación directa
 * de las mismas y la sincronización entre la tabla y el formulario de edición.
 *
 * @author Eneko.Plaza
 */
public class GestionTarifasController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(GestionTarifasController.class.getName());

    /** Campo de texto para el nombre comercial de la tarifa. */
    @FXML
    private TextField txtNombre;

    /** Campo de texto para el precio mensual de la tarifa. */
    @FXML
    private TextField txtPrecio;

    /** Selector desplegable con las categorías de tarifa disponibles (Fibra, Móvil, etc.). */
    @FXML
    private ComboBox<String> comboTipo;

    /** Área de texto para describir las características y condiciones de la tarifa. */
    @FXML
    private TextArea txtDetalles;

    /** Casilla de verificación que indica si la tarifa está activa y visible para los clientes. */
    @FXML
    private CheckBox chkActiva;

    /** Botón para registrar una nueva tarifa en la base de datos. */
    @FXML
    private Button btnGuardar;

    /** Botón para guardar los cambios realizados sobre una tarifa seleccionada. */
    @FXML
    private Button btnActualizar;

    /** Botón para eliminar de la base de datos la tarifa seleccionada. */
    @FXML
    private Button btnEliminar;

    /** Botón para restablecer los campos del formulario y limpiar la selección. */
    @FXML
    private Button btnLimpiar;

    /** Tabla principal donde se listan todas las tarifas registradas. */
    @FXML
    private TableView<Tarifa> tablaTarifas;

    /** Columna de la tabla que muestra el nombre de la tarifa. */
    @FXML
    private TableColumn<Tarifa, String> colNombre;

    /** Columna de la tabla que muestra el precio de la tarifa. */
    @FXML
    private TableColumn<Tarifa, Double> colPrecio;

    /** Columna de la tabla que muestra la categoría o tipo de la tarifa. */
    @FXML
    private TableColumn<Tarifa, String> colTipo;

    /** Columna de la tabla que muestra los detalles descriptivos de la tarifa. */
    @FXML
    private TableColumn<Tarifa, String> colDetalles;

    /** Columna de la tabla que indica mediante un booleano si la tarifa está activa. */
    @FXML
    private TableColumn<Tarifa, Boolean> colActiva;

    /** Referencia a la {@link Tarifa} actualmente seleccionada en {@code tablaTarifas}. */
    private Tarifa tarifaSeleccionada;

    /** Objeto de acceso a datos para gestionar la persistencia de las tarifas en MongoDB. */
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Rellena el desplegable de tipos de tarifa, configura las factorías de celdas
     * de {@code tablaTarifas}, carga los datos desde MongoDB y registra un listener
     * para volcar en el formulario la fila seleccionada.
     *
     * @param url Ubicación utilizada para resolver rutas relativas del objeto raíz, o {@code null} si no se conoce.
     * @param rb  Recursos utilizados para localizar el objeto raíz, o {@code null} si no se localizó.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboTipo.setItems(FXCollections.observableArrayList("Fibra", "Móvil","Fibra + Móvil", "Convergente", "Televisión"));

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoTarifa"));
        colDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));

        cargarTabla();

        tablaTarifas.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tarifa>() {
            @Override
            public void changed(ObservableValue<? extends Tarifa> obs, Tarifa oldSelection, Tarifa newSelection) {
                if (newSelection != null) {
                    tarifaSeleccionada = newSelection;
                    mostrarDatosEnFormulario(tarifaSeleccionada);
                }
            }
        });
    }

    /**
     * Recupera el listado completo de tarifas desde MongoDB y actualiza los elementos
     * mostrados en {@code tablaTarifas}.
     */
    private void cargarTabla() {
        try {
            List<Tarifa> lista = tarifaDAO.listarTarifas();
            LOGGER.info("Tarifas cargadas: " + lista.size());
            ObservableList<Tarifa> datos = FXCollections.observableArrayList(lista);
            tablaTarifas.setItems(datos);
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Rellena los controles del formulario con los atributos de la tarifa recibida.
     *
     * @param t Objeto {@link Tarifa} cuyos datos se mostrarán para consulta o edición.
     */
    private void mostrarDatosEnFormulario(Tarifa t) {
        txtNombre.setText(t.getNombreTarifa());
        txtPrecio.setText(String.valueOf(t.getPrecio()));
        comboTipo.setValue(t.getTipoTarifa());
        txtDetalles.setText(t.getDetalles());
        chkActiva.setSelected(t.getActiva());
    }

    /**
     * Manejador del botón para guardar una nueva tarifa.
     * Valida los campos del formulario, construye una nueva instancia de {@link Tarifa},
     * la inserta mediante {@link TarifaDAO#insertarTarifa(Tarifa)} y refresca la interfaz.
     */
    @FXML
    private void btnGuardarAccion() {
        try {
            if (!validarCampos()) return;

            String nombre = txtNombre.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            String tipo = comboTipo.getValue();
            String detalles = txtDetalles.getText().trim();
            boolean activa = chkActiva.isSelected();

            Tarifa nueva = new Tarifa(nombre, precio, detalles, tipo, activa);

            if (tarifaDAO.insertarTarifa(nueva)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa guardada en MongoDB.");
                cargarTabla();
                btnLimpiarAccion();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar la tarifa.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador del botón para actualizar la tarifa seleccionada en la tabla.
     * Verifica que haya una selección activa y que los campos sean válidos, actualiza
     * los atributos de {@code tarifaSeleccionada} y persiste los cambios en MongoDB.
     */
    @FXML
    private void btnActualizarAccion() {
        try {
            if (tarifaSeleccionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona una tarifa de la tabla.");
                return;
            }

            if (!validarCampos()) return;

            tarifaSeleccionada.setNombreTarifa(txtNombre.getText().trim());
            tarifaSeleccionada.setPrecio(Double.parseDouble(txtPrecio.getText().trim()));
            tarifaSeleccionada.setTipoTarifa(comboTipo.getValue());
            tarifaSeleccionada.setDetalles(txtDetalles.getText().trim());
            tarifaSeleccionada.setActiva(chkActiva.isSelected());

            if (tarifaDAO.actualizarTarifa(tarifaSeleccionada)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa actualizada.");
                cargarTabla();
                btnLimpiarAccion();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la tarifa.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador del botón para eliminar la tarifa seleccionada en {@code tablaTarifas}
     * a través de {@link TarifaDAO#eliminarTarifa(Tarifa)}, recargando la tabla y limpiando el formulario.
     */
    @FXML
    private void btnEliminarAccion() {
        try {
            if (tarifaSeleccionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona una tarifa para eliminar.");
                return;
            }

            if (tarifaDAO.eliminarTarifa(tarifaSeleccionada)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa eliminada.");
                cargarTabla();
                btnLimpiarAccion();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la tarifa.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }
    
    /**
     * Activa la tarifa seleccionada en la tabla estableciendo su propiedad {@code activa} a {@code true}
     * y guardando el cambio en MongoDB. Revierte el estado en memoria si la actualización falla.
     */
    @FXML
    private void confirmarTarifa() {
        try {
            Tarifa seleccionada = tablaTarifas.getSelectionModel().getSelectedItem();
            if (seleccionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona una tarifa de la tabla.");
                return;
            }
            if (seleccionada.getActiva()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Esta tarifa ya está confirmada.");
                return;
            }

            seleccionada.setActiva(true);
            if (tarifaDAO.actualizarTarifa(seleccionada)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa confirmada.");
                cargarTabla();
                btnLimpiarAccion();
            } else {
                seleccionada.setActiva(false);
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo confirmar la tarifa.");
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Desactiva la tarifa seleccionada en la tabla estableciendo su propiedad {@code activa} a {@code false}
     * y guardando el cambio en MongoDB. Revierte el estado en memoria si la actualización falla.
     */
    @FXML
    private void rechazarTarifa() {
        try {
            Tarifa seleccionada = tablaTarifas.getSelectionModel().getSelectedItem();
            if (seleccionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Selecciona una tarifa de la tabla.");
                return;
            }
            if (!seleccionada.getActiva()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Esta tarifa ya está rechazada.");
                return;
            }

            seleccionada.setActiva(false);
            if (tarifaDAO.actualizarTarifa(seleccionada)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarifa rechazada.");
                cargarTabla();
                btnLimpiarAccion();
            } else {
                seleccionada.setActiva(true);
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo rechazar la tarifa.");
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Limpia todos los campos del formulario, marca {@code chkActiva} por defecto
     * y deselecciona cualquier tarifa activa en {@code tablaTarifas}.
     */
    @FXML
    private void btnLimpiarAccion() {
        txtNombre.clear();
        txtPrecio.clear();
        comboTipo.setValue(null);
        txtDetalles.clear();
        chkActiva.setSelected(true);
        tarifaSeleccionada = null;
        tablaTarifas.getSelectionModel().clearSelection();
    }

    /**
     * Comprueba que el nombre no esté vacío, que se haya seleccionado un tipo de tarifa
     * y que el precio introducido pueda convertirse correctamente a {@code double}.
     *
     * @return {@code true} si todos los campos cumplen con el formato requerido; {@code false} en caso contrario.
     */
    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Vacío", "Introduce el nombre.");
            return false;
        }
        if (comboTipo.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Vacío", "Selecciona un tipo.");
            return false;
        }
        try {
            Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Formato Incorrecto", "El precio debe ser un número decimal válido.");
            return false;
        }
        return true;
    }

    /**
     * Muestra un cuadro de diálogo modal de tipo {@link Alert} con la severidad, título y mensaje especificados.
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