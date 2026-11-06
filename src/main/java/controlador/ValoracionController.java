package controlador;

import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Valoracion;

/**
 * Valoraciones de todos los clientes, con filtro por puntuación y la media.
 */
public class ValoracionController {

    private static final Logger LOGGER = Logger.getLogger(ValoracionController.class.getName());
    private static final String TODAS = "Todas";

    private final DAO dao = Sesion.getDao();

    @FXML private ComboBox<String> cmbPuntuacion;
    @FXML private TableView<Valoracion> tablaValoraciones;
    @FXML private TableColumn<Valoracion, String> colCliente;
    @FXML private TableColumn<Valoracion, Integer> colPuntuacion;
    @FXML private TableColumn<Valoracion, String> colTexto;
    @FXML private Label lblMedia;

    @FXML
    public void initialize() {
        cmbPuntuacion.setItems(FXCollections.observableArrayList(TODAS, "1", "2", "3", "4", "5"));
        cmbPuntuacion.setValue(TODAS);
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colTexto.setCellValueFactory(new PropertyValueFactory<>("texto"));
        cargarValoraciones();
    }

    private void cargarValoraciones() {
        List<Valoracion> todas = new ArrayList<>();
        try {
            todas = dao.obtenerTodasLasValoraciones();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
        // Filtro por puntuación y media de todas las valoraciones
        List<Valoracion> filtradas = new ArrayList<>();
        double suma = 0;
        for (Valoracion v : todas) {
            suma = suma + v.getPuntuacion();
            if (TODAS.equals(cmbPuntuacion.getValue()) || String.valueOf(v.getPuntuacion()).equals(cmbPuntuacion.getValue())) {
                filtradas.add(v);
            }
        }
        tablaValoraciones.setItems(FXCollections.observableArrayList(filtradas));
        if (todas.isEmpty()) {
            lblMedia.setText("Media: sin valoraciones");
        } else {
            lblMedia.setText("Media: " + String.format(Locale.forLanguageTag("es-ES"), "%.2f", suma / todas.size()) + " / 5");
        }
    }

    @FXML
    public void cmbFiltrar(ActionEvent event) {
        cargarValoraciones();
    }

    @FXML
    public void btnEliminar(ActionEvent event) {
        Valoracion v = tablaValoraciones.getSelectionModel().getSelectedItem();
        if (v == null) {
            ShowAlert.info("Aviso", "Selecciona una valoración de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar eliminación", "¿Seguro que deseas eliminar esta valoración?")) {
            return;
        }
        try {
            dao.borrarValoracion(v.getId());
            ShowAlert.info("Valoración eliminada", "La valoración ha sido eliminada.");
            cargarValoraciones();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
