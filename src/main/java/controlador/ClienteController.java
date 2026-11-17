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

/**
 * Controlador principal de la vista del Cliente en JavaFX.
 * Gestiona la autenticación del cliente, la actualización de bienvenida y
 * la navegación dinámica entre las distintas subvistas (perfil, tarifas, contratos,
 * soporte y valoraciones), además del cierre de sesión.
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        
        cmbOpciones.getSelectionModel().select("Mis Contratos");
        cargarSubvista("/vista/ClienteContratos.fxml");
    }

    public static Cliente getCliente() {
        return cliente;
    }

    public static void setCliente(Cliente nuevo) {
        cliente = nuevo;
    }

    public static void actualizarBienvenida() {
        if (bienvenida != null && cliente != null) {
            bienvenida.setText("Cliente: " + cliente.getNombre());
        }
    }

    @FXML
    public void cambiarSubVista(ActionEvent event) {
        String seleccion = cmbOpciones.getValue();
        if (seleccion == null) {
            return;
        }

        switch (seleccion) {
            case "Mi Perfil":
                cargarSubvista("/vista/ClientePerfil.fxml");
                break;
            case "Mis Tarifas":
                cargarSubvista("/vista/ClienteTarifas.fxml");
                break;
            case "Mis Contratos":
                cargarSubvista("/vista/ClienteContratos.fxml");
                break;
            case "Soporte":
                cargarSubvista("/vista/ClienteSoporte.fxml");
                break;
            case "Mis Valoraciones":
                cargarSubvista("/vista/ClienteValoraciones.fxml");
                break;
            case "Cerrar Sesión":
                cerrarSesion(event);
                break;
        }
    }

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