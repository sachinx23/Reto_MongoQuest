package controlador;

import dao.DAO;
import dao.DAOImplementacion;
import exception.OurException;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Tarifa;
import modelo.TicketSoporte;

public class ClienteController implements Initializable {

    private static Cliente usuarioActual;

    public static void setUsuario(Cliente u) {
        usuarioActual = u;
    }

    private static final String OP_TARIFAS = "Mis Tarifas";
    private static final String OP_CONTRATOS = "Mis Contratos";
    private static final String OP_SOPORTE = "Soporte";
    private static final String OP_CERRAR = "Cerrar Sesion";

    private final DAO dao = new DAOImplementacion();

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<String> cmbOpciones;

    @FXML private AnchorPane panelPerfil;
    @FXML private AnchorPane panelTarifas;
    @FXML private AnchorPane panelContratos;
    @FXML private AnchorPane panelSoporte;

    // Perfil
    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtDni;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCuentaBancaria;

    // Soporte
    @FXML private TextField txtSoporteAsunto;
    @FXML private TextField txtSoporteMensaje;

    // Tablas
    @FXML private TableView<Tarifa> tablaTarifas;
    @FXML private TableColumn<Tarifa, String> colTarifaNombre;
    @FXML private TableColumn<Tarifa, Double> colTarifaPrecio;
    @FXML private TableColumn<Tarifa, String> colTarifaDetalles;
    @FXML private TableColumn<Tarifa, String> colTarifaTipo;

    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, String> colContratoTelefono;
    @FXML private TableColumn<Contrato, String> colContratoEstado;
    @FXML private TableColumn<Contrato, String> colContratoFecha;
    @FXML private TableColumn<Contrato, String> colContratoIdTarifa;

    @FXML private TableView<TicketSoporte> tablaSoporte;
    @FXML private TableColumn<TicketSoporte, String> colSoporteAsunto;
    @FXML private TableColumn<TicketSoporte, String> colSoporteFecha;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbOpciones.getItems().addAll(OP_TARIFAS, OP_CONTRATOS, OP_SOPORTE, OP_CERRAR);

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
     * PropertyValueFactory llama al getter: "nombreTarifa" -> getNombreTarifa().
     */
    private void configurarColumnas() {
        colTarifaNombre.setCellValueFactory(new PropertyValueFactory<Tarifa, String>("nombreTarifa"));
        colTarifaPrecio.setCellValueFactory(new PropertyValueFactory<Tarifa, Double>("precio"));
        colTarifaDetalles.setCellValueFactory(new PropertyValueFactory<Tarifa, String>("detalles"));
        colTarifaTipo.setCellValueFactory(new PropertyValueFactory<Tarifa, String>("tipoTarifa"));

        colContratoTelefono.setCellValueFactory(new PropertyValueFactory<Contrato, String>("telefono"));
        colContratoEstado.setCellValueFactory(new PropertyValueFactory<Contrato, String>("estado"));
        colContratoFecha.setCellValueFactory(new PropertyValueFactory<Contrato, String>("fechaFinalizacion"));
        colContratoIdTarifa.setCellValueFactory(new PropertyValueFactory<Contrato, String>("idTarifa"));

        colSoporteAsunto.setCellValueFactory(new PropertyValueFactory<TicketSoporte, String>("asunto"));
        colSoporteFecha.setCellValueFactory(new PropertyValueFactory<TicketSoporte, String>("fechaCreacion"));
    }

    private void ocultarTodo() {
        panelPerfil.setVisible(false);
        panelTarifas.setVisible(false);
        panelContratos.setVisible(false);
        panelSoporte.setVisible(false);
    }

    private void cargarDatosPerfil() {
        if (usuarioActual != null) {
            txtNombre.setText(usuarioActual.getNombre());
            txtNombreUsuario.setText(usuarioActual.getNombreUsuario());
            txtDni.setText(usuarioActual.getDni());
            txtCorreo.setText(usuarioActual.getCorreo());
            txtDireccion.setText(usuarioActual.getDireccion());
            txtTelefono.setText(usuarioActual.getTelefono());
            txtCuentaBancaria.setText(usuarioActual.getCuentaBancaria());
        }
    }

    private void cargarTarifasDesdeBD() {
        try {
            tablaTarifas.setItems(FXCollections.observableArrayList(dao.obtenerTarifas()));
        } catch (OurException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarContratosDesdeBD() {
        if (usuarioActual == null) {
            return;
        }
        try {
            tablaContratos.setItems(FXCollections.observableArrayList(dao.obtenerContratosCliente(usuarioActual.getId())));
        } catch (OurException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarTicketsDesdeBD() {
        if (usuarioActual == null) {
            return;
        }
        try {
            tablaSoporte.setItems(FXCollections.observableArrayList(dao.obtenerTicketsCliente(usuarioActual.getId())));
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
                case OP_TARIFAS:
                    panelTarifas.setVisible(true);
                    cargarTarifasDesdeBD();
                    break;
                case OP_CONTRATOS:
                    panelContratos.setVisible(true);
                    cargarContratosDesdeBD();
                    break;
                case OP_SOPORTE:
                    panelSoporte.setVisible(true);
                    cargarTicketsDesdeBD();
                    break;
                case OP_CERRAR:
                    cerrarSesion(event);
                    break;
            }
            cmbOpciones.getSelectionModel().clearSelection();
        }
    }

    private void cerrarSesion(ActionEvent event) {
        if (mostrarConfirmacion("Cerrar sesión", "¿Seguro que deseas cerrar sesión?")) {
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

    @FXML
    public void btnModificar(ActionEvent event) {
        if (usuarioActual == null) {
            mostrarInfo("Error", "No hay un usuario en sesión.");
            return;
        }

        String dni = txtDni.getText().trim();
        String correo = txtCorreo.getText().trim();
        if (txtNombre.getText().trim().isEmpty() || txtNombreUsuario.getText().trim().isEmpty()) {
            mostrarInfo("Aviso", "El nombre y el nombre de usuario son obligatorios.");
            return;
        }
        if (!dni.matches("[0-9]{8}[A-Z]")) {
            mostrarInfo("Aviso", "DNI introducido incorrectamente.");
            return;
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            mostrarInfo("Aviso", "El correo no es válido.");
            return;
        }

        if (mostrarConfirmacion("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            usuarioActual.setNombre(txtNombre.getText().trim());
            usuarioActual.setNombreUsuario(txtNombreUsuario.getText().trim());
            usuarioActual.setDni(dni);
            usuarioActual.setCorreo(correo);
            usuarioActual.setDireccion(txtDireccion.getText().trim());
            usuarioActual.setTelefono(txtTelefono.getText().trim());
            usuarioActual.setCuentaBancaria(txtCuentaBancaria.getText().trim());

            try {
                dao.actualizarCliente(usuarioActual);
                mostrarInfo("Modificación exitosa", "Los datos se han actualizado correctamente en la base de datos.");
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    @FXML
    public void btnCambiarTarifa(ActionEvent event) {
        if (usuarioActual == null) {
            mostrarInfo("Error", "No hay un usuario en sesión.");
            return;
        }

        Tarifa tarifaSel = tablaTarifas.getSelectionModel().getSelectedItem();
        if (tarifaSel == null) {
            mostrarInfo("Aviso", "Por favor, selecciona una tarifa de la tabla primero.");
            return;
        }

        if (mostrarConfirmacion("Confirmar contratación", "¿Seguro que deseas contratar la tarifa: " + tarifaSel.getNombreTarifa() + "?")) {
            LocalDate hoy = LocalDate.now();
            Contrato contrato = new Contrato(null, usuarioActual.getTelefono(), "activo", usuarioActual.getId(),
                    tarifaSel.getId(), null, hoy.toString(), hoy.plusYears(1).toString());
            try {
                dao.insertarContrato(contrato);
                mostrarInfo("Éxito", "La tarifa " + tarifaSel.getNombreTarifa() + " se ha añadido a tu cuenta correctamente.");
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    @FXML public void btnEliminarTarifa(ActionEvent event) {
        if (mostrarConfirmacion("Confirmar eliminación", "¿Seguro que deseas eliminar esta tarifa?")) {
            mostrarInfo("Tarifa eliminada", "La tarifa ha sido eliminada.");
        }
    }

    @FXML public void btnFirmarContrato(ActionEvent event) {
        if (mostrarConfirmacion("Confirmar firma", "¿Seguro que deseas firmar este contrato?")) {
            mostrarInfo("Contrato firmado", "El contrato ha sido firmado y activado.");
        }
    }

    @FXML public void btnRescindirContrato(ActionEvent event) {
        Contrato contratoSel = tablaContratos.getSelectionModel().getSelectedItem();
        if (contratoSel == null) {
            mostrarInfo("Aviso", "Selecciona un contrato de la tabla primero.");
            return;
        }
        if ("rescindido".equals(contratoSel.getEstado())) {
            mostrarInfo("Aviso", "Ese contrato ya está rescindido.");
            return;
        }
        if (mostrarConfirmacion("Confirmar rescisión", "¿Seguro que deseas rescindir este contrato?")) {
            try {
                dao.rescindirContrato(contratoSel.getId());
                mostrarInfo("Contrato rescindido", "El contrato se ha rescindido correctamente.");
                cargarContratosDesdeBD();
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    @FXML
    public void btnEnviarTicket(ActionEvent event) {
        if (usuarioActual == null) {
            mostrarInfo("Error", "No hay un usuario en sesión.");
            return;
        }

        String asunto = txtSoporteAsunto.getText().trim();
        String descripcion = txtSoporteMensaje.getText().trim();

        if (asunto.isEmpty() || descripcion.isEmpty()) {
            mostrarInfo("Aviso", "Rellena el asunto y la descripción del ticket.");
            return;
        }

        if (mostrarConfirmacion("Confirmar envío", "¿Seguro que deseas enviar este ticket?")) {
            TicketSoporte ticket = new TicketSoporte(null, asunto, descripcion, LocalDate.now().toString(),
                    "pendiente", usuarioActual.getId(), null);
            try {
                dao.insertarTicket(ticket);
                mostrarInfo("Ticket enviado", "Tu ticket de soporte ha sido guardado.");
                txtSoporteAsunto.setText("");
                txtSoporteMensaje.setText("");
                cargarTicketsDesdeBD();
            } catch (OurException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}