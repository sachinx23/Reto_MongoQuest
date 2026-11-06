package controlador;

import com.mycompany.reto_1.Reto_1;
import exception.ShowAlert;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Panel de cliente: cabecera con el desplegable. Cada apartado es una pantalla (FXML) con su controlador
 * que se carga dentro de panelContenido, igual que en el panel de administrador.
 */
public class ClienteController {

    private static final Logger LOGGER = Logger.getLogger(ClienteController.class.getName());

    // La guarda para que Mi Perfil pueda cambiar el nombre de la cabecera después de modificarlo
    private static Label bienvenida;

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<OpcionCliente> cmbOpciones;
    @FXML private StackPane panelContenido;

    @FXML
    public void initialize() {
        bienvenida = lblBienvenida;
        actualizarBienvenida();
        cmbOpciones.setItems(FXCollections.observableArrayList(OpcionCliente.values()));
        // Al entrar se muestra el perfil; el desplegable siempre indica en qué apartado estás
        cmbOpciones.setValue(OpcionCliente.PERFIL);
        mostrar(OpcionCliente.PERFIL);
    }

    public static void actualizarBienvenida() {
        bienvenida.setText("Bienvenido/a " + Sesion.getUsuario().getNombre());
    }

    // Carga la pantalla del apartado dentro del panel
    private void mostrar(OpcionCliente opcion) {
        try {
            panelContenido.getChildren().setAll(Reto_1.loadFXML(opcion.getVista()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir la pantalla " + opcion.getVista(), e);
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }

    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        if (cmbOpciones.getValue() != null) {
            mostrar(cmbOpciones.getValue());
        }
    }

    @FXML
    public void btnMiPerfil(ActionEvent event) {
        if (cmbOpciones.getValue() == OpcionCliente.PERFIL) {
            // Ya estaba elegido: el desplegable no avisa, así que se vuelve a cargar a mano
            mostrar(OpcionCliente.PERFIL);
        } else {
            cmbOpciones.setValue(OpcionCliente.PERFIL);
        }
    }

    @FXML
    public void btnCerrarSesion(ActionEvent event) {
        Reto_1.cerrarSesion("cliente");
    }

    // Menú Archivo > Salir y Ayuda > Acerca de
    @FXML
    public void menuSalir(ActionEvent event) {
        Reto_1.salir();
    }

    @FXML
    public void menuAcercaDe(ActionEvent event) {
        Reto_1.acercaDe();
    }
}
