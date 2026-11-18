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
import DAO.implementacion.EmpleadoDAOImplementacion;
import DAO.implementacion.ClienteDAOImplementacion;
import DAO.implementacion.AdministradorDAOImpl;
import DAO.implementacion.SucursalDAOImplementacion;
import modelo.Ubicacion;

/**
 * Controlador de la vista de gestión de plantilla y clientes para el Administrador en JavaFX.
 * Administra dos tablas independientes: una para visualizar y gestionar el personal
 * (alta de nuevos empleados mediante formulario desplegable, ascenso a administrador y baja)
 * y otra para consultar los clientes registrados y proceder a la anonimización o borrado
 * de sus datos sensibles.
 *
 * @author Eneko.Plaza
 */
public class GestionarPlantillaController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(GestionarPlantillaController.class.getName());

    // Tabla empleado

    /** Tabla principal donde se muestra el listado de empleados de la empresa. */
    @FXML
    private TableView<Empleado> tablaEmpleados;

    /** Columna de la tabla que muestra el nombre de pila del empleado. */
    @FXML
    private TableColumn<Empleado, String> colEmpNombre;

    /** Columna de la tabla que muestra los apellidos del empleado. */
    @FXML
    private TableColumn<Empleado, String> colEmpApellidos;

    /** Columna de la tabla que muestra el nombre de usuario corporativo del empleado. */
    @FXML
    private TableColumn<Empleado, String> colEmpUsuario;

    /** Columna de la tabla que indica el rol asignado al empleado (por ejemplo, Empleado o Admin). */
    @FXML
    private TableColumn<Empleado, String> colEmpRol;

    /** Columna de la tabla que muestra el correo electrónico corporativo del empleado. */
    @FXML
    private TableColumn<Empleado, String> colEmpEmail;

    /** Columna de la tabla que muestra el domicilio del empleado. */
    @FXML
    private TableColumn<Empleado, String> colEmpDireccion;

    /** Columna de la tabla que muestra el identificador o ubicación de la sucursal asignada. */
    @FXML
    private TableColumn<Empleado, String> colEmpSucursal;

    /** Columna de la tabla que muestra la cuenta bancaria (IBAN) del empleado. */
    @FXML
    private TableColumn<Empleado, String> colEmpIBAN;

    // Tabla Clientes

    /** Tabla secundaria donde se listan los clientes dados de alta en el sistema. */
    @FXML
    private TableView<Cliente> tablaClientes;

    /** Columna de la tabla que muestra el nombre del cliente. */
    @FXML
    private TableColumn<Cliente, String> colCliNombre;

    /** Columna de la tabla que muestra los apellidos del cliente. */
    @FXML
    private TableColumn<Cliente, String> colCliApellidos;

    /** Columna de la tabla que muestra el nombre de usuario de acceso del cliente. */
    @FXML
    private TableColumn<Cliente, String> colCliUsuario;

    /** Columna de la tabla que muestra el correo electrónico del cliente. */
    @FXML
    private TableColumn<Cliente, String> colCliEmail;

    /** Columna de la tabla que muestra el número de teléfono del cliente. */
    @FXML
    private TableColumn<Cliente, String> colCliTelefono;

    /** Columna de la tabla que muestra el DNI del cliente. */
    @FXML
    private TableColumn<Cliente, String> colCliDni;

    /** Columna de la tabla que muestra la dirección postal del cliente. */
    @FXML
    private TableColumn<Cliente, String> colCliDireccion;

    // CAMPOS DEL FORMULARIO INTEGRADO (EMPLEADOS)

    /** Contenedor desplegable que agrupa los campos para dar de alta a un nuevo empleado. */
    @FXML
    private VBox panelFormulario;

    /** Campo de texto para el nombre del nuevo empleado. */
    @FXML
    private TextField txtNombre;

    /** Campo de texto para los apellidos del nuevo empleado. */
    @FXML
    private TextField txtApellidos;

    /** Campo de texto para la dirección particular del nuevo empleado. */
    @FXML
    private TextField txtDireccion;

    /** Selector desplegable con las ubicaciones de las sucursales disponibles para asignar al empleado. */
    @FXML
    private ComboBox<String> cmbDireccionSucursal;

    /** Campo de texto para el código IBAN de la cuenta bancaria del nuevo empleado. */
    @FXML
    private TextField txtIban;

    /** Lista observable que respalda los datos mostrados en {@code tablaEmpleados}. */
    private final ObservableList<Empleado> listaEmpleados = FXCollections.observableArrayList();

    /** Lista observable que respalda los datos mostrados en {@code tablaClientes}. */
    private final ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    /** Objeto de acceso a datos para consultar, ascender y dar de baja empleados. */
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOImplementacion();

    /** Objeto de acceso a datos para listar clientes y eliminar sus datos sensibles. */
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    /** Objeto de acceso a datos para crear empleados y vincularlos a una sucursal. */
    private final AdministradorDAO administradorDAO = new AdministradorDAOImpl();

    /** Objeto de acceso a datos para obtener las sucursales disponibles en el selector. */
    private final SucursalDAO sucursalDAO = new SucursalDAOImplementacion();

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Vincula las columnas de {@code tablaEmpleados} y {@code tablaClientes} con las
     * propiedades de sus respectivas clases modelo, asocia las listas observables
     * y carga tanto los registros de la base de datos como el desplegable de sucursales.
     *
     * @param url Ubicación utilizada para resolver rutas relativas del objeto raíz, o {@code null} si no se conoce.
     * @param rb  Recursos utilizados para localizar el objeto raíz, o {@code null} si no se localizó.
     */
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

    /**
     * Consulta los listados completos de empleados y clientes desde MongoDB y actualiza
     * las listas observables vinculadas a ambas tablas de la interfaz.
     */
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

    /**
     * Recupera las sucursales registradas mediante {@link SucursalDAO#listarSucursales()}
     * y rellena el selector {@code cmbDireccionSucursal} con sus ubicaciones.
     */
    private void cargarComboSucursales() {
        try {
            cmbDireccionSucursal.getItems().clear();
            List<Sucursal> sucursales = sucursalDAO.listarSucursales();
            if (sucursales != null) {
                for (Sucursal s : sucursales) {
                    cmbDireccionSucursal.getItems().add(String.valueOf(s.getUbicacion()));
                }
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Muestra u oculta el panel integrado del formulario de alta de empleados.
     * Cuando se despliega, recarga la lista de sucursales disponibles en el selector.
     *
     * @param event Evento de acción disparado al pulsar el botón de alternar formulario.
     */
    @FXML
    public void btnAlternarFormulario(ActionEvent event) {
        boolean visible = panelFormulario.isVisible();
        panelFormulario.setVisible(!visible);
        panelFormulario.setManaged(!visible);
        if (!visible) {
            cargarComboSucursales();
        }
    }

    /**
     * Limpia los campos del formulario de alta de empleados y oculta el panel desplegable.
     */
    @FXML
    public void btnCancelarFormulario() {
        limpiarCampos();
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
    }

    /**
     * Manejador del botón para registrar un nuevo empleado en el sistema.
     * Valida que todos los campos estén completos, genera automáticamente el nombre de usuario
     * ({@code nombre.primerapellido}), la contraseña por defecto ({@code "1234"}) y el correo
     * corporativo ({@code @telonda.com}), inserta el empleado y lo vincula a la sucursal elegida.
     *
     * @param event Evento de acción disparado al pulsar el botón de crear empleado.
     */
    @FXML
    public void btnCrearEmpleado(ActionEvent event) {
        try {
            String nombre = txtNombre.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String direccion = txtDireccion.getText().trim();
            String sucursalTexto = cmbDireccionSucursal.getValue();
            String cuentaBancaria = txtIban.getText().trim();

            if (nombre.isEmpty() || apellidos.isEmpty() || direccion.isEmpty() || sucursalTexto == null || sucursalTexto.isEmpty() || cuentaBancaria.isEmpty()) {
                mostrarAlerta("Error", "Por favor, rellena todos los campos y selecciona una sucursal.");
                return;
            }

            String nombreN = nombre.toLowerCase().replaceAll("\\s+", "");
            String primerApellido = apellidos.split("\\s+")[0].toLowerCase();
            String usuario = nombreN + "." + primerApellido;

            boolean ok = administradorDAO.crearEmpleado(usuario, nombre, apellidos, "1234", usuario + "@telonda.com", cuentaBancaria, direccion, sucursalTexto);

            if (ok) {
                administradorDAO.agregarEmpleadoASucursal(sucursalTexto, usuario);

                mostrarAlerta("Perfecto", "Empleado creado correctamente.");
                cargarTablas();
                btnCancelarFormulario();
                limpiarCampos();
            } else {
                mostrarAlerta("Error", "No se pudo crear el empleado (quizás el usuario ya existe).");
            }

        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Vacía todos los campos de texto y reinicia la selección del desplegable de sucursales
     * en el formulario de creación de empleados.
     */
    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtDireccion.clear();
        cmbDireccionSucursal.getSelectionModel().clearSelection();
        txtIban.clear();
    }

    /**
     * Manejador del botón para promocionar al empleado seleccionado en {@code tablaEmpleados}
     * al rol de {@code "Admin"} mediante {@link EmpleadoDAO#ascenderEmpleado(String, String)}.
     *
     * @param event Evento de acción disparado al pulsar el botón de ascender a administrador.
     */
    @FXML
    public void btnAscenderAdmin(ActionEvent event) {
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

    /**
     * Manejador del botón para dar de baja al empleado seleccionado en {@code tablaEmpleados}
     * mediante {@link EmpleadoDAO#eliminarEmpleado(String)} y refrescar las tablas.
     *
     * @param event Evento de acción disparado al pulsar el botón de eliminar empleado.
     */
    @FXML
    public void btnEliminarEmpleado(ActionEvent event) {
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

    /**
     * Manejador del botón para eliminar los datos sensibles del cliente seleccionado en
     * {@code tablaClientes}. Solicita confirmación previa mediante un cuadro de diálogo
     * y ejecuta {@link ClienteDAO#eliminarDatosSensiblesCliente(String)}.
     *
     * @param event Evento de acción disparado al pulsar el botón de eliminar cliente.
     */
    @FXML
    public void btnEliminarCliente(ActionEvent event) {
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

    /**
     * Muestra un cuadro de diálogo modal informativo ({@link AlertType#INFORMATION})
     * con el título y mensaje especificados.
     *
     * @param titulo Título de la ventana de diálogo.
     * @param texto  Contenido textual que se mostrará al usuario.
     */
    private void mostrarAlerta(String titulo, String texto) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}