package controlador;

import com.mycompany.reto_1.Reto_1;
import dao.DAO;
import exception.OurException;
import exception.ShowAlert;
import java.time.LocalDate;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import modelo.Cliente;
import modelo.Contrato;
import modelo.EstadoContrato;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Valoracion;

public class ClienteController {

    private static final Logger LOGGER = Logger.getLogger(ClienteController.class.getName());

    private final DAO dao = Sesion.getDao();

    private Cliente cliente;

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<OpcionCliente> cmbOpciones;

    @FXML private AnchorPane panelPerfil;
    @FXML private AnchorPane panelTarifas;
    @FXML private AnchorPane panelContratos;
    @FXML private AnchorPane panelSoporte;
    @FXML private AnchorPane panelValoraciones;

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtDni;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCuentaBancaria;

    @FXML private TextField txtSoporteAsunto;
    @FXML private TextField txtSoporteMensaje;

    @FXML private TableView<Tarifa> tablaTarifas;
    @FXML private TableColumn<Tarifa, String> colTarifaNombre;
    @FXML private TableColumn<Tarifa, Double> colTarifaPrecio;
    @FXML private TableColumn<Tarifa, String> colTarifaDetalles;
    @FXML private TableColumn<Tarifa, String> colTarifaTipo;

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, String> colContratoTelefono;
    @FXML private TableColumn<Contrato, EstadoContrato> colContratoEstado;
    @FXML private TableColumn<Contrato, String> colContratoFecha;
    @FXML private TableColumn<Contrato, String> colContratoIdTarifa;

    @FXML private TableView<TicketSoporte> tablaSoporte;
    @FXML private TableColumn<TicketSoporte, String> colSoporteAsunto;
    @FXML private TableColumn<TicketSoporte, String> colSoporteFecha;

    @FXML private TableView<Valoracion> tablaValoraciones;
    @FXML private TableColumn<Valoracion, Integer> colValoracionPuntuacion;
    @FXML private TableColumn<Valoracion, String> colValoracionTexto;
    @FXML private ComboBox<Integer> cmbPuntuacion;
    @FXML private TextField txtValoracion;

    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        lblBienvenida.setText("Bienvenido/a " + cliente.getNombre());

        cmbOpciones.setItems(FXCollections.observableArrayList(OpcionCliente.values()));
        cmbPuntuacion.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));

        // PropertyValueFactory usa el getter: "nombreTarifa" llama a getNombreTarifa()
        colTarifaNombre.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colTarifaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colTarifaDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));
        colTarifaTipo.setCellValueFactory(new PropertyValueFactory<>("tipoTarifa"));

        colContratoTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colContratoEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colContratoFecha.setCellValueFactory(new PropertyValueFactory<>("fechaFinalizacion"));
        colContratoIdTarifa.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));

        colSoporteAsunto.setCellValueFactory(new PropertyValueFactory<>("asunto"));
        colSoporteFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        colValoracionPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colValoracionTexto.setCellValueFactory(new PropertyValueFactory<>("texto"));
        // Las celdas de texto se pueden editar con doble clic
        colValoracionTexto.setCellFactory(TextFieldTableCell.forTableColumn());

        // Al entrar se muestra el perfil; el desplegable siempre indica en qué apartado estás
        cmbOpciones.setValue(OpcionCliente.PERFIL);
        mostrarPerfil();
    }

    private void mostrarPerfil() {
        mostrarPanel(panelPerfil);
        cargarPerfil();
    }

    private void mostrarPanel(AnchorPane panel) {
        panelPerfil.setVisible(false);
        panelTarifas.setVisible(false);
        panelContratos.setVisible(false);
        panelSoporte.setVisible(false);
        panelValoraciones.setVisible(false);
        panel.setVisible(true);
    }

    private void cargarPerfil() {
        txtNombre.setText(cliente.getNombre());
        txtNombreUsuario.setText(cliente.getNombreUsuario());
        txtDni.setText(cliente.getDni());
        txtCorreo.setText(cliente.getCorreo());
        txtDireccion.setText(cliente.getDireccion());
        txtTelefono.setText(cliente.getTelefono());
        txtCuentaBancaria.setText(cliente.getCuentaBancaria());
    }

    private void cargarTarifas() {
        try {
            tablaTarifas.setItems(FXCollections.observableArrayList(dao.obtenerTarifas()));
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    private void cargarContratos() {
        try {
            tablaContratos.setItems(FXCollections.observableArrayList(dao.obtenerContratosCliente(cliente.getId())));
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    private void cargarTickets() {
        try {
            tablaSoporte.setItems(FXCollections.observableArrayList(dao.obtenerTicketsCliente(cliente.getId())));
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    private void cargarValoraciones() {
        try {
            tablaValoraciones.setItems(FXCollections.observableArrayList(dao.obtenerValoracionesCliente(cliente.getId())));
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    @FXML
    public void btnMiPerfil(ActionEvent event) {
        cmbOpciones.setValue(OpcionCliente.PERFIL);
        mostrarPerfil();
    }

    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        OpcionCliente opcion = cmbOpciones.getValue();
        if (opcion == null) {
            return;
        }
        switch (opcion) {
            case PERFIL:
                mostrarPerfil();
                break;
            case TARIFAS:
                mostrarPanel(panelTarifas);
                cargarTarifas();
                break;
            case CONTRATOS:
                mostrarPanel(panelContratos);
                cargarContratos();
                break;
            case SOPORTE:
                mostrarPanel(panelSoporte);
                cargarTickets();
                break;
            case VALORACIONES:
                mostrarPanel(panelValoraciones);
                cargarValoraciones();
                break;
        }
    }

    @FXML
    public void btnCerrarSesion(ActionEvent event) {
        if (ShowAlert.confirmar("Salir", "¿Deseas salir del panel de cliente?")) {
            Sesion.cerrar();
            Reto_1.setRoot("IniciarSesion");
        }
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String dni = txtDni.getText().trim().toUpperCase();
        String correo = txtCorreo.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String cuenta = txtCuentaBancaria.getText().trim().toUpperCase();

        String error = ValidarPerfil.comprobarCliente(nombre, nombreUsuario, dni, correo, telefono, direccion, cuenta);
        if (error != null) {
            ShowAlert.info("Datos inválidos", error);
            return;
        }
        if (!ShowAlert.confirmar("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            return;
        }

        // Se crea un cliente nuevo con los cambios: si falla al guardar, el de la sesión no se toca
        Cliente modificado = new Cliente(cliente.getId(), nombre, nombreUsuario, cliente.getContrasena(), correo,
                cuenta, dni, telefono, direccion);
        modificado.setDadoDeBaja(cliente.isDadoDeBaja());
        try {
            dao.actualizarCliente(modificado);
            cliente = modificado;
            Sesion.setUsuario(modificado);
            lblBienvenida.setText("Bienvenido/a " + cliente.getNombre());
            ShowAlert.info("Modificación exitosa", "Los datos se han actualizado correctamente.");
        } catch (OurException e) {
            mostrarError(e);
            cargarPerfil();
        }
    }

    @FXML
    public void btnCambiarTarifa(ActionEvent event) {
        Tarifa tarifa = tablaTarifas.getSelectionModel().getSelectedItem();
        if (tarifa == null) {
            ShowAlert.info("Aviso", "Por favor, selecciona una tarifa de la tabla primero.");
            return;
        }
        try {
            // No se puede pedir otra vez una tarifa que ya está pendiente o activa
            for (Contrato c : dao.obtenerContratosCliente(cliente.getId())) {
                boolean mismaTarifa = c.getIdTarifa().equals(tarifa.getId());
                boolean enVigor = c.getEstado() == EstadoContrato.PENDIENTE
                        || c.getEstado() == EstadoContrato.ACTIVO;
                if (mismaTarifa && enVigor) {
                    ShowAlert.info("Aviso", "Ya tienes la tarifa " + tarifa.getNombreTarifa() + " pedida o contratada.");
                    return;
                }
            }
        } catch (OurException e) {
            mostrarError(e);
            return;
        }
        if (!ShowAlert.confirmar("Cambio de tarifa", "¿Estás seguro de que deseas contratar la tarifa " + tarifa.getNombreTarifa() + "?")) {
            return;
        }
        // Se guarda como petición pendiente: un empleado la valida en "Validar tarifas"
        LocalDate hoy = LocalDate.now();
        Contrato contrato = new Contrato(null, cliente.getTelefono(), EstadoContrato.PENDIENTE, cliente.getId(),
                tarifa.getId(), null, hoy.toString(), hoy.plusYears(1).toString());
        contrato.setNombreTarifa(tarifa.getNombreTarifa());
        contrato.setPrecio(tarifa.getPrecio());
        try {
            dao.insertarContrato(contrato);
            ShowAlert.info("Petición enviada", "Tu petición de la tarifa " + tarifa.getNombreTarifa()
                    + " está pendiente de validar por un empleado.");
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    @FXML
    public void btnEliminarTarifa(ActionEvent event) {
        Tarifa tarifa = tablaTarifas.getSelectionModel().getSelectedItem();
        if (tarifa == null) {
            ShowAlert.info("Aviso", "Por favor, selecciona una tarifa de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar eliminación", "¿Seguro que deseas eliminar tu petición de la tarifa " + tarifa.getNombreTarifa() + "?")) {
            return;
        }
        try {
            // Solo se puede eliminar una petición que todavía no ha validado ningún empleado
            boolean borrada = dao.borrarPeticion(cliente.getId(), tarifa.getId());
            if (borrada) {
                ShowAlert.info("Tarifa eliminada", "Tu petición de la tarifa " + tarifa.getNombreTarifa() + " ha sido eliminada.");
            } else {
                ShowAlert.info("Aviso", "No tienes ninguna petición pendiente de esa tarifa.");
            }
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    @FXML
    public void btnFirmarContrato(ActionEvent event) {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();
        if (contrato == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        // El contrato queda firmado cuando un empleado valida la tarifa (pasa a "activo")
        if (contrato.getEstado() == EstadoContrato.ACTIVO) {
            ShowAlert.info("Contrato firmado", "Este contrato ya está firmado y activo.");
        } else if (contrato.getEstado() == EstadoContrato.PENDIENTE) {
            ShowAlert.info("Contrato pendiente", "Este contrato se firmará cuando un empleado valide la tarifa.");
        } else {
            ShowAlert.info("Aviso", "Este contrato no se puede firmar porque está " + contrato.getEstado() + ".");
        }
    }

    @FXML
    public void btnRescindirContrato(ActionEvent event) {
        Contrato contrato = tablaContratos.getSelectionModel().getSelectedItem();
        if (contrato == null) {
            ShowAlert.info("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        if (contrato.getEstado() != EstadoContrato.ACTIVO) {
            ShowAlert.info("Aviso", "Solo se puede pedir la baja de un contrato activo.");
            return;
        }
        if (!ShowAlert.confirmar("Baja de compañía", "¿Estás seguro de solicitar la baja definitiva y rescindir tu contrato?")) {
            return;
        }
        try {
            // Queda en "baja pendiente" hasta que un empleado la apruebe
            dao.solicitarBaja(contrato.getId());
            ShowAlert.info("Solicitud enviada", "Tu solicitud de baja está pendiente de aprobar por un empleado.");
            cargarContratos();
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    @FXML
    public void btnEnviarTicket(ActionEvent event) {
        String asunto = txtSoporteAsunto.getText().trim();
        String descripcion = txtSoporteMensaje.getText().trim();
        if (asunto.isEmpty() || descripcion.isEmpty()) {
            ShowAlert.info("Campos incompletos", "Rellena el asunto y la descripción del ticket.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar envío", "¿Seguro que deseas enviar este ticket?")) {
            return;
        }
        TicketSoporte ticket = new TicketSoporte(null, asunto, descripcion, LocalDate.now().toString(), "pendiente",
                cliente.getId(), null);
        try {
            dao.insertarTicket(ticket);
            ShowAlert.info("Ticket enviado", "Tu consulta ha sido enviada al departamento de soporte con éxito.");
            txtSoporteAsunto.clear();
            txtSoporteMensaje.clear();
            cargarTickets();
        } catch (OurException e) {
            mostrarError(e);
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
        if (!valoracionValida()) {
            return;
        }
        if (!ShowAlert.confirmar("Confirmar valoración", "¿Seguro que deseas publicar esta valoración?")) {
            return;
        }
        Valoracion valoracion = new Valoracion(null, cmbPuntuacion.getValue(), txtValoracion.getText().trim(), cliente.getId(), null);
        try {
            dao.insertarValoracion(valoracion);
            ShowAlert.info("Valoración publicada", "Tu valoración ha sido registrada correctamente. ¡Gracias por tu opinión!");
            limpiarValoracion();
            cargarValoraciones();
        } catch (OurException e) {
            mostrarError(e);
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

    // Se llama al terminar de editar una celda de texto en la tabla
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
            dao.actualizarValoracion(valoracion);
            ShowAlert.info("Modificación exitosa", "Tu valoración se ha actualizado.");
            limpiarValoracion();
        } catch (OurException e) {
            mostrarError(e);
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
            dao.borrarValoracion(valoracion.getId());
            ShowAlert.info("Valoración eliminada", "La valoración ha sido eliminada.");
            limpiarValoracion();
            cargarValoraciones();
        } catch (OurException e) {
            mostrarError(e);
        }
    }

    private void limpiarValoracion() {
        cmbPuntuacion.setValue(null);
        txtValoracion.clear();
    }

    // Apunta el error en el log y se lo enseña al usuario
    private void mostrarError(OurException e) {
        LOGGER.warning(e.getMessage());
        ShowAlert.error(e.getMessage());
    }
}
