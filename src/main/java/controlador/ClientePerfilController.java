package controlador;

import daocliente.ClienteDAO;
import daocliente.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import modelocliente.Cliente;

/**
 * Mi Perfil del cliente: ver y modificar sus datos.
 */
public class ClientePerfilController {
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();
    private Cliente cliente;

    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtDni;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCuentaBancaria;
    @FXML private TextField txtDireccion;

    /**
     * Se ejecuta al abrir la pantalla: rellena el formulario con los datos del cliente.
     */
    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        cargarPerfil();
    }

    /**
     * Rellena el formulario con los datos del cliente de la sesión.
     */
    private void cargarPerfil() {
        txtNombre.setText(cliente.getNombre());
        txtNombreUsuario.setText(cliente.getNombreUsuario());
        txtDni.setText(cliente.getDni());
        txtCorreo.setText(cliente.getCorreo());
        txtDireccion.setText(cliente.getDireccion());
        txtTelefono.setText(cliente.getTelefono());
        txtCuentaBancaria.setText(cliente.getCuentaBancaria());
    }

    /**
     * Comprueba los datos, pide confirmación y guarda el perfil.
     *
     * @param event evento de la acción (botón, menú o desplegable)
     */
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
            clienteDAO.actualizarCliente(modificado);
            cliente = modificado;
            Sesion.setUsuario(modificado);
            ClienteController.actualizarBienvenida();
            ShowAlert.info("Modificación exitosa", "Los datos se han actualizado correctamente.");
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
            cargarPerfil();
        }
    }
}
