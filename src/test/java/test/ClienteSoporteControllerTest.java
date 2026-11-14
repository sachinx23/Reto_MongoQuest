package test;

import controlador.ClienteController;
import controlador.IniciarSesionController;

import org.testfx.util.WaitForAsyncUtils;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import DAO.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import java.util.concurrent.TimeoutException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.AfterEach;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import DAO.MongoDBConexion;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.matcher.control.TextInputControlMatchers;

/**
 * Pruebas de la pantalla Soporte.
 */
public class ClienteSoporteControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        sesionPedro();
        Parent root = FXMLLoader.load(getClass().getResource("/vista/ClienteSoporte.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Número de tickets de la tabla.
     */
    private int filas() {
        TableView<?> tabla = lookup("#tablaSoporte").queryTableView();
        return tabla.getItems().size();
    }

    /**
     * Pedro tiene al menos un ticket.
     */
    @Test
    public void testTablaConTickets() {
        assertTrue(filas() > 0);
    }

    /**
     * Sin asunto ni mensaje avisa.
     */
    @Test
    public void testEnviarVacio() {
        clickOn("#btnEnviarTicket");
        assertEquals("Rellena el asunto y la descripción del ticket.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Solo con asunto también avisa.
     */
    @Test
    public void testFaltaMensaje() {
        clickOn("#txtSoporteAsunto").write("Prueba");
        clickOn("#btnEnviarTicket");
        assertEquals("Rellena el asunto y la descripción del ticket.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Si se cancela no se crea el ticket.
     */
    @Test
    public void testCancelarEnvio() {
        int antes = filas();
        clickOn("#txtSoporteAsunto").write("Prueba cancelar");
        clickOn("#txtSoporteMensaje").write("No se envía");
        clickOn("#btnEnviarTicket");
        cancelarAlerta();
        assertEquals(antes, filas());
    }

    /**
     * Envía un ticket y sale en la tabla.
     */
    @Test
    public void testEnviarTicket() {
        int antes = filas();
        clickOn("#txtSoporteAsunto").write("Prueba test");
        clickOn("#txtSoporteMensaje").write("Ticket de prueba");
        clickOn("#btnEnviarTicket");
        aceptarAlerta();
        assertEquals("Tu consulta ha sido enviada al departamento de soporte con éxito.", textoAlerta());
        aceptarAlerta();
        assertEquals(antes + 1, filas());
        verifyThat("#txtSoporteAsunto", TextInputControlMatchers.hasText(""));
        // Deja la base de datos como estaba
        MongoDBConexion.getColeccion("TicketsSoporte").deleteOne(new Document("asunto", "Prueba test"));
    }

    /**
     * Inicia la sesión de Pedro (crear_bd.js) sin pasar por el login.
     *
     * @throws OurException si falla MongoDB
     */
    private void sesionPedro() throws OurException {
        ClienteDAOImplementacion dao = new ClienteDAOImplementacion();
        Document d = dao.comprobarUsuarios("PedroDiaz0", "abcd");
        IniciarSesionController.idUsuarioActual = d.getObjectId("_id").toHexString();
        ClienteController.setCliente(dao.obtenerCliente(IniciarSesionController.idUsuarioActual));
    }

    /**
     * Mensaje de la alerta abierta.
     */
    private String textoAlerta() {
        esperar(".dialog-pane");
        return lookup(".dialog-pane").queryAs(DialogPane.class).getContentText();
    }

    /**
     * Pulsa Aceptar en la alerta abierta.
     */
    private void aceptarAlerta() {
        esperar(".dialog-pane");
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        clickOn(dialogo.lookupButton(ButtonType.OK));
    }

    /**
     * Pulsa Cancelar en la alerta abierta.
     */
    private void cancelarAlerta() {
        esperar(".dialog-pane");
        DialogPane dialogo = lookup(".dialog-pane").queryAs(DialogPane.class);
        clickOn(dialogo.lookupButton(ButtonType.CANCEL));
    }

    /**
     * Cierra la ventana y suelta teclas y ratón después de cada prueba.
     *
     * @throws TimeoutException si la ventana no se cierra
     */
    @AfterEach
    public void tearDown() throws TimeoutException {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * Espera hasta 5 segundos a que aparezca un nodo.
     */
    private void esperar(final String selector) {
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return lookup(selector).tryQuery().isPresent();
                }
            });
        } catch (TimeoutException e) {
            // Si no aparece, la prueba falla en el lookup siguiente
        }
    }
}
