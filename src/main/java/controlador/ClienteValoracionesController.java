package controlador;

import daocliente.ValoracionDAO;
import daocliente.implementacion.ValoracionDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
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

/**
 * Mis Valoraciones: valorar, editar (también con doble clic en la celda) y eliminar.
 */
public class ClienteValoracionesController {

    private static final Logger LOGGER = Logger.getLogger(ClienteValoracionesController.class.getName());

    private final ValoracionDAO valoracionDAO = new ValoracionDAOImplementacion();
    private Cliente cliente;

    @FXML private ComboBox<Integer> cmbPuntuacion;
    @FXML private TextField txtValoracion;
    @FXML private TableView<Valoracion> tablaValoraciones;
    @FXML private TableColumn<Valoracion, Integer> colValoracionPuntuacion;
    @FXML private TableColumn<Valoracion, String> colValoracionTexto;

    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        cmbPuntuacion.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        colValoracionPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colValoracionTexto.setCellValueFactory(new PropertyValueFactory<>("texto"));
        colValoracionTexto.setCellFactory(TextFieldTableCell.forTableColumn());
        cargarValoraciones();
    }

    private void cargarValoraciones() {
        try {
            tablaValoraciones.setItems(FXCollections.observableArrayList(valoracionDAO.obtenerValoracionesCliente(cliente.getId())));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void seleccionarValoracion(MouseEvent event) {
        Valoracion valoracion = tablaValoraciones.getSelectionModel().getSelectedItem();
        if (valoracion != null) {
            cmbPuntuacion.setValue(valoracion.getPuntuacion());
            txtValoracion.setText(valoracion.getTexto());
        }
    }

    private boolean valoracionValida() {
        if (cmbPuntuacion.getValue() == null || txtValoracion.getText().trim().isEmpty()) {
            ShowAlert.info("Aviso", "Elige una puntuación y escribe tu opinión.");
            return false;
        }
        return true;
    }

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
            ShowAlert.error(LOGGER, e);
        }
    }

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

    // Se llama al terminar de editar una celda de texto en la tabla
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

    private void guardarValoracion(Valoracion valoracion) {
        try {
            valoracionDAO.actualizarValoracion(valoracion);
            ShowAlert.info("Modificación exitosa", "Tu valoración se ha actualizado.");
            limpiarValoracion();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
        cargarValoraciones();
    }

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
            ShowAlert.error(LOGGER, e);
        }
    }

    private void limpiarValoracion() {
        cmbPuntuacion.setValue(null);
        txtValoracion.clear();
    }
}
