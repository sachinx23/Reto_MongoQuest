package controladorTest;

import controlador.ClienteController;
import controlador.IniciarSesionController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import modelo.Cliente;
import modelo.Contrato;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TableViewMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code ClienteContratosController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code ClienteContratos.fxml} y comprobar
 * los botones de firmar y rescindir. Los contratos se cargan en la tabla desde el propio test,
 * por lo que las pruebas no dependen de los datos guardados en MongoDB.
 * </p>
 *
 * @author sachin
 * @version 1.0
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class ClienteContratosControllerTest extends ApplicationTest {

    /**
     * Inicializa el escenario cargando la vista con un cliente de prueba y cuatro contratos de prueba
     * (activo, pendiente, pendiente de firma y rescindido).
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws IOException Si ocurre un error al cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        IniciarSesionController.idUsuarioActual = null;
        ClienteController.setCliente(new Cliente(new ObjectId().toHexString(), "ES7921000813610123456789",
                "usuarioPrueba", "Prueba", "Test", "abcd", "prueba@gmail.com", "12345678Z", "666666666", "Calle Falsa 1"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/ClienteContratos.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();

        List<Contrato> contratos = new ArrayList<>();
        contratos.add(new Contrato("c1", "666666666", "activo", "x", null, null, "2026-01-01", "2027-01-01"));
        contratos.add(new Contrato("c2", "666666666", "pendiente", "x", null, null, "2026-01-01", "2027-01-01"));
        contratos.add(new Contrato("c3", "666666666", "pendiente de firma", "x", null, null, "2026-01-01", "2027-01-01"));
        contratos.add(new Contrato("c4", "666666666", "rescindido", "x", null, null, "2026-01-01", "2027-01-01"));
        TableView<Contrato> tabla = lookup("#tablaContratos").queryTableView();
        tabla.setItems(FXCollections.observableArrayList(contratos));
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
     * Comprueba que los botones y la tabla sean visibles y tengan sus textos.
     */
    @Test
    public void comprobarBotonesYTabla() {
        verifyThat("#btnFirmarContrato", isVisible());
        verifyThat("#btnRescindirContrato", isVisible());
        verifyThat("#btnFirmarContrato", LabeledMatchers.hasText("_Firmar contrato"));
        verifyThat("#btnRescindirContrato", LabeledMatchers.hasText("_Rescindir contrato"));
        verifyThat("#tablaContratos", TableViewMatchers.hasNumRows(4));
    }

    /**
     * Comprueba que la tabla muestre el estado de cada contrato.
     */
    @Test
    public void comprobarEstadosEnTabla() {
        verifyThat("#tablaContratos", TableViewMatchers.hasTableCell("activo"));
        verifyThat("#tablaContratos", TableViewMatchers.hasTableCell("pendiente"));
        verifyThat("#tablaContratos", TableViewMatchers.hasTableCell("pendiente de firma"));
        verifyThat("#tablaContratos", TableViewMatchers.hasTableCell("rescindido"));
    }

    /**
     * Comprueba que al firmar sin seleccionar contrato aparezca un aviso.
     */
    @Test
    public void comprobarFirmarSinSeleccion() {
        clickOn("#btnFirmarContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Selecciona un contrato de la tabla primero.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al rescindir sin seleccionar contrato aparezca un aviso.
     */
    @Test
    public void comprobarRescindirSinSeleccion() {
        clickOn("#btnRescindirContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Selecciona un contrato de la tabla primero.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que un contrato activo no se vuelva a firmar.
     */
    @Test
    public void comprobarFirmarContratoActivo() {
        clickOn("activo");
        clickOn("#btnFirmarContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Este contrato ya está firmado y activo.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que un contrato pendiente avise de que falta la validación del empleado.
     */
    @Test
    public void comprobarFirmarContratoPendiente() {
        clickOn("pendiente");
        clickOn("#btnFirmarContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Podrás firmar este contrato cuando un empleado valide la tarifa.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que un contrato pendiente de firma pida confirmación y que al cancelar no se firme.
     */
    @Test
    public void comprobarFirmarPendienteDeFirmaCancelar() {
        clickOn("pendiente de firma");
        clickOn("#btnFirmarContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("¿Firmar el contrato de la tarifa ? Empieza hoy.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.CANCEL));
        verifyThat("#tablaContratos", TableViewMatchers.hasTableCell("pendiente de firma"));
    }

    /**
     * Comprueba que un contrato rescindido no se pueda firmar.
     */
    @Test
    public void comprobarFirmarContratoRescindido() {
        clickOn("rescindido");
        clickOn("#btnFirmarContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Este contrato no se puede firmar porque está rescindido.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que solo se pueda pedir la baja de un contrato activo.
     */
    @Test
    public void comprobarRescindirContratoPendiente() {
        clickOn("pendiente");
        clickOn("#btnRescindirContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Solo se puede pedir la baja de un contrato activo.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al cancelar la baja el contrato siga activo.
     */
    @Test
    public void comprobarRescindirCancelar() {
        clickOn("activo");
        clickOn("#btnRescindirContrato");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("¿Estás seguro de solicitar la baja definitiva y rescindir tu contrato?", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.CANCEL));
        verifyThat("#tablaContratos", TableViewMatchers.hasTableCell("activo"));
    }
}
