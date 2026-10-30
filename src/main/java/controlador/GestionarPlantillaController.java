package controlador;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Cliente;
import modelo.Empleado;

/**
 * Controlador para la vista GestionarPlantilla.fxml
 *
 * @author eneko
 */
public class GestionarPlantillaController implements Initializable {

    // TABLA EMPLEADOS
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
    private TableColumn<Empleado, String> colEmpIBAN;

    // TABLA CLIENTES
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

    private final ObservableList<Empleado> listaEmpleados = FXCollections.observableArrayList();
    private final ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mapeo de columnas con el POJO Empleado
        colEmpNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmpApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colEmpUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmpRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEmpEmail.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colEmpIBAN.setCellValueFactory(new PropertyValueFactory<>("cuentaBancaria"));

        // Mapeo de columnas con el POJO Cliente
        colCliNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCliApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colCliUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colCliEmail.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCliTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        tablaEmpleados.setItems(listaEmpleados);
        tablaClientes.setItems(listaClientes);

        cargarTablas();
    }

    private void cargarTablas() {
        listaEmpleados.clear();
        listaClientes.clear();

        // Llamadas a los métodos estáticos del DAO
        List<Empleado> empleados = DAO.listarEmpleados();
        List<Cliente> clientes = DAO.listarClientes();

        if (empleados != null) {
            listaEmpleados.addAll(empleados);
        }
        if (clientes != null) {
            listaClientes.addAll(clientes);
        }
    }

    // Ascender empleado a admin
    @FXML
    public void ascenderAAdmin(ActionEvent event) {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            String usuario = seleccionado.getNombreUsuario();

            boolean ok = DAO.ascenderEmpleado(usuario, "Admin");

            if (ok) {
                mostrarAlerta("Éxito", "El empleado " + usuario + " ahora es Administrador.");
                cargarTablas();
            } else {
                mostrarAlerta("Error", "No se pudo ascender al empleado.");
            }
        } else {
            mostrarAlerta("Atención", "Selecciona primero un empleado en la tabla.");
        }
    }

    // Crear Empleado
    @FXML
    public void crearEmpleado(ActionEvent event) {
        String nombre = pedirTexto("Introduce el Nombre:");
        if (nombre == null) {
            return;
        }
        String apellidos = pedirTexto("Introduce los Apellidos:");
        if (apellidos == null) {
            return;
        }
        String cuentaBancaria = pedirTexto("Introduce la Cuenta Bancaria (IBAN):");
        if (cuentaBancaria == null) {
            return;
        }

        // Generar nombreUsuario automáticamente (ejemplo: eneko.garcia)
        String nombreN = nombre.toLowerCase().replaceAll("\\s+", "");
        String primerApellido = apellidos.split("\\s+")[0].toLowerCase();
        String usuario = nombreN + "." + primerApellido;


        boolean ok = DAO.crearEmpleado(usuario, nombre, apellidos, "1234", usuario + "@telonda.com", cuentaBancaria);

        if (ok) {
            mostrarAlerta("Éxito", "Empleado " + nombre + " " + apellidos + " creado correctamente.");
            cargarTablas();
        } else {
            mostrarAlerta("Error", "No se pudo crear el empleado (quizás el usuario ya existe).");
        }
    }

    @FXML
    public void eliminarEmpleado(ActionEvent event) {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            String usuario = seleccionado.getNombreUsuario();

            boolean ok = DAO.eliminarEmpleado(usuario);

            if (ok) {
                mostrarAlerta("Éxito", "El empleado " + usuario + " ha sido dado de baja.");
                cargarTablas();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar al empleado.");
            }
        } else {
            mostrarAlerta("Atención", "Selecciona primero un empleado en la tabla.");
        }
    }

    @FXML
    public void eliminarCliente(ActionEvent event) {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            String usuario = seleccionado.getNombreUsuario();

            Alert confirmacion = new Alert(AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación de datos sensibles");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Desea eliminar permanentemente los datos sensibles del cliente " + usuario + "?");

            Optional<ButtonType> res = confirmacion.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                boolean ok = DAO.eliminarDatosSensiblesCliente(usuario);
                if (ok) {
                    mostrarAlerta("Éxito", "Datos sensibles del cliente " + usuario + " eliminados.");
                    cargarTablas();
                } else {
                    mostrarAlerta("Error", "No se pudieron borrar los datos.");
                }
            }
        } else {
            mostrarAlerta("Atención", "Selecciona primero un cliente de la tabla.");
        }
    }

    private String pedirTexto(String mensaje) {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Nuevo Empleado");
        dialogo.setContentText(mensaje);
        Optional<String> res = dialogo.showAndWait();
        if (!res.isPresent() || res.get().trim().isEmpty()) {
            return null;
        }
        return res.get().trim();
    }

    private void mostrarAlerta(String titulo, String texto) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}
