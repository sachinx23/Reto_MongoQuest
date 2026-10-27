package controlador;

import dao.DAO;
import dao.DAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.TicketSoporte;

/**
 * Panel de empleado: resolver tickets pendientes, aprobar solicitudes de baja
 * y validar peticiones de tarifa de los clientes.
 */
public class EmpleadoController implements Initializable {

    private static Empleado usuarioActual;

    public static void setUsuario(Empleado e) {
        usuarioActual = e;
    }

    private static final String OP_TICKETS = "Tickets pendientes";
    private static final String OP_BAJAS = "Solicitudes de baja";
    private static final String OP_VALIDAR = "Validar tarifas";
    private static final String OP_CERRAR = "Cerrar Sesion";

    private final DAO dao = new DAOImplementacion();

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<String> cmbOpciones;

    @FXML private AnchorPane panelPerfil;
    @FXML private AnchorPane panelTickets;
    @FXML private AnchorPane panelBajas;
    @FXML private AnchorPane panelValidar;

    // Perfil
    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;

    // Tickets pendientes
    @FXML private TableView<TicketSoporte> tablaTickets;
    @FXML private TableColumn<TicketSoporte, String> colTicketCliente;
    @FXML private TableColumn<TicketSoporte, String> colTicketAsunto;
    @FXML private TableColumn<TicketSoporte, String> colTicketDescripcion;
    @FXML private TableColumn<TicketSoporte, String> colTicketFecha;

    // Solicitudes de baja
    @FXML private TableView<Cliente> tablaBajas;
    @FXML private TableColumn<Cliente, String> colBajaCliente;
    @FXML private TableColumn<Cliente, String> colBajaDni;

    // Validar tarifas
    @FXML private TableView<Contrato> tablaPeticiones;
    @FXML private TableColumn<Contrato, String> colPeticionCliente;
    @FXML private TableColumn<Contrato, String> colPeticionTarifa;
    @FXML private TableColumn<Contrato, Double> colPeticionPrecio;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbOpciones.getItems().addAll(OP_TICKETS, OP_BAJAS, OP_VALIDAR, OP_CERRAR);

        if (usuarioActual != null) {
            lblBienvenida.setText("Bienvenido/a " + usuarioActual.getNombre());
        }

        configurarColumnas();
        ocultarTodo();
        panelPerfil.setVisible(true);
        cargarDatosPerfil();
    }

    /**
     * Enlaza cada columna con el atributo del modelo que muestra.
     * PropertyValueFactory llama al getter: "nombreCliente" -> getNombreCliente().
     */
    private void configurarColumnas() {
        colTicketCliente.setCellValueFactory(new PropertyValueFactory<TicketSoporte, String>("nombreCliente"));
        colTicketAsunto.setCellValueFactory(new PropertyValueFactory<TicketSoporte, String>("asunto"));
        colTicketDescripcion.setCellValueFactory(new PropertyValueFactory<TicketSoporte, String>("descripcion"));
        colTicketFecha.setCellValueFactory(new PropertyValueFactory<TicketSoporte, String>("fechaCreacion"));

        colBajaCliente.setCellValueFactory(new PropertyValueFactory<Cliente, String>("nombre"));
        colBajaDni.setCellValueFactory(new PropertyValueFactory<Cliente, String>("dni"));

        colPeticionCliente.setCellValueFactory(new PropertyValueFactory<Contrato, String>("nombreCliente"));
        colPeticionTarifa.setCellValueFactory(new PropertyValueFactory<Contrato, String>("nombreTarifa"));
        colPeticionPrecio.setCellValueFactory(new PropertyValueFactory<Contrato, Double>("precio"));
    }

    private void ocultarTodo() {
        panelPerfil.setVisible(false);
        panelTickets.setVisible(false);
        panelBajas.setVisible(false);
        panelValidar.setVisible(false);
    }

    private void cargarDatosPerfil() {
        if (usuarioActual != null) {
            txtNombre.setText(usuarioActual.getNombre());
            txtNombreUsuario.setText(usuarioActual.getNombreUsuario());
            txtContrasena.setText(usuarioActual.getContrasena());
            txtCorreo.setText(usuarioActual.getCorreo());
            txtCuentaBancaria.setText(usuarioActual.getCuentaBancaria());
        }
    }

    private void cargarTickets() {
        try {
            tablaTickets.setItems(FXCollections.observableArrayList(dao.obtenerTicketsPendientes()));
        } catch (OurException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarBajas() {
        try {
            tablaBajas.setItems(FXCollections.observableArrayList(dao.obtenerClientesConBajaPendiente()));
        } catch (OurException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarPeticiones() {
        try {
            tablaPeticiones.setItems(FXCollections.observableArrayList(
                    dao.obtenerContratosPorEstado(DAOImplementacion.ESTADO_PENDIENTE)));
        } catch (OurException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void btnMiPerfil(ActionEvent event) {
        ocultarTodo();
        panelPerfil.setVisible(true);
        cargarDatosPerfil();
        cmbOpciones.getSelectionModel().clearSelection();
    }

    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        String seleccion = cmbOpciones.getValue();
        if (seleccion != null) {
            ocultarTodo();
            switch (seleccion) {
                case OP_TICKETS:
                    panelTickets.setVisible(true);
                    cargarTickets();
                    break;
                case OP_BAJAS:
                    panelBajas.setVisible(true);
                    cargarBajas();
                    break;
                case OP_VALIDAR:
                    panelValidar.setVisible(true);
                    cargarPeticiones();
                    break;
                case OP_CERRAR:
                    cerrarSesion(event);
                    break;
            }
            cmbOpciones.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        String error = ValidarPerfil.comprobar(txtNombre.getText(), txtNombreUsuario.getText(),
                txtContrasena.getText(), txtCorreo.getText());
        if (error != null) {
            mostrarInfo("Datos inválidos", error);
            return;
        }
        if (mostrarConfirmacion("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            usuarioActual.setNombre(txtNombre.getText().trim());
            usuarioActual.setNombreUsuario(txtNombreUsuario.getText().trim());
            usuarioActual.setContrasena(txtContrasena.getText().trim());
            usuarioActual.setCorreo(txtCorreo.getText().trim());
            usuarioActual.setCuentaBancaria(txtCuentaBancaria.getText().trim());
            try {
                dao.actualizarEmpleado(usuarioActual);
                mostrarInfo("Modificación exitosa", "Tus datos se han actualizado correctamente.");
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    @FXML
    public void btnResolverTicket(ActionEvent event) {
        TicketSoporte ticketSel = tablaTickets.getSelectionModel().getSelectedItem();
        if (ticketSel == null) {
            mostrarInfo("Aviso", "Selecciona un ticket de la tabla primero.");
            return;
        }
        if (mostrarConfirmacion("Actualizar ticket", "¿Deseas cambiar el estado de este ticket a 'Resuelto'?")) {
            try {
                dao.resolverTicket(ticketSel.getId(), usuarioActual.getId());
                mostrarInfo("Ticket resuelto", "El ticket se ha marcado como resuelto.");
                cargarTickets();
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    @FXML
    public void btnAprobarBaja(ActionEvent event) {
        Cliente clienteSel = tablaBajas.getSelectionModel().getSelectedItem();
        if (clienteSel == null) {
            mostrarInfo("Aviso", "Selecciona una solicitud de la tabla primero.");
            return;
        }
        if (mostrarConfirmacion("Tramitar baja", "¿Confirmas la rescisión de contrato y la baja definitiva de este cliente?")) {
            try {
                dao.aprobarBaja(clienteSel.getId(), usuarioActual.getId());
                mostrarInfo("Gestión completada", "La operación sobre la cuenta del cliente se ha procesado con éxito.");
                cargarBajas();
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    @FXML
    public void btnValidarTarifa(ActionEvent event) {
        Contrato peticionSel = tablaPeticiones.getSelectionModel().getSelectedItem();
        if (peticionSel == null) {
            mostrarInfo("Aviso", "Selecciona una petición de la tabla primero.");
            return;
        }
        if (mostrarConfirmacion("Validar tarifa", "¿Validar y activar esta tarifa para el cliente seleccionado?")) {
            try {
                dao.validarContrato(peticionSel.getId(), usuarioActual.getId());
                mostrarInfo("Tarifa validada", "La tarifa se ha validado y el contrato está activo.");
                cargarPeticiones();
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    private void cerrarSesion(ActionEvent event) {
        if (mostrarConfirmacion("Salir", "¿Deseas salir del panel de empleado?")) {
            usuarioActual = null;
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Optional<ButtonType> respuesta = alert.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private void mostrarInfo(String titulo, String mensaje) {
        ShowAlert.showAlert(titulo, mensaje, Alert.AlertType.INFORMATION);
    }

    private void mostrarError(String mensaje) {
        ShowAlert.showAlert("Error", mensaje, Alert.AlertType.ERROR);
    }
}
