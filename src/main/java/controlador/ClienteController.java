package controlador;

import daocliente.ClienteDAO;
import daocliente.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import modelocliente.Cliente;

/**
 * Panel de cliente: cabecera con el desplegable (mismo estilo que los paneles de Eneko). Cada apartado es una
 * pantalla (FXML) con su controlador que se carga dentro de panelContenido.
 * El login es el de Eneko: guarda el id del usuario en IniciarSesionController.idUsuarioActual.
 */
public class ClienteController {
    // La guarda para que Mi Perfil pueda cambiar el nombre de la cabecera después de modificarlo
    private static Label bienvenida;

    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<OpcionCliente> cmbOpciones;
    @FXML private StackPane panelContenido;

    // Apartado que se está viendo (si se cancela el cierre de sesión, el desplegable vuelve a él)
    private OpcionCliente actual;

    /**
     * Busca el cliente que ha iniciado sesión, pone su nombre en la cabecera, rellena el desplegable y abre
     * Mi Perfil.
     */
    @FXML
    public void initialize() {
        bienvenida = lblBienvenida;
        try {
            Cliente cliente = clienteDAO.obtenerCliente(IniciarSesionController.idUsuarioActual);
            Sesion.setUsuario(cliente);
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
        }
        if (Sesion.getUsuario() == null) {
            return;
        }
        actualizarBienvenida();
        cmbOpciones.setItems(FXCollections.observableArrayList(OpcionCliente.values()));
        // Al entrar se muestra el perfil; el desplegable siempre indica en qué apartado estás
        cmbOpciones.setValue(OpcionCliente.PERFIL);
        if (actual == null) {
            mostrar(OpcionCliente.PERFIL);
        }
    }

    /**
     * Vuelve a escribir el nombre de la cabecera (después de modificar el perfil).
     */
    public static void actualizarBienvenida() {
        bienvenida.setText("Cliente: " + Sesion.getUsuario().getNombre());
    }

    /**
     * Carga la pantalla del apartado dentro del panel.
     *
     * @param opcion apartado del desplegable
     */
    private void mostrar(OpcionCliente opcion) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/vista/" + opcion.getVista() + ".fxml"));
            panelContenido.getChildren().setAll(vista);
            actual = opcion;
        } catch (IOException | RuntimeException e) {
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }

    /**
     * Abre el apartado elegido en el desplegable, o cierra la sesión.
     *
     * @param event evento de la acción (botón, menú o desplegable)
     */
    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        OpcionCliente elegida = cmbOpciones.getValue();
        if (elegida == null || elegida == actual) {
            return;
        }
        if (elegida == OpcionCliente.CERRAR_SESION) {
            cerrarSesion();
            return;
        }
        mostrar(elegida);
    }

    /**
     * Pide confirmación y vuelve a la pantalla de inicio de sesión de Eneko. Si cancela, el desplegable vuelve
     * al apartado que estaba viendo.
     */
    private void cerrarSesion() {
        if (!ShowAlert.confirmar("Salir", "¿Deseas salir del panel de cliente?")) {
            cmbOpciones.setValue(actual);
            return;
        }
        try {
            Sesion.cerrar();
            Parent login = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
            Stage stage = (Stage) panelContenido.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.show();
        } catch (IOException | RuntimeException e) {
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }
}
