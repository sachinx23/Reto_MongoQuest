package controladorTest;

import controlador.ClienteController;
import controlador.IniciarSesionController;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import modelo.Cliente;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code ClienteController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code Cliente.fxml} con un cliente de prueba y comprobar
 * que el desplegable cambia la pantalla central, sin depender de los datos de MongoDB.
 * </p>
 * <p>
 * Las opciones del desplegable se seleccionan por código (no con clics sobre el popup)
 * para que las pruebas sean estables.
 * </p>
 *
 * @author sachin
 * @version 1.3
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class ClienteControllerTest extends ApplicationTest {

    /**
     * Inicializa el escenario cargando el panel con un cliente de prueba.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        IniciarSesionController.idUsuarioActual = null;
        ClienteController.setCliente(new Cliente(new ObjectId().toHexString(), "ES7921000813610123456789",
                "usuarioPrueba", "Prueba", "Test", "abcd", "prueba@gmail.com", "12345678Z", "666666666", "Calle Falsa 1"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Cliente.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia y oculta la ventana tras la finalización de cada método de prueba.
     *
     * @throws Exception Si ocurre un fallo al intentar cerrar el escenario.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
    }

    /**
     * Selecciona una opción del desplegable por código y espera a que la interfaz termine de actualizarse.
     *
     * @param opcion Texto de la opción a seleccionar.
     */
    private void seleccionar(final String opcion) {
        final ComboBox<String> combo = lookup("#cmbOpciones").query();
        interact(new Runnable() {
            @Override
            public void run() {
                combo.getSelectionModel().select(opcion);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Verifica que la etiqueta salude al cliente y que al abrir el panel se muestre Mi Perfil.
     */
    @Test
    public void comprobarNombre() {
        verifyThat("#lblBienvenida", LabeledMatchers.hasText("Cliente: Prueba"));
        verifyThat("#cmbOpciones", ComboBoxMatchers.hasSelectedItem("Mi Perfil"));
        verifyThat("#txtNombreUsuario", isVisible());
    }

    /**
     * Comprueba que el desplegable tenga las seis opciones del cliente.
     */
    @Test
    public void comprobarCombobox() {
        verifyThat("#cmbOpciones", isVisible());
        verifyThat("#cmbOpciones", ComboBoxMatchers.containsExactlyItems("Mi Perfil", "Mis Tarifas",
                "Mis Contratos", "Soporte", "Mis Valoraciones", "Cerrar Sesión"));
    }

    /**
     * Comprueba que la opción "Mi Perfil" abra su pantalla.
     */
    @Test
    public void comprobarMiPerfil() {
        seleccionar("Mi Perfil");
        verifyThat("#txtNombreUsuario", isVisible());
        verifyThat("#btnModificar", isVisible());
    }

    /**
     * Comprueba que la opción "Mis Tarifas" abra su pantalla.
     */
    @Test
    public void comprobarMisTarifas() {
        seleccionar("Mis Tarifas");
        verifyThat("#contenedorTarifas", isVisible());
        verifyThat("#btnCambiarTarifa", isVisible());
    }

    /**
     * Comprueba que la opción "Soporte" abra su pantalla.
     */
    @Test
    public void comprobarSoporte() {
        seleccionar("Soporte");
        verifyThat("#txtSoporteAsunto", isVisible());
        verifyThat("#btnEnviarTicket", isVisible());
    }

    /**
     * Comprueba que la opción "Mis Valoraciones" abra su pantalla.
     */
    @Test
    public void comprobarMisValoraciones() {
        seleccionar("Mis Valoraciones");
        verifyThat("#tablaValoraciones", isVisible());
        verifyThat("#btnValorar", isVisible());
    }

    /**
     * Comprueba que "Cerrar Sesión" pida confirmación y, al aceptar, vuelva a la pantalla de inicio de sesión.
     * Se usa {@code Platform.runLater} porque el diálogo se abre con {@code showAndWait()}.
     */
    @Test
    public void comprobarCerrarSesion() {
        final ComboBox<String> combo = lookup("#cmbOpciones").query();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                combo.getSelectionModel().select("Cerrar Sesión");
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        clickOn(dialog.lookupButton(ButtonType.OK));
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat("#txtUsuario", isVisible());
        verifyThat("#txtContrasena", isVisible());
    }

    /**
     * Comprueba que se pueda abrir Mis Contratos desde otra pantalla.
     */
    @Test
    public void comprobarMisContratos() {
        seleccionar("Mis Tarifas");
        seleccionar("Mis Contratos");
        verifyThat("#tablaContratos", isVisible());
        verifyThat("#btnFirmarContrato", isVisible());
    }
}