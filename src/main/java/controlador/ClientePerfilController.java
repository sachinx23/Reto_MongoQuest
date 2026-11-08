package controlador;

import java.util.logging.Logger;
import DAO.ClienteDAO;
import DAO.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import modelo.Cliente;

public class ClientePerfilController {

    private static final Logger LOGGER = Logger.getLogger(ClientePerfilController.class.getName());
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();
    private Cliente cliente;

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtDni;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCuentaBancaria;
    @FXML private TextField txtDireccion;

    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        cargarPerfil();
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

        Cliente modificado = new Cliente(cliente.getId(), cuenta, nombreUsuario, nombre, cliente.getApellidos(),
                cliente.getContrasena(), correo, dni, telefono, direccion);
        try {
            clienteDAO.actualizarCliente(modificado);
            cliente = modificado;
            ClienteController.setCliente(modificado);
            ClienteController.actualizarBienvenida();
            ShowAlert.info("Modificación exitosa", "Los datos se han actualizado correctamente.");
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
            cargarPerfil();
        }
    }
}
