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
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Pruebas de la pantalla Mis Tarifas.
 */
public class ClienteTarifasControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        sesionPedro();
        Parent root = FXMLLoader.load(getClass().getResource("/vista/ClienteTarifas.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * La tabla muestra las tarifas activas.
     */
    @Test
    public void testTablaConTarifas() {
        verifyThat("#tablaTarifas", isVisible());
        TableView<?> tabla = lookup("#tablaTarifas").queryTableView();
        assertTrue(tabla.getItems().size() > 0);
    }

    /**
     * Contratar sin elegir tarifa avisa.
     */
    @Test
    public void testContratarSinSeleccion() {
        clickOn("#btnCambiarTarifa");
        assertEquals("Por favor, selecciona una tarifa de la tabla primero.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Eliminar sin elegir tarifa avisa.
     */
    @Test
    public void testEliminarSinSeleccion() {
        clickOn("#btnEliminarTarifa");
        assertEquals("Por favor, selecciona una tarifa de la tabla primero.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * No deja pedir una tarifa que ya tiene.
     */
    @Test
    public void testTarifaYaContratada() {
        clickOn("Onda Fibra 600");
        clickOn("#btnCambiarTarifa");
        assertEquals("Ya tienes la tarifa Onda Fibra 600 pedida o contratada.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Si se cancela no se pide la tarifa.
     */
    @Test
    public void testCancelarContratar() {
        clickOn("Onda Fibra 1G");
        clickOn("#btnCambiarTarifa");
        assertEquals("¿Estás seguro de que deseas contratar la tarifa Onda Fibra 1G?", textoAlerta());
        cancelarAlerta();
        clickOn("#btnEliminarTarifa");
        aceptarAlerta();
        assertEquals("No tienes ninguna petición pendiente de esa tarifa.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Pide una tarifa nueva y después borra la petición.
     */
    @Test
    public void testContratarYEliminarPeticion() {
        clickOn("Onda Fibra 1G");
        clickOn("#btnCambiarTarifa");
        aceptarAlerta();
        assertTrue(textoAlerta().startsWith("Tu petición de la tarifa Onda Fibra 1G"));
        aceptarAlerta();
        clickOn("Onda Fibra 1G");
        clickOn("#btnEliminarTarifa");
        aceptarAlerta();
        assertEquals("Tu petición de la tarifa Onda Fibra 1G ha sido eliminada.", textoAlerta());
        aceptarAlerta();
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
