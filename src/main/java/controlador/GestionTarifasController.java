package controlador;

import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import modelo.Tarifa;

/**
 * Gestión de tarifas: crear, modificar, activar/desactivar y borrar.
 */
public class GestionTarifasController {

    private static final Logger LOGGER = Logger.getLogger(GestionTarifasController.class.getName());

    private final DAO dao = Sesion.getDao();

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextArea txtDetalles;
    @FXML private CheckBox chkActiva;

    @FXML private TableView<Tarifa> tablaTarifas;
    @FXML private TableColumn<Tarifa, String> colNombre;
    @FXML private TableColumn<Tarifa, String> colPrecio;
    @FXML private TableColumn<Tarifa, String> colTipo;
    @FXML private TableColumn<Tarifa, String> colDetalles;
    @FXML private TableColumn<Tarifa, String> colActiva;

    @FXML
    public void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList("Fibra", "Móvil", "Fibra + Móvil", "Televisión"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTexto"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoTarifa"));
        colDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activaTexto"));
        chkActiva.setSelected(true);
        cargarTarifas();
    }

    private void cargarTarifas() {
        try {
            tablaTarifas.setItems(FXCollections.observableArrayList(dao.obtenerTodasLasTarifas()));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void seleccionarTarifa(MouseEvent event) {
        Tarifa t = tablaTarifas.getSelectionModel().getSelectedItem();
        if (t != null) {
            txtNombre.setText(t.getNombreTarifa());
            txtPrecio.setText(String.valueOf(t.getPrecio()));
            cmbTipo.setValue(t.getTipoTarifa());
            txtDetalles.setText(t.getDetalles());
            chkActiva.setSelected(t.isActiva());
        }
    }

    // Devuelve el mensaje de error de los datos del formulario, o null si están bien
    private String comprobarDatos() {
        if (txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty() || cmbTipo.getValue() == null) {
            return "Rellena el nombre, el precio y el tipo.";
        }
        // Se admite la coma decimal (12,5)
        if (!txtPrecio.getText().trim().replace(",", ".").matches("[0-9]+(\\.[0-9]{1,2})?")) {
            return "El precio debe ser un número, por ejemplo 29.99";
        }
        return null;
    }

    private Tarifa tarifaDelFormulario(String id) {
        double precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
        return new Tarifa(id, txtNombre.getText().trim(), precio, txtDetalles.getText().trim(), cmbTipo.getValue(),
                chkActiva.isSelected());
    }

    @FXML
    public void btnGuardar(ActionEvent event) {
        String error = comprobarDatos();
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Nueva tarifa", "¿Deseas crear la tarifa " + txtNombre.getText().trim() + "?")) {
            return;
        }
        try {
            dao.insertarTarifa(tarifaDelFormulario(null));
            ShowAlert.info("Tarifa creada", "La tarifa se ha guardado correctamente.");
            limpiar();
            cargarTarifas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnActualizar(ActionEvent event) {
        Tarifa seleccionada = tablaTarifas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            ShowAlert.info("Aviso", "Selecciona una tarifa de la tabla primero.");
            return;
        }
        String error = comprobarDatos();
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar cambios", "¿Seguro que deseas guardar los cambios de la tarifa?")) {
            return;
        }
        guardar(tarifaDelFormulario(seleccionada.getId()));
    }

    // Activar o desactivar: una tarifa desactivada ya no la ven los clientes
    @FXML
    public void btnActivar(ActionEvent event) {
        Tarifa seleccionada = tablaTarifas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            ShowAlert.info("Aviso", "Selecciona una tarifa de la tabla primero.");
            return;
        }
        String accion = "activar";
        if (seleccionada.isActiva()) {
            accion = "desactivar";
        }
        if (!ShowAlert.confirmar("Activar / desactivar", "¿Deseas " + accion + " la tarifa " + seleccionada.getNombreTarifa() + "?")) {
            return;
        }
        Tarifa cambiada = new Tarifa(seleccionada.getId(), seleccionada.getNombreTarifa(), seleccionada.getPrecio(),
                seleccionada.getDetalles(), seleccionada.getTipoTarifa(), !seleccionada.isActiva());
        guardar(cambiada);
    }

    private void guardar(Tarifa tarifa) {
        try {
            dao.actualizarTarifa(tarifa);
            ShowAlert.info("Modificación exitosa", "La tarifa se ha actualizado.");
            limpiar();
            cargarTarifas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnEliminar(ActionEvent event) {
        Tarifa seleccionada = tablaTarifas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            ShowAlert.info("Aviso", "Selecciona una tarifa de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar eliminación", "¿Seguro que deseas eliminar la tarifa " + seleccionada.getNombreTarifa() + "?")) {
            return;
        }
        try {
            dao.borrarTarifa(seleccionada.getId());
            ShowAlert.info("Tarifa eliminada", "La tarifa ha sido eliminada.");
            limpiar();
            cargarTarifas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnLimpiar(ActionEvent event) {
        limpiar();
    }

    private void limpiar() {
        txtNombre.clear();
        txtPrecio.clear();
        cmbTipo.setValue(null);
        txtDetalles.clear();
        chkActiva.setSelected(true);
        tablaTarifas.getSelectionModel().clearSelection();
    }
}
