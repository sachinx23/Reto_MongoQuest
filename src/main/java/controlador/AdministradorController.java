package controlador;

import log.LogApp;
import exception.ShowAlert;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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

/**
 * Controlador principal de la vista del Administrador en JavaFX.
 * Gestiona la navegación dinámica entre las distintas subvistas de administración
 * (plantilla, sucursales, tarifas, registros del sistema) y las operaciones heredadas
 * del rol de empleado (contratos, tickets de soporte y valoraciones), además del cierre de sesión.
 *
 * @author eneko
 */
public class AdministradorController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar las acciones y errores de navegación del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(AdministradorController.class.getName());

    /** Selector desplegable con las distintas secciones y acciones disponibles para el administrador. */
    @FXML
    private ComboBox<String> cbOpcionesAdmin;

    /** Contenedor principal donde se cargan dinámicamente las subvistas FXML seleccionadas. */
    @FXML
    private StackPane contenidoDinamico;

    /** Etiqueta de cabecera que muestra el nombre del administrador con sesión activa. */
    @FXML 
    private Label lblNombreAdmin;

    /**
     * Inicializa los componentes de la interfaz al cargar la vista del administrador.
     * Rellena el desplegable con las opciones disponibles, selecciona por defecto
     * {@code "Gestionar Plantilla"} y carga su subvista correspondiente.
     *
     * @param url Ubicación utilizada para resolver rutas relativas del objeto raíz, o {@code null} si no se conoce.
     * @param rb  Recursos utilizados para localizar el objeto raíz, o {@code null} si no se localizó.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbOpcionesAdmin.setItems(FXCollections.observableArrayList(
                "Gestionar Plantilla",
                "Gestionar Sucursal",
                "Gestionar Tarifas",
                "Gestionar Contratos",
                "Tickets de Soporte",
                "Ver Valoraciones",
                "Ver Log",
                "Cerrar Sesión"
        ));
        
        cbOpcionesAdmin.getSelectionModel().select("Gestionar Plantilla");
        cargarSubvista("/vista/GestionarPlantilla.fxml");
    }

    /**
     * Manejador de eventos del {@link ComboBox} que cambia la subvista mostrada en el panel
     * central o ejecuta el cierre de sesión según la opción seleccionada por el usuario.
     *
     * @param event Evento de acción disparado al seleccionar una opción en {@code cbOpcionesAdmin}.
     */
    @FXML
    public void cambiarSubVista(ActionEvent event) {
        String seleccion = cbOpcionesAdmin.getValue();
        if (seleccion == null) {
            return;
        }

        switch (seleccion) {
            case "Gestionar Plantilla":
                cargarSubvista("/vista/GestionarPlantilla.fxml");
                break;
            case "Gestionar Sucursal":
                cargarSubvista("/vista/Sucursal.fxml");
                break;
            case "Gestionar Tarifas":
                cargarSubvista("/vista/GestionarTarifas.fxml");
                break;
            case "Ver Log":
                cargarSubvista("/vista/GestionLogs.fxml");
                break;
            case "Gestionar Contratos":
                cargarSubvista("/vista/GestionarContrato.fxml");
                break;
            case "Tickets de Soporte":
                cargarSubvista("/vista/GestionarTickets.fxml");
                break;
            case "Ver Valoraciones":
                cargarSubvista("/vista/GestionarValoraciones.fxml");
                break;

            case "Cerrar Sesión":
                cerrarSesion(event);
                break;
        }
    }

    /**
     * Establece y muestra el nombre del administrador autenticado en la etiqueta superior de la vista.
     *
     * @param nombre Nombre de usuario del administrador que ha iniciado sesión.
     */
    @FXML
    public void setNombreUsuario(String nombre) {
        if (lblNombreAdmin != null && nombre != null) {
            lblNombreAdmin.setText("Admin: " + nombre);
        }
    }

    /**
     * Cierra la sesión actual limpiando el usuario registrado en {@link LogApp} y
     * redirige la ventana principal a la pantalla de inicio de sesión ({@code IniciarSesion.fxml}).
     *
     * @param event Evento de acción utilizado para obtener la ventana ({@link Stage}) actual.
     */
    private void cerrarSesion(ActionEvent event) {
        try {
            LOGGER.info("Sesión cerrada");
            LogApp.setUsuario(null);
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
    }

    /**
     * Carga un archivo FXML en el contenedor dinámico {@code contenidoDinamico},
     * reemplazando cualquier vista que estuviera cargada previamente.
     *
     * @param rutaFXML Ruta relativa dentro de los recursos del proyecto hacia el archivo {@code .fxml}.
     */
    private void cargarSubvista(String rutaFXML) {
        try {
            URL url = getClass().getResource(rutaFXML);
            if (url != null) {
                Parent vista = FXMLLoader.load(url);
                contenidoDinamico.getChildren().clear();
                contenidoDinamico.getChildren().add(vista);
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