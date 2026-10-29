package controlador;

import com.mycompany.reto_1.Reto_1;
import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.EstadoContrato;
import modelo.TicketSoporte;

public class EmpleadoController {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoController.class.getName());

    private final DAO dao = Sesion.getDao();

    private Empleado empleado;

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<OpcionEmpleado> cmbOpciones;

    @FXML private AnchorPane panelPerfil;
    @FXML private AnchorPane panelTickets;
    @FXML private AnchorPane panelBajas;
    @FXML private AnchorPane panelValidar;

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCuentaBancaria;

    @FXML private TableView<TicketSoporte> tablaTickets;
    @FXML private TableColumn<TicketSoporte, String> colTicketCliente;
    @FXML private TableColumn<TicketSoporte, String> colTicketAsunto;
    @FXML private TableColumn<TicketSoporte, String> colTicketDescripcion;
    @FXML private TableColumn<TicketSoporte, String> colTicketFecha;

    @FXML private TableView<Cliente> tablaBajas;
    @FXML private TableColumn<Cliente, String> colBajaCliente;
    @FXML private TableColumn<Cliente, String> colBajaDni;

    @FXML private TableView<Contrato> tablaPeticiones;
    @FXML private TableColumn<Contrato, String> colPeticionCliente;
    @FXML private TableColumn<Contrato, String> colPeticionTarifa;
    @FXML private TableColumn<Contrato, Double> colPeticionPrecio;

    @FXML
    public void initialize() {
        empleado = (Empleado) Sesion.getUsuario();
        lblBienvenida.setText("Bienvenido/a " + empleado.getNombre());
        cmbOpciones.setItems(FXCollections.observableArrayList(OpcionEmpleado.values()));

        colTicketCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colTicketAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colTicketDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colTicketFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        colBajaCliente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colBajaDni.setCellValueFactory(new PropertyValueFactory<>("dni"));

        colPeticionCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPeticionTarifa.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colPeticionPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        // Al entrar se muestra el perfil; el desplegable siempre indica en qué apartado estás
        cmbOpciones.setValue(OpcionEmpleado.PERFIL);
        mostrarPerfil();
    }

    private void mostrarPerfil() {
        mostrarPanel(panelPerfil);
        cargarPerfil();
    }

    private void mostrarPanel(AnchorPane panel) {
        panelPerfil.setVisible(false);
        panelTickets.setVisible(false);
        panelBajas.setVisible(false);
        panelValidar.setVisible(false);
        panel.setVisible(true);
    }

    private void cargarPerfil() {
        txtNombre.setText(empleado.getNombre());
        txtNombreUsuario.setText(empleado.getNombreUsuario());
        txtContrasena.setText(empleado.getContrasena());
        txtCorreo.setText(empleado.getCorreo());
        txtCuentaBancaria.setText(empleado.getCuentaBancaria());
    }

    private void cargarTickets() {
        try {
            tablaTickets.setItems(FXCollections.observableArrayList(dao.obtenerTicketsPendientes()));
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    private void cargarBajas() {
        try {
            tablaBajas.setItems(FXCollections.observableArrayList(dao.obtenerClientesConBajaPendiente()));
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    private void cargarPeticiones() {
        try {
            tablaPeticiones.setItems(FXCollections.observableArrayList(dao.obtenerContratosPorEstado(EstadoContrato.PENDIENTE)));
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    @FXML
    public void btnMiPerfil(ActionEvent event) {
        cmbOpciones.setValue(OpcionEmpleado.PERFIL);
        mostrarPerfil();
    }

    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        OpcionEmpleado opcion = cmbOpciones.getValue();
        if (opcion == null) {
            return;
        }
        switch (opcion) {
            case PERFIL:
                mostrarPerfil();
                break;
            case TICKETS:
                mostrarPanel(panelTickets);
                cargarTickets();
                break;
            case BAJAS:
                mostrarPanel(panelBajas);
                cargarBajas();
                break;
            case VALIDAR:
                mostrarPanel(panelValidar);
                cargarPeticiones();
                break;
        }
    }

    @FXML
    public void btnCerrarSesion(ActionEvent event) {
        if (ShowAlert.confirmar("Salir", "¿Deseas salir del panel de empleado?")) {
            Sesion.cerrar();
            Reto_1.setRoot("IniciarSesion");
        }
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        String error = ValidarPerfil.comprobar(txtNombre.getText(), txtNombreUsuario.getText(),
                txtContrasena.getText(), txtCorreo.getText());
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        String cuenta = txtCuentaBancaria.getText().trim().toUpperCase();
        if (!ValidarPerfil.cuentaValida(cuenta)) {
            ShowAlert.info("Datos inválidos", "La cuenta bancaria debe ser ES seguido de 22 números (o dejarla vacía).");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            return;
        }

        // Se crea un empleado nuevo con los cambios: si falla al guardar, el de la sesión no se toca
        Empleado modificado = new Empleado(empleado.getId(), txtNombre.getText().trim(), txtNombreUsuario.getText().trim(),
                txtContrasena.getText().trim(), txtCorreo.getText().trim(), cuenta, empleado.getIdSucursal());
        try {
            dao.actualizarEmpleado(modificado);
            empleado = modificado;
            Sesion.setUsuario(modificado);
            lblBienvenida.setText("Bienvenido/a " + empleado.getNombre());
            ShowAlert.info("Modificación exitosa", "Tus datos se han actualizado correctamente.");
        } catch (OurException e) {
            mostrarError(e);
            cargarPerfil();
        }
    }

    @FXML
    public void btnResolverTicket(ActionEvent event) {
        TicketSoporte ticket = tablaTickets.getSelectionModel().getSelectedItem();
        if (ticket == null) {
            ShowAlert.info("Aviso", "Selecciona un ticket de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Actualizar ticket", "¿Deseas cambiar el estado de este ticket a 'Resuelto'?")) {
            return;
        }
        try {
            dao.resolverTicket(ticket.getId(), empleado.getId());
            ShowAlert.info("Ticket resuelto", "El ticket se ha marcado como resuelto.");
            cargarTickets();
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    @FXML
    public void btnAprobarBaja(ActionEvent event) {
        Cliente cliente = tablaBajas.getSelectionModel().getSelectedItem();
        if (cliente == null) {
            ShowAlert.info("Aviso", "Selecciona una solicitud de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Tramitar baja", "¿Confirmas la rescisión de contrato y la baja definitiva de este cliente?")) {
            return;
        }
        try {
            dao.aprobarBaja(cliente.getId(), empleado.getId());
            ShowAlert.info("Gestión completada", "La operación sobre la cuenta del cliente se ha procesado con éxito.");
            cargarBajas();
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    @FXML
    public void btnValidarTarifa(ActionEvent event) {
        Contrato peticion = tablaPeticiones.getSelectionModel().getSelectedItem();
        if (peticion == null) {
            ShowAlert.info("Aviso", "Selecciona una petición de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Validar tarifa", "¿Validar y activar esta tarifa para el cliente seleccionado?")) {
            return;
        }
        try {
            dao.validarContrato(peticion.getId(), empleado.getId());
            ShowAlert.info("Tarifa validada", "La tarifa se ha validado y el contrato está activo.");
            cargarPeticiones();
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    // Apunta el error en el log y se lo enseña al usuario
    private void mostrarError(OurException e) {
        LOGGER.warning(e.getMessage());
        ShowAlert.error(e.getMessage());
    }
}
