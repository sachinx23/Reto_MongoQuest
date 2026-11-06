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

public class ClienteValoracionesController {

    private static final Logger LOGGER = Logger.getLogger(ClienteValoracionesController.class.getName());
    private final ValoracionDAO valoracionDAO = new ValoracionDAOImplementacion();
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();
    private Cliente cliente;

    // Mismo orden que cmbTarifa
    private List<Tarifa> tarifas = new ArrayList<>();

    @FXML private ComboBox<String> cmbTarifa;
    @FXML private ComboBox<Integer> cmbPuntuacion;
    @FXML private TextField txtValoracion;
    @FXML private TableView<Valoracion> tablaValoraciones;
    @FXML private TableColumn<Valoracion, Integer> colValoracionPuntuacion;
    @FXML private TableColumn<Valoracion, String> colValoracionTexto;

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

    private void cargarValoraciones() {
        try {
            tablaValoraciones.setItems(FXCollections.observableArrayList(valoracionDAO.obtenerValoracionesCliente(cliente.getId())));
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
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
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
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
            valoracionDAO.eliminarValoracion(valoracion);
            ShowAlert.info("Valoración eliminada", "La valoración ha sido eliminada.");
            limpiarValoracion();
            cargarValoraciones();
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private void limpiarValoracion() {
        cmbTarifa.getSelectionModel().clearSelection();
        cmbPuntuacion.setValue(null);
        txtValoracion.clear();
    }
}
