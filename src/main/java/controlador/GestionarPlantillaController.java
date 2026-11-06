package controlador;

import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Sucursal;

/**
 * Gestión de la plantilla: alta, ascenso y baja de empleados, y borrado de datos de clientes dados de baja.
 */
public class GestionarPlantillaController {

    private static final Logger LOGGER = Logger.getLogger(GestionarPlantillaController.class.getName());

    private final DAO dao = Sesion.getDao();

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colEmpNombre;
    @FXML private TableColumn<Empleado, String> colEmpUsuario;
    @FXML private TableColumn<Empleado, String> colEmpRol;
    @FXML private TableColumn<Empleado, String> colEmpCorreo;
    @FXML private TableColumn<Empleado, String> colEmpCuenta;

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<Sucursal> cmbSucursal;

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colCliNombre;
    @FXML private TableColumn<Cliente, String> colCliUsuario;
    @FXML private TableColumn<Cliente, String> colCliDni;
    @FXML private TableColumn<Cliente, String> colCliTelefono;
    @FXML private TableColumn<Cliente, String> colCliEstado;

    @FXML
    public void initialize() {
        colEmpNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmpUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmpRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEmpCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colEmpCuenta.setCellValueFactory(new PropertyValueFactory<>("cuentaBancaria"));

        colCliNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCliUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colCliDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colCliTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCliEstado.setCellValueFactory(new PropertyValueFactory<>("bajaTexto"));

        cargarTablas();
        try {
            cmbSucursal.setItems(FXCollections.observableArrayList(dao.obtenerSucursales()));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    private void cargarTablas() {
        try {
            tablaEmpleados.setItems(FXCollections.observableArrayList(dao.obtenerEmpleados()));
            tablaClientes.setItems(FXCollections.observableArrayList(dao.obtenerClientes()));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnCrearEmpleado(ActionEvent event) {
        String error = ValidarPerfil.comprobar(txtNombre.getText(), txtNombreUsuario.getText(),
                txtContrasena.getText(), txtCorreo.getText());
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar alta", "¿Deseas dar de alta a este nuevo empleado?")) {
            return;
        }
        // La sucursal es opcional
        String idSucursal = null;
        if (cmbSucursal.getValue() != null) {
            idSucursal = cmbSucursal.getValue().getId();
        }
        Empleado nuevo = new Empleado(null, txtNombre.getText().trim(), txtNombreUsuario.getText().trim(),
                txtContrasena.getText().trim(), txtCorreo.getText().trim(), "", idSucursal);
        try {
            dao.insertarEmpleado(nuevo);
            ShowAlert.info("Empleado registrado", "La cuenta del nuevo trabajador ha sido generada en el sistema.");
            txtNombre.clear();
            txtNombreUsuario.clear();
            txtContrasena.clear();
            txtCorreo.clear();
            cmbSucursal.setValue(null);
            cargarTablas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnAscender(ActionEvent event) {
        Empleado empleado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (empleado == null) {
            ShowAlert.info("Aviso", "Selecciona un empleado de la tabla primero.");
            return;
        }
        if (empleado instanceof Administrador) {
            ShowAlert.info("Aviso", "Ese usuario ya es administrador.");
            return;
        }
        if (!ShowAlert.confirmar("Ascender empleado", "¿Estás seguro de ascender a " + empleado.getNombre()
                + "? Pasará a tener privilegios totales de Administrador.")) {
            return;
        }
        try {
            dao.ascenderEmpleado(empleado);
            ShowAlert.info("Empleado ascendido", "El empleado ahora tiene privilegios de Administrador.");
            cargarTablas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnBorrarEmpleado(ActionEvent event) {
        Empleado empleado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (empleado == null) {
            ShowAlert.info("Aviso", "Selecciona un empleado de la tabla primero.");
            return;
        }
        if (empleado instanceof Administrador) {
            ShowAlert.info("Aviso", "Solo se pueden borrar empleados, no administradores.");
            return;
        }
        if (!ShowAlert.confirmar("Eliminar cuenta de empleado", "Vas a borrar definitivamente la cuenta de "
                + empleado.getNombre() + ". Perderá su acceso al sistema. ¿Deseas continuar?")) {
            return;
        }
        try {
            dao.borrarEmpleado(empleado.getId());
            ShowAlert.info("Empleado eliminado", "La cuenta del empleado ha sido eliminada del sistema.");
            cargarTablas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnBorrarDatosCliente(ActionEvent event) {
        Cliente cliente = tablaClientes.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            ShowAlert.info("Aviso", "Selecciona un cliente de la tabla primero.");
            return;
        }
        if (!cliente.isDadoDeBaja()) {
            ShowAlert.info("Aviso", "Solo se pueden borrar los datos de un cliente dado de baja.");
            return;
        }
        if (!ShowAlert.confirmar("Borrar datos sensibles", "¿Deseas borrar la cuenta bancaria, el teléfono y la dirección de "
                + cliente.getNombre() + "?")) {
            return;
        }
        try {
            dao.borrarDatosSensiblesCliente(cliente.getId());
            ShowAlert.info("Datos borrados", "Los datos sensibles del cliente se han borrado.");
            cargarTablas();
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
