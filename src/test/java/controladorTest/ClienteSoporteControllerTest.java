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
import modelo.TicketSoporte;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code ClienteSoporteController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code ClienteSoporte.fxml} y comprobar el formulario
 * de tickets. El ticket de la tabla se carga desde el propio test, sin depender de MongoDB.
 * </p>
 *
 * @author sachin
 * @version 1.0
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class ClienteSoporteControllerTest extends ApplicationTest {

    /**
     * Inicializa el escenario cargando la vista con un cliente y un ticket de prueba.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        IniciarSesionController.idUsuarioActual = null;
        ClienteController.setCliente(new Cliente(new ObjectId().toHexString(), "ES7921000813610123456789",
                "usuarioPrueba", "Prueba", "Test", "abcd", "prueba@gmail.com", "12345678Z", "666666666", "Calle Falsa 1"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/ClienteSoporte.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();

        List<TicketSoporte> tickets = new ArrayList<>();
        tickets.add(new TicketSoporte("k1", "Sin conexion", "No tengo internet", "incidencia", "2026-11-03",
                "pendiente", "x", null, null));
        TableView<TicketSoporte> tabla = lookup("#tablaSoporte").queryTableView();
        tabla.setItems(FXCollections.observableArrayList(tickets));
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
     * Comprueba que el formulario esté vacío y se vea el ticket de prueba.
     */
    @Test
    public void comprobarEstadoInicial() {
        verifyThat("#txtSoporteAsunto", TextInputControlMatchers.hasText(""));
        verifyThat("#txtSoporteMensaje", TextInputControlMatchers.hasText(""));
        verifyThat("#btnEnviarTicket", LabeledMatchers.hasText("E_nviar ticket"));
        verifyThat("#tablaSoporte", TableViewMatchers.hasNumRows(1));
        verifyThat("#tablaSoporte", TableViewMatchers.hasTableCell("Sin conexion"));
        verifyThat("#tablaSoporte", TableViewMatchers.hasTableCell("2026-11-03"));
    }

    /**
     * Comprueba que al enviar sin rellenar nada aparezca un aviso.
     */
    @Test
    public void comprobarEnviarVacio() {
        clickOn("#btnEnviarTicket");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Rellena el asunto y la descripción del ticket.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al enviar solo con asunto aparezca un aviso.
     */
    @Test
    public void comprobarEnviarSinDescripcion() {
        clickOn("#txtSoporteAsunto").write("Prueba");
        clickOn("#btnEnviarTicket");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Rellena el asunto y la descripción del ticket.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que al enviar solo con descripción aparezca un aviso.
     */
    @Test
    public void comprobarEnviarSinAsunto() {
        clickOn("#txtSoporteMensaje").write("Descripcion de prueba");
        clickOn("#btnEnviarTicket");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Rellena el asunto y la descripción del ticket.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que con todo relleno se pida confirmación y que al cancelar el texto se quede.
     */
    @Test
    public void comprobarEnviarCancelar() {
        clickOn("#txtSoporteAsunto").write("Prueba");
        clickOn("#txtSoporteMensaje").write("Descripcion de prueba");
        clickOn("#btnEnviarTicket");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("¿Seguro que deseas enviar este ticket?", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.CANCEL));
        verifyThat("#txtSoporteAsunto", TextInputControlMatchers.hasText("Prueba"));
        verifyThat("#tablaSoporte", TableViewMatchers.hasNumRows(1));
    }
}
