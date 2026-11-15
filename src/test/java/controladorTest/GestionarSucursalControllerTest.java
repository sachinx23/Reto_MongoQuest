package controladorTest;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * Clase de pruebas de interfaz de usuario (GUI) para la vista {@code Sucursal.fxml}.
 * <p>
 * Emplea el marco de pruebas TestFX para verificar la correcta visualización de los campos
 * del formulario, los botones de acción, la lista de sucursales, el mapa web y la escritura
 * en los campos de texto.
 * </p>
 *
 * @author eneko
 * @version 1.0
 */
public class GestionarSucursalControllerTest extends ApplicationTest {

    /**
     * Inicializa el entorno de la interfaz gráfica cargando la vista FXML antes de cada prueba.
     *
     * @param stage Escenario principal {@link Stage} gestionado por TestFX.
     * @throws IOException Si ocurre un error al intentar cargar el recurso FXML desde la ruta indicada.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Sucursal.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia la ventana y resetea el estado físico de periféricos (teclado y ratón) al finalizar cada test.
     *
     * @throws Exception Si se produce una falla al ocultar el escenario a través de FxToolkit.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * Comprueba que el campo de texto del nombre de la sucursal sea visible.
     */
    @Test
    public void testCampoNombreVisible() {
        verifyThat("#txtNombre", isVisible());
    }

    /**
     * Comprueba que el campo de texto del horario de la sucursal sea visible.
     */
    @Test
    public void testCampoHorarioVisible() {
        verifyThat("#txtHorario", isVisible());
    }

    /**
     * Comprueba que el campo de texto del teléfono de la sucursal sea visible.
     */
    @Test
    public void testCampoTelefonoVisible() {
        verifyThat("#txtTelefono", isVisible());
    }

    /**
     * Comprueba que el campo de texto de la ubicación de la sucursal sea visible.
     */
    @Test
    public void testCampoUbicacionVisible() {
        verifyThat("#txtUbicacion", isVisible());
    }

    /**
     * Comprueba que el campo de texto del código postal de la sucursal sea visible.
     */
    @Test
    public void testCampoCodigoPostalVisible() {
        verifyThat("#txtCodigoPostal", isVisible());
    }

    /**
     * Comprueba que la lista de sucursales sea visible.
     */
    @Test
    public void testListaSucursalesVisible() {
        verifyThat("#listSucursales", isVisible());
    }

    /**
     * Comprueba que el mapa web de ubicación sea visible.
     */
    @Test
    public void testWebViewMapaVisible() {
        verifyThat("#webViewMapa", isVisible());
    }

    /**
     * Comprueba el texto del botón para añadir una nueva sucursal.
     */
    @Test
    public void testBotonAnadirTieneTexto() {
        verifyThat("#btnAnadir", hasText("Añadir"));
    }

    /**
     * Comprueba el texto del botón para modificar una sucursal.
     */
    @Test
    public void testBotonModificarTieneTexto() {
        verifyThat("#btnGestionar", hasText("Modificar"));
    }

    /**
     * Comprueba el texto del botón para eliminar una sucursal.
     */
    @Test
    public void testBotonEliminarTieneTexto() {
        verifyThat("#btnEliminar", hasText("Eliminar"));
    }

    /**
     * Comprueba la escritura de datos en todas las cajas de texto del formulario de sucursales.
     */
    @Test
    public void testEscrituraDeCamposFormulario() {
        clickOn("#txtNombre").write("Tel-ONDA");
        clickOn("#txtHorario").write("08:00 - 20:00");
        clickOn("#txtTelefono").write("944 123 456");
        clickOn("#txtUbicacion").write("Lonbo Kalea, Bilbao");
        clickOn("#txtCodigoPostal").write("48480");

        verifyThat("#txtNombre", TextInputControlMatchers.hasText("Tel-ONDA"));
        verifyThat("#txtHorario", TextInputControlMatchers.hasText("08:00 - 20:00"));
        verifyThat("#txtTelefono", TextInputControlMatchers.hasText("944 123 456"));
        verifyThat("#txtUbicacion", TextInputControlMatchers.hasText("Lonbo Kalea, Bilbao"));
        verifyThat("#txtCodigoPostal", TextInputControlMatchers.hasText("48480"));
    }
    /**
     * Comprueba que al pulsar Añadir con los campos vacíos salta la alerta de aviso.
     */
    @Test
    public void testAlertaCamposVaciosAlAnadir() {
        clickOn("#btnAnadir");

        verifyThat(".dialog-pane", isVisible());
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Por favor, completa todos los campos.", dialogo.getContentText());
        clickOn(dialogo.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al pulsar Modificar sin seleccionar ninguna sucursal salta la alerta.
     */
    @Test
    public void testAlertaModificarSinSeleccion() {
        clickOn("#btnGestionar");

        verifyThat(".dialog-pane", isVisible());
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Selecciona una sucursal de la lista.", dialogo.getContentText());
        clickOn(dialogo.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al pulsar Eliminar sin seleccionar ninguna sucursal salta la alerta.
     */
    @Test
    public void testAlertaEliminarSinSeleccion() {
        clickOn("#btnEliminar");

        verifyThat(".dialog-pane", isVisible());
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Selecciona una sucursal para borrar.", dialogo.getContentText());
        clickOn(dialogo.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al rellenar los datos y pulsar Añadir se guarda la sucursal.
     */
    @Test
    public void testAnadirSucursal() {
        clickOn("#txtNombre").write("Sucursal Test");
        clickOn("#txtHorario").write("08:00 - 20:00");
        clickOn("#txtTelefono").write("944123456");
        clickOn("#txtUbicacion").write("Lonbo Kalea, Bilbao");
        clickOn("#txtCodigoPostal").write("48480");
        clickOn("#btnAnadir");

        verifyThat(".dialog-pane", isVisible());
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Sucursal guardada correctamente.", dialogo.getContentText());
        clickOn(dialogo.lookupButton(ButtonType.OK));
    }
}