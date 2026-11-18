package controlador;

import log.LogApp;
import java.util.logging.Logger;
import DAO.ClienteDAO;
import DAO.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import modelo.Cliente;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Controlador principal de la vista del Cliente en JavaFX. Gestiona la
 * autenticación del cliente, la actualización de bienvenida y la navegación
 * dinámica entre las distintas subvistas (perfil, tarifas, contratos, soporte y
 * valoraciones), además del cierre de sesión.
 *
 * @author Eneko.Plaza
 */
public class ClienteController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ClienteController.class.getName());

    private static Cliente cliente;
    private static Label bienvenida;

    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    @FXML
    private ComboBox<String> cmbOpciones;

    @FXML
    private StackPane panelContenido;

    @FXML
    private Label lblBienvenida;

    private String vistaActual = "Mi Perfil";

    /**
     * Inicializa la vista del cliente. Si hay un usuario con sesión iniciada
     * ({@code IniciarSesionController.idUsuarioActual}), carga su cliente desde
     * la base de datos. Si no hay id de sesión, conserva el cliente fijado
     * previamente con {@link #setCliente(Cliente)} (por ejemplo, desde los
     * tests) y no consulta la base de datos.
     *
     * @param url Ubicación usada para resolver rutas relativas (no se usa).
     * @param rb Recursos de internacionalización (no se usan).
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bienvenida = lblBienvenida;

        String idUsuario = IniciarSesionController.idUsuarioActual;
        if (idUsuario != null) {
            cliente = null;
            try {
                cliente = clienteDAO.obtenerCliente(idUsuario);
            } catch (OurException e) {
                LOGGER.warning(e.getMessage());
                ShowAlert.error(e.getMessage());
            }
        }

        if (cliente == null) {
            return;
        }
        actualizarBienvenida();

        cmbOpciones.setItems(FXCollections.observableArrayList(
                "Mi Perfil",
                "Mis Tarifas",
                "Mis Contratos",
                "Soporte",
                "Mis Valoraciones",
                "Cerrar Sesión"
        ));

        cmbOpciones.getSelectionModel().select("Mi Perfil");
        cargarSubvista("/vista/ClientePerfil.fxml");
    }

    /**
     * Devuelve el cliente que ha iniciado sesión.
     *
     * @return Cliente actual, o {@code null} si no hay ninguno.
     */
    public static Cliente getCliente() {
        return cliente;
    }

    /**
     * Fija el cliente actual.
     *
     * @param nuevo Cliente que pasa a ser el actual.
     */
    public static void setCliente(Cliente nuevo) {
        cliente = nuevo;
    }

    /**
     * Actualiza la etiqueta de bienvenida con el nombre del cliente actual.
     */
    public static void actualizarBienvenida() {
        if (bienvenida != null && cliente != null) {
            bienvenida.setText("Cliente: " + cliente.getNombre());
        }
    }

    /**
     * Cambia la subvista mostrada según la opción elegida en el desplegable.
     *
     * @param event Evento de acción generado por el desplegable.
     */
    @FXML
    public void cambiarSubVista(ActionEvent event) {
        String seleccion = cmbOpciones.getValue();
        if (seleccion == null) {
            return;
        }

        switch (seleccion) {
            case "Mi Perfil":
                vistaActual = "Mi Perfil";
                cargarSubvista("/vista/ClientePerfil.fxml");
                break;
            case "Mis Tarifas":
                vistaActual = "Mis Tarifas";
                cargarSubvista("/vista/ClienteTarifas.fxml");
                break;
            case "Mis Contratos":
                vistaActual = "Mis Contratos";
                cargarSubvista("/vista/ClienteContratos.fxml");
                break;
            case "Soporte":
                vistaActual = "Soporte";
                cargarSubvista("/vista/ClienteSoporte.fxml");
                break;
            case "Mis Valoraciones":
                vistaActual = "Mis Valoraciones";
                cargarSubvista("/vista/ClienteValoraciones.fxml");
                break;
            case "Cerrar Sesión":
                cerrarSesion(event);
                break;
        }
    }

    /**
     * Pide confirmación para cerrar la sesión. Si se acepta, limpia los datos
     * de sesión y vuelve a la pantalla de inicio de sesión; si no, restaura la
     * opción anterior del desplegable.
     *
     * @param event Evento de acción que originó el cierre de sesión.
     */
    private void cerrarSesion(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cerrar Sesión");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Deseas salir del panel de cliente?");

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                LOGGER.info("Sesión cerrada");
                LogApp.setUsuario(null);
                cliente = null;
                IniciarSesionController.idUsuarioActual = null;
                URL url = getClass().getResource("/vista/IniciarSesion.fxml");
                Parent loginView = FXMLLoader.load(url);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(loginView);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error al volver al login", e);
                ShowAlert.error("No se pudo volver a la pantalla de inicio de sesión.");
            }
        } else {
            cmbOpciones.getSelectionModel().select(vistaActual);
        }
    }

    /**
     * Carga una subvista FXML en el panel central.
     *
     * @param rutaFXML Ruta del recurso FXML a cargar.
     */
    private void cargarSubvista(String rutaFXML) {
        try {
            URL url = getClass().getResource(rutaFXML);
            if (url != null) {
                Parent vista = FXMLLoader.load(url);
                panelContenido.getChildren().clear();
                panelContenido.getChildren().add(vista);
            } else {
                LOGGER.severe("No existe la pantalla " + rutaFXML);
                ShowAlert.error("No se encontró la pantalla " + rutaFXML);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir " + rutaFXML, e);
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }
}