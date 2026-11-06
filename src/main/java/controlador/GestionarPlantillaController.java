package controlador;

import java.util.logging.Logger;
import exception.ShowAlert;
import exception.OurException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Sucursal;
import DAO.EmpleadoDAO;
import DAO.ClienteDAO;
import DAO.AdministradorDAO;
import DAO.SucursalDAO;
import DAO.implementacion.EmpleadoDaoImplementacion;
import DAO.implementacion.ClienteDAOImplementacion;
import DAO.implementacion.AdministradorDAOImpl;
import DAO.implementacion.SucursalDaoImplementacion;

public class GestionarPlantillaController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(GestionarPlantillaController.class.getName());

    // Tabla empleado
    @FXML
    private TableView<Empleado> tablaEmpleados;
    @FXML
    private TableColumn<Empleado, String> colEmpNombre;
    @FXML
    private TableColumn<Empleado, String> colEmpApellidos;
    @FXML
    private TableColumn<Empleado, String> colEmpUsuario;
    @FXML
    private TableColumn<Empleado, String> colEmpRol;
    @FXML
    private TableColumn<Empleado, String> colEmpEmail;
    @FXML
    private TableColumn<Empleado, String> colEmpDireccion;
    @FXML
    private TableColumn<Empleado, String> colEmpSucursal;
    @FXML
    private TableColumn<Empleado, String> colEmpIBAN;

    // Tabla Clientes
    @FXML
    private TableView<Cliente> tablaClientes;
    @FXML
    private TableColumn<Cliente, String> colCliNombre;
    @FXML
    private TableColumn<Cliente, String> colCliApellidos;
    @FXML
    private TableColumn<Cliente, String> colCliUsuario;
    @FXML
    private TableColumn<Cliente, String> colCliEmail;
    @FXML
    private TableColumn<Cliente, String> colCliTelefono;
    @FXML
    private TableColumn<Cliente, String> colCliDni;
    @FXML
    private TableColumn<Cliente, String> colCliDireccion;

    // CAMPOS DEL FORMULARIO INTEGRADO (EMPLEADOS)
    @FXML
    private VBox panelFormulario;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtDireccion;
    @FXML
    private ComboBox<String> cmbIdSucursal;
    @FXML
    private TextField txtIban;

    private final ObservableList<Empleado> listaEmpleados = FXCollections.observableArrayList();
    private final ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    private final EmpleadoDAO empleadoDAO = new EmpleadoDaoImplementacion();
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();
    private final SucursalDAO sucursalDAO = new SucursalDaoImplementacion();
    


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mapeo columnas Empleados
        colEmpNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmpApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colEmpUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmpRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEmpEmail.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colEmpDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colEmpSucursal.setCellValueFactory(new PropertyValueFactory<>("idSucursal"));
        colEmpIBAN.setCellValueFactory(new PropertyValueFactory<>("cuentaBancaria"));

        // Mapeo columnas Clientes
        colCliNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCliApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colCliUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colCliEmail.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCliTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCliDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colCliDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        tablaEmpleados.setItems(listaEmpleados);
        tablaClientes.setItems(listaClientes);

        cargarTablas();
        cargarComboSucursales();
    }

    private void cargarTablas() {
        try {
            listaEmpleados.clear();
            listaClientes.clear();

            List<Empleado> empleados = empleadoDAO.listarEmpleados();
            List<Cliente> clientes = clienteDAO.listarClientes();

            if (empleados != null) {
                listaEmpleados.addAll(empleados);
            }
            if (clientes != null) {
                listaClientes.addAll(clientes);
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private void cargarComboSucursales() {
        try {
            cmbIdSucursal.getItems().clear();
            List<Sucursal> sucursales = sucursalDAO.listarSucursales();
            if (sucursales != null) {
                for (Sucursal s : sucursales) {
                    cmbIdSucursal.getItems().add(String.valueOf(s.getUbicacion()));
                }
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void alternarFormulario(ActionEvent event) {
        boolean visible = panelFormulario.isVisible();
        panelFormulario.setVisible(!visible);
        panelFormulario.setManaged(!visible);
        if (!visible) {
            cargarComboSucursales();
        }
    }

    @FXML
    public void cancelarFormulario() {
        limpiarCampos();
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
    }

    @FXML
    public void crearEmpleado(ActionEvent event) {
        try {
            String nombre = txtNombre.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String direccion = txtDireccion.getText().trim();
            String idSucursal = cmbIdSucursal.getValue();
            String cuentaBancaria = txtIban.getText().trim();

            if (nombre.isEmpty() || apellidos.isEmpty() || direccion.isEmpty() || idSucursal == null || idSucursal.isEmpty() || cuentaBancaria.isEmpty()) {
                mostrarAlerta("Error", "Por favor, rellena todos los campos y selecciona una sucursal.");
                return;
            }

            String nombreN = nombre.toLowerCase().replaceAll("\\s+", "");
            String primerApellido = apellidos.split("\\s+")[0].toLowerCase();
            String usuario = nombreN + "." + primerApellido;

            boolean ok = administradorDAO.crearEmpleado(usuario, nombre, apellidos, "1234", usuario + "@telonda.com", cuentaBancaria, direccion, idSucursal);

            if (ok) {
                administradorDAO.agregarEmpleadoASucursal(Integer.parseInt(idSucursal), usuario);

                mostrarAlerta("Éxito", "Empleado creado y metido en la sucursal.");
                cargarTablas();
                cancelarFormulario();
            } else {
                mostrarAlerta("Error", "No se pudo crear el empleado (quizás el usuario ya existe).");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtDireccion.clear();
        cmbIdSucursal.getSelectionModel().clearSelection();
        txtIban.clear();
    }

    @FXML
    public void ascenderAAdmin(ActionEvent event) {
        try {
            Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                boolean ok = empleadoDAO.ascenderEmpleado(seleccionado.getNombreUsuario(), "Admin");
                if (ok) {
                    mostrarAlerta("Éxito", "El empleado ahora es Administrador.");
                    cargarTablas();
                } else {
                    mostrarAlerta("Error", "No se pudo ascender al empleado.");
                }
            } else {
                mostrarAlerta("Atención", "Selecciona primero un empleado en la tabla.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void eliminarEmpleado(ActionEvent event) {
        try {
            Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                boolean ok = empleadoDAO.eliminarEmpleado(seleccionado.getNombreUsuario());
                if (ok) {
                    mostrarAlerta("Éxito", "El empleado ha sido dado de baja.");
                    cargarTablas();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar al empleado.");
                }
            } else {
                mostrarAlerta("Atención", "Selecciona primero un empleado en la tabla.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void eliminarCliente(ActionEvent event) {
        try {
            Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                Alert confirmacion = new Alert(AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar eliminación");
                confirmacion.setHeaderText(null);
                confirmacion.setContentText("¿Desea eliminar los datos sensibles del cliente?");

                Optional<ButtonType> res = confirmacion.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.OK) {
                    boolean ok = clienteDAO.eliminarDatosSensiblesCliente(seleccionado.getNombreUsuario());
                    if (ok) {
                        mostrarAlerta("Éxito", "Datos sensibles eliminados.");
                        cargarTablas();
                    } else {
                        mostrarAlerta("Error", "No se pudieron borrar los datos.");
                    }
                }
            } else {
                mostrarAlerta("Atención", "Selecciona primero un cliente de la tabla.");
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String texto) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}
