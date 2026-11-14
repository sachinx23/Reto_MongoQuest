package controlador;

import log.LogApp;
import java.util.logging.Logger;
import DAO.ClienteDAO;
import DAO.implementacion.ClienteDAOImplementacion;
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
import modelo.Cliente;

/**
 * Controlador principal del panel del Cliente en JavaFX.
 * Gestiona el estado de la sesión del cliente autenticado, el saludo de cabecera,
 * la navegación dinámica entre sus subvistas (perfil, tarifas, contratos, soporte
 * y valoraciones) y el cierre de sesión.
 *
 * @author Eneko.Plaza
 */
public class ClienteController {

    /**
     * Registrador de eventos (Logger) para trazar las acciones y advertencias del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(ClienteController.class.getName());

    /** Instancia estática del cliente con la sesión iniciada actualmente. */
    private static Cliente cliente;

    /** Referencia estática a la etiqueta de bienvenida para actualizarla desde subcontroladores. */
    private static Label bienvenida;

    /** Objeto de acceso a datos para recuperar la información del cliente. */
    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    /** Etiqueta de cabecera que muestra el saludo con el nombre del cliente. */
    @FXML private Label lblBienvenida;

    /** Selector desplegable con las secciones disponibles en el área de cliente. */
    @FXML private ComboBox<String> cmbOpciones;

    /** Contenedor central donde se cargan dinámicamente las distintas vistas FXML del cliente. */
    @FXML private StackPane panelContenido;

    /** Nombre del apartado que se encuentra cargado actualmente en pantalla. */
    private String actual;

    /**
     * Inicializa el controlador tras cargar la vista FXML del panel de cliente.
     * Carga los datos del cliente autenticado mediante {@link IniciarSesionController#idUsuarioActual},
     * actualiza el mensaje de bienvenida, rellena el selector de navegación y muestra por defecto
     * la subvista de {@code "Mi Perfil"}.
     */
    @FXML
    public void initialize() {
        bienvenida = lblBienvenida;
        try {
            cliente = clienteDAO.obtenerCliente(IniciarSesionController.idUsuarioActual);
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
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
        cmbOpciones.setValue("Mi Perfil");
        if (actual == null) {
            cargarSubvista("Mi Perfil", "ClientePerfil");
        }
    }

    /**
     * Obtiene la instancia del cliente que tiene la sesión iniciada actualmente.
     *
     * @return Objeto {@link Cliente} autenticado, o {@code null} si no hay sesión activa.
     */
    public static Cliente getCliente() {
        return cliente;
    }

    /**
     * Actualiza la referencia estática del cliente autenticado (por ejemplo, tras editar su perfil).
     *
     * @param nuevo Nueva instancia de {@link Cliente} con los datos actualizados.
     */
    public static void setCliente(Cliente nuevo) {
        cliente = nuevo;
    }

    /**
     * Refresca el texto de la etiqueta de bienvenida superior utilizando el nombre actual del cliente.
     */
    public static void actualizarBienvenida() {
        bienvenida.setText("Cliente: " + cliente.getNombre());
    }

    /**
     * Manejador de eventos del desplegable {@code cmbOpciones} que carga la subvista
     * correspondiente a la sección elegida o inicia el proceso de cierre de sesión.
     *
     * @param event Evento de acción disparado al seleccionar una opción en el {@link ComboBox}.
     */
    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        String seleccion = cmbOpciones.getValue();
        if (seleccion == null || seleccion.equals(actual)) {
            return;
        }
        switch (seleccion) {
            case "Mi Perfil":
                cargarSubvista(seleccion, "ClientePerfil");
                break;
            case "Mis Tarifas":
                cargarSubvista(seleccion, "ClienteTarifas");
                break;
            case "Mis Contratos":
                cargarSubvista(seleccion, "ClienteContratos");
                break;
            case "Soporte":
                cargarSubvista(seleccion, "ClienteSoporte");
                break;
            case "Mis Valoraciones":
                cargarSubvista(seleccion, "ClienteValoraciones");
                break;
            case "Cerrar Sesión":
                cerrarSesion();
                break;
        }
    }

    /**
     * Carga un archivo FXML ubicado en {@code /vista/} dentro del contenedor {@code panelContenido}
     * y registra el nombre del apartado activo.
     *
     * @param apartado Nombre de la sección que se va a mostrar.
     * @param fxml     Nombre base del archivo FXML (sin ruta ni extensión {@code .fxml}).
     */
    private void cargarSubvista(String apartado, String fxml) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/vista/" + fxml + ".fxml"));
            panelContenido.getChildren().setAll(vista);
            actual = apartado;
        } catch (IOException | RuntimeException e) {
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }

    /**
     * Solicita confirmación al cliente para abandonar el panel y, si acepta, limpia los datos
     * de sesión en memoria y en {@link LogApp}, redirigiendo a la pantalla de inicio de sesión.
     * Si cancela, restaura la selección anterior en el {@link ComboBox}.
     */
    private void cerrarSesion() {
        if (!ShowAlert.confirmar("Salir", "¿Deseas salir del panel de cliente?")) {
            cmbOpciones.setValue(actual);
            return;
        }
        try {
            cliente = null;
            LOGGER.info("Sesión cerrada");
            LogApp.setUsuario(null);
            Parent login = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
            Stage stage = (Stage) panelContenido.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.show();
        } catch (IOException | RuntimeException e) {
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }
}