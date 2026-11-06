package controlador;

import daocliente.ValoracionDAO;
import daocliente.implementacion.ValoracionDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
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
import modelocliente.Cliente;
import modelocliente.Valoracion;

/**
 * Mis Valoraciones: valorar, editar (también con doble clic en la celda) y eliminar.
 */
public class ClienteValoracionesController {
    private final ValoracionDAO valoracionDAO = new ValoracionDAOImplementacion();
    private Cliente cliente;

    @FXML private ComboBox<Integer> cmbPuntuacion;
    @FXML private TextField txtValoracion;
    @FXML private TableView<Valoracion> tablaValoraciones;
    @FXML private TableColumn<Valoracion, Integer> colValoracionPuntuacion;
    @FXML private TableColumn<Valoracion, String> colValoracionTexto;

    /**
     * Se ejecuta al abrir la pantalla: prepara la tabla y carga los datos.
     */
    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        cmbPuntuacion.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        colValoracionPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colValoracionTexto.setCellValueFactory(new PropertyValueFactory<>("texto"));
        colValoracionTexto.setCellFactory(TextFieldTableCell.forTableColumn());
        cargarValoraciones();
    }

    /**
     * Recarga la tabla con las valoraciones del cliente.
     */
    private void cargarValoraciones() {
        try {
            tablaValoraciones.setItems(FXCollections.observableArrayList(valoracionDAO.obtenerValoracionesCliente(cliente.getId())));
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Pasa la valoración pulsada al formulario para poder editarla.
     *
     * @param event evento de la acción (botón, menú o desplegable)
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
     * Comprueba que hay puntuación y opinión (si no, avisa).
     *
     * @return true si hay puntuación y opinión
     */
    private boolean valoracionValida() {
        if (cmbPuntuacion.getValue() == null || txtValoracion.getText().trim().isEmpty()) {
            ShowAlert.info("Aviso", "Elige una puntuación y escribe tu opinión.");
            return false;
        }
        return true;
    }

    /**
     * Publica una valoración nueva de la tarifa elegida, después de confirmarlo.
     *
     * @param event evento de la acción (botón, menú o desplegable)
     */
    @FXML
    public void btnValorar(ActionEvent event) {
        if (!valoracionValida()) {
            return;
        }
        if (!ShowAlert.confirmar("Confirmar valoración", "¿Seguro que deseas publicar esta valoración?")) {
            return;
        }
        Valoracion valoracion = new Valoracion(null, cmbPuntuacion.getValue(), txtValoracion.getText().trim(), cliente.getId(), null);
        try {
            valoracionDAO.insertarValoracion(valoracion);
            ShowAlert.info("Valoración publicada", "Tu valoración ha sido registrada correctamente. ¡Gracias por tu opinión!");
            limpiarValoracion();
            cargarValoraciones();
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Guarda los cambios de la valoración elegida, después de confirmarlo.
     *
     * @param event evento de la acción (botón, menú o desplegable)
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
     * Se llama al terminar de editar una celda de texto en la tabla.
     *
     * @param event edición de la celda (valoración y texto nuevo)
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
     * Guarda en MongoDB los cambios de una valoración y recarga la tabla.
     *
     * @param valoracion valoración
     */
    private void guardarValoracion(Valoracion valoracion) {
        try {
            valoracionDAO.actualizarValoracion(valoracion);
            ShowAlert.info("Modificación exitosa", "Tu valoración se ha actualizado.");
            limpiarValoracion();
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
        }
        cargarValoraciones();
    }

    /**
     * Elimina la valoración elegida, después de confirmarlo.
     *
     * @param event evento de la acción (botón, menú o desplegable)
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
            valoracionDAO.borrarValoracion(valoracion.getId());
            ShowAlert.info("Valoración eliminada", "La valoración ha sido eliminada.");
            limpiarValoracion();
            cargarValoraciones();
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Vacía el formulario de valoración.
     */
    private void limpiarValoracion() {
        cmbPuntuacion.setValue(null);
        txtValoracion.clear();
    }
}
