package controladorTest;

import controlador.ClienteController;
import controlador.ClienteTarifasController;
import controlador.IniciarSesionController;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import modelo.Cliente;
import modelo.Tarifa;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code ClienteTarifasController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code ClienteTarifas.fxml}. Las tarjetas se crean
 * con dos tarifas de prueba, por lo que las pruebas no dependen de los datos guardados en MongoDB.
 * </p>
 *
 * @author sachin
 * @version 1.0
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class ClienteTarifasControllerTest extends ApplicationTest {

    /**
     * Inicializa el escenario cargando la vista con un cliente de prueba y dos tarifas de prueba.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws Exception Si ocurre un error al cargar el FXML o al crear las tarjetas.
     */
    @Override
    public void start(Stage stage) throws Exception {
        IniciarSesionController.idUsuarioActual = null;
        ClienteController.setCliente(new Cliente(new ObjectId().toHexString(), "ES7921000813610123456789",
                "usuarioPrueba", "Prueba", "Test", "abcd", "prueba@gmail.com", "12345678Z", "666666666", "Calle Falsa 1"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/ClienteTarifas.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();

        List<Tarifa> tarifas = new ArrayList<>();
        tarifas.add(new Tarifa("t1", "Tarifa Prueba Fibra", 40, "600 Mb", "Fibra", true));
        tarifas.add(new Tarifa("t2", "Tarifa Prueba Movil", 15, "30 GB", "Móvil", true));
        ClienteTarifasController controlador = loader.getController();
        Method mostrar = ClienteTarifasController.class.getDeclaredMethod("mostrarTarifasEnTarjetas", List.class);
        mostrar.setAccessible(true);
        mostrar.invoke(controlador, tarifas);
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
     * Comprueba los textos de los botones y que haya una tarjeta por tarifa.
     */
    @Test
    public void comprobarBotonesYTarjetas() {
        verifyThat("#btnCambiarTarifa", LabeledMatchers.hasText("_Contratar tarifa"));
        verifyThat("#btnEliminarTarifa", LabeledMatchers.hasText("_Eliminar petición"));
        FlowPane contenedor = lookup("#contenedorTarifas").queryAs(FlowPane.class);
        assertEquals(2, contenedor.getChildren().size());
        verifyThat("Tarifa Prueba Fibra", isVisible());
    }

    /**
     * Comprueba que la tarjeta muestre el tipo, el precio y los detalles.
     */
    @Test
    public void comprobarDatosTarjeta() {
        verifyThat("Fibra", isVisible());
        verifyThat("40.0 €", isVisible());
        verifyThat("600 Mb", isVisible());
    }

    /**
     * Comprueba que al pulsar una tarjeta quede marcada como seleccionada.
     */
    @Test
    public void comprobarSeleccionarTarjeta() {
        WaitForAsyncUtils.waitForFxEvents();
        FlowPane contenedor = lookup("#contenedorTarifas").queryAs(FlowPane.class);
        clickOn(contenedor.getChildren().get(0));
        assertTrue(contenedor.getChildren().get(0).getStyleClass().contains("tarjeta-tarifa-seleccionada"));
    }

    /**
     * Comprueba que al contratar sin elegir tarifa aparezca un aviso.
     */
    @Test
    public void comprobarContratarSinSeleccion() {
        clickOn("#btnCambiarTarifa");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Por favor, selecciona una tarifa haciendo clic en una tarjeta.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al eliminar la petición sin elegir tarifa aparezca un aviso.
     */
    @Test
    public void comprobarEliminarSinSeleccion() {
        clickOn("#btnEliminarTarifa");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Por favor, selecciona una tarifa haciendo clic en una tarjeta.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al contratar se pida confirmación y que al cancelar no se pida nada.
     */
    @Test
    public void comprobarContratarCancelar() {
        WaitForAsyncUtils.waitForFxEvents();
        FlowPane contenedor = lookup("#contenedorTarifas").queryAs(FlowPane.class);
        clickOn(contenedor.getChildren().get(0));
        clickOn("#btnCambiarTarifa");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("¿Estás seguro de que deseas contratar la tarifa Tarifa Prueba Fibra?", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.CANCEL));
    }

    /**
     * Comprueba que al eliminar la petición se pida confirmación y que al cancelar no se borre nada.
     */
    @Test
    public void comprobarEliminarCancelar() {
        WaitForAsyncUtils.waitForFxEvents();
        FlowPane contenedor = lookup("#contenedorTarifas").queryAs(FlowPane.class);
        clickOn(contenedor.getChildren().get(0));
        clickOn("#btnEliminarTarifa");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("¿Seguro que deseas eliminar tu petición de la tarifa Tarifa Prueba Fibra?", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.CANCEL));
    }
}
