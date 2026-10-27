package controlador;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.bson.Document;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;

/**
 * Controlador para la vista GestionarPlantilla.fxml
 *
 * @author eneko
 */
public class GestionarPlantillaController implements Initializable {

    // TABLA EMPLEADOS
    @FXML
    private TableView<Document> tablaEmpleados;
    @FXML
    private TableColumn<Document, String> colEmpNombre;
    @FXML
    private TableColumn<Document, String> colEmpApellidos;
    @FXML
    private TableColumn<Document, String> colEmpUsuario;
    @FXML
    private TableColumn<Document, String> colEmpRol;
    @FXML
    private TableColumn<Document, String> colEmpEmail;
    @FXML
    private TableColumn<Document, String> colEmpIBAN;

    // TABLA CLIENTES
    @FXML
    private TableView<Document> tablaClientes;
    @FXML
    private TableColumn<Document, String> colCliNombre;
    @FXML
    private TableColumn<Document, String> colCliApellidos;
    @FXML
    private TableColumn<Document, String> colCliUsuario;
    @FXML
    private TableColumn<Document, String> colCliEmail;
    @FXML
    private TableColumn<Document, String> colCliTelefono;

    private ObservableList<Document> listaEmpleados = FXCollections.observableArrayList();
    private ObservableList<Document> listaClientes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración de las columnas con los campos del Document
        colEmpNombre.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("nombre")));
        colEmpApellidos.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("apellidos")));
        colEmpUsuario.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("nombreUsuario")));
        colEmpRol.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("rol")));
        colEmpIBAN.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("cuentaBancaria")));

        colEmpEmail.setCellValueFactory(fila -> {
            String mail = fila.getValue().getString("correo");
            if (mail == null) {
                mail = fila.getValue().getString("email");
            }
            return new SimpleStringProperty(mail != null ? mail : "");
        });

        colCliNombre.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("nombre")));
        colCliApellidos.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("apellidos")));
        colCliUsuario.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("nombreUsuario")));

        colCliEmail.setCellValueFactory(fila -> {
            String mail = fila.getValue().getString("correo");
            if (mail == null) {
                mail = fila.getValue().getString("email");
            }
            return new SimpleStringProperty(mail != null ? mail : "");
        });

        colCliTelefono.setCellValueFactory(fila -> new SimpleStringProperty(fila.getValue().getString("telefono")));

        // Cargar las dos tablas
        cargarTablas();
    }

    private void cargarTablas() {
        listaEmpleados.clear();
        listaClientes.clear();

        // USAMOS TU MÉTODO EXACTO DEL DAO: obtenerPlantilla()
        List<Document> todosLosUsuarios = DAO.obtenerPlantilla();

        if (todosLosUsuarios != null) {
            for (Document doc : todosLosUsuarios) {
                String rol = doc.getString("rol");

                if ("Empleado".equals(rol)) {
                    listaEmpleados.add(doc);
                } else if ("Cliente".equals(rol)) {
                    listaClientes.add(doc);
                }
            }
        }

        tablaEmpleados.setItems(listaEmpleados);
        tablaClientes.setItems(listaClientes);
    }

    // Ascender empleado a admin
    @FXML
    public void ascenderAAdmin(ActionEvent event) {
        Document seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            String usuario = seleccionado.getString("nombreUsuario");

            // USAMOS TU MÉTODO EXACTO DEL DAO: ascenderEmpleado(...)
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
        // Pedir Nombre
        TextInputDialog dNombre = new TextInputDialog();
        dNombre.setTitle("Nuevo Empleado");
        dNombre.setContentText("Introduce el Nombre:");
        Optional<String> resNombre = dNombre.showAndWait();
        if (!resNombre.isPresent() || resNombre.get().trim().isEmpty()) return;
        String nombre = resNombre.get().trim();

        //  Pedir Apellidos
        TextInputDialog dApellidos = new TextInputDialog();
        dApellidos.setTitle("Nuevo Empleado");
        dApellidos.setContentText("Introduce los Apellidos:");
        Optional<String> resApellidos = dApellidos.showAndWait();
        if (!resApellidos.isPresent() || resApellidos.get().trim().isEmpty()) return;
        String apellidos = resApellidos.get().trim();

        // Pedir Cuenta Bancaria
        TextInputDialog nIBAN = new TextInputDialog();
        nIBAN.setTitle("Nuevo Empleado");
        nIBAN.setContentText("Introduce la Cuenta Bancaria (IBAN):");
        Optional<String> resIBAN = nIBAN.showAndWait();
        if (!resIBAN.isPresent() || resIBAN.get().trim().isEmpty()) return;
        String cuentaBancaria = resIBAN.get().trim();

        // Generamos el nombreUsuario automático (ej: pablodiaz)
        String usuario = (nombre + apellidos.split(" ")[0]).toLowerCase().replaceAll("\\s+", "");

        //Llamada al DAO con los 6 parámetros exactos
        boolean ok = DAO.crearEmpleado(usuario, nombre, apellidos, "1234", usuario + "@telonda.com", cuentaBancaria);

        if (ok) {
            mostrarAlerta("Éxito", "Empleado" + nombre + " " + apellidos + " creado correctamente.");
            cargarTablas();
        } else {
            mostrarAlerta("Error", "No se pudo crear el empleado (quizás el usuario ya existe).");
        }
    }
    @FXML
    public void eliminarEmpleado(ActionEvent event) {
        Document seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            String usuario = seleccionado.getString("nombreUsuario");

            // USAMOS TU MÉTODO EXACTO DEL DAO: eliminarEmpleado(...)
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
        Document seleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            String usuario = seleccionado.getString("nombreUsuario");

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

    private void mostrarAlerta(String titulo, String texto) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}
