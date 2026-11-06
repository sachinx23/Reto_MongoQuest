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

public class ClienteController {

    private static final Logger LOGGER = Logger.getLogger(ClienteController.class.getName());
    // Cliente con sesión iniciada
    private static Cliente cliente;

    private static Label bienvenida;

    private final ClienteDAO clienteDAO = new ClienteDAOImplementacion();

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<String> cmbOpciones;
    @FXML private StackPane panelContenido;

    // Apartado actual
    private String actual;

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

    public static Cliente getCliente() {
        return cliente;
    }

    public static void setCliente(Cliente nuevo) {
        cliente = nuevo;
    }

    public static void actualizarBienvenida() {
        bienvenida.setText("Cliente: " + cliente.getNombre());
    }

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

    private void cargarSubvista(String apartado, String fxml) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/vista/" + fxml + ".fxml"));
            panelContenido.getChildren().setAll(vista);
            actual = apartado;
        } catch (IOException | RuntimeException e) {
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }

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
