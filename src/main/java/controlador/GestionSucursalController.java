package controlador;

import com.mycompany.reto_1.Reto_1;
import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import modelo.Sucursal;

/**
 * Gestión de sucursales. El botón "Ver en el mapa" abre la dirección en OpenStreetMap, en el navegador.
 */
public class GestionSucursalController {

    private static final Logger LOGGER = Logger.getLogger(GestionSucursalController.class.getName());

    private final DAO dao = Sesion.getDao();

    @FXML private ListView<Sucursal> listSucursales;
    @FXML private TextField txtNombre;
    @FXML private TextField txtHorario;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtLocalidad;
    @FXML private TextField txtCodigoPostal;

    @FXML
    public void initialize() {
        cargarSucursales();
    }

    private void cargarSucursales() {
        try {
            listSucursales.setItems(FXCollections.observableArrayList(dao.obtenerSucursales()));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void seleccionarSucursal(MouseEvent event) {
        Sucursal s = listSucursales.getSelectionModel().getSelectedItem();
        if (s == null) {
            return;
        }
        txtNombre.setText(s.getNombre());
        txtHorario.setText(s.getHorario());
        txtTelefono.setText(s.getTelefono());
        txtDireccion.setText(s.getDireccion());
        txtLocalidad.setText(s.getLocalidad());
        txtCodigoPostal.setText(String.valueOf(s.getCodigoPostal()));
    }

    // Abre la dirección de la sucursal seleccionada en OpenStreetMap
    @FXML
    public void btnVerMapa(ActionEvent event) {
        Sucursal s = listSucursales.getSelectionModel().getSelectedItem();
        if (s == null) {
            ShowAlert.info("Aviso", "Selecciona una sucursal de la lista primero.");
            return;
        }
        String direccion = s.getDireccion() + ", " + s.getCodigoPostal() + " " + s.getLocalidad() + ", España";
        Reto_1.abrirEnNavegador("https://www.openstreetmap.org/search?query=" + URLEncoder.encode(direccion, StandardCharsets.UTF_8));
        LOGGER.info("Mapa abierto para la sucursal " + s.getNombre());
    }

    // Devuelve el mensaje de error de los datos del formulario, o null si están bien
    private String comprobarDatos() {
        if (txtNombre.getText().trim().isEmpty() || txtHorario.getText().trim().isEmpty() || txtTelefono.getText().trim().isEmpty()
                || txtDireccion.getText().trim().isEmpty() || txtLocalidad.getText().trim().isEmpty()) {
            return "Por favor, rellena todos los campos.";
        }
        if (!ValidarPerfil.telefonoValido(txtTelefono.getText())) {
            return "El teléfono debe tener 9 números.";
        }
        if (!ValidarPerfil.codigoPostalValido(txtCodigoPostal.getText())) {
            return "El código postal debe tener 5 números.";
        }
        return null;
    }

    private Sucursal sucursalDelFormulario(String id) {
        return new Sucursal(id, txtNombre.getText().trim(), txtHorario.getText().trim(), txtTelefono.getText().trim(),
                txtDireccion.getText().trim(), txtLocalidad.getText().trim(), Integer.parseInt(txtCodigoPostal.getText().trim()));
    }

    @FXML
    public void btnAnadir(ActionEvent event) {
        String error = comprobarDatos();
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar creación de sucursal", "¿Deseas crear esta nueva sucursal?")) {
            return;
        }
        try {
            dao.insertarSucursal(sucursalDelFormulario(null));
            ShowAlert.info("Sucursal registrada", "La nueva sucursal ha sido dada de alta.");
            limpiar();
            cargarSucursales();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        Sucursal seleccionada = listSucursales.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            ShowAlert.info("Aviso", "Selecciona una sucursal de la lista primero.");
            return;
        }
        String error = comprobarDatos();
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar modificación de sucursal", "¿Deseas guardar los cambios de esta sucursal?")) {
            return;
        }
        try {
            dao.actualizarSucursal(sucursalDelFormulario(seleccionada.getId()));
            ShowAlert.info("Sucursal modificada", "Los datos de la sucursal se han actualizado correctamente.");
            limpiar();
            cargarSucursales();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnEliminar(ActionEvent event) {
        Sucursal seleccionada = listSucursales.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            ShowAlert.info("Aviso", "Selecciona una sucursal de la lista primero.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar eliminación de sucursal", "Vas a eliminar definitivamente la sucursal "
                + seleccionada.getNombre() + ". ¿Deseas continuar?")) {
            return;
        }
        try {
            dao.borrarSucursal(seleccionada.getId());
            ShowAlert.info("Sucursal eliminada", "La sucursal ha sido eliminada del sistema.");
            limpiar();
            cargarSucursales();
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
        txtHorario.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        txtLocalidad.clear();
        txtCodigoPostal.clear();
        listSucursales.getSelectionModel().clearSelection();
    }
}
