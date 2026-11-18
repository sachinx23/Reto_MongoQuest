package controladorTest;

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

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.TableViewMatchers;

/**
 * Pruebas de la pantalla Mis Contratos.
 */
public class ClienteContratosControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        sesionPedro();
        Parent root = FXMLLoader.load(getClass().getResource("/vista/ClienteContratos.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Pedro tiene un contrato activo y otro pendiente.
     */
    @Test
    public void testTablaConContratos() {
        verifyThat("#tablaContratos", TableViewMatchers.hasNumRows(2));
    }

    /**
     * Firmar sin elegir contrato avisa.
     */
    @Test
    public void testFirmarSinSeleccion() {
        clickOn("#btnFirmarContrato");
        assertEquals("Selecciona un contrato de la tabla primero.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Pedir la baja sin elegir contrato avisa.
     */
    @Test
    public void testRescindirSinSeleccion() {
        clickOn("#btnRescindirContrato");
        assertEquals("Selecciona un contrato de la tabla primero.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Un contrato activo no se vuelve a firmar.
     */
    @Test
    public void testFirmarContratoActivo() {
        clickOn("activo");
        clickOn("#btnFirmarContrato");
        assertEquals("Este contrato ya está firmado y activo.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Un contrato pendiente espera a que lo valide un empleado.
     */
/**
     * Un contrato pendiente espera a que lo valide un empleado.
     */
/**
     * Un contrato pendiente espera a que lo valide un empleado.
     */
    @Test
    public void testFirmarContratoPendiente() {
        // Selecciona la celda de la tabla que contiene el texto "pendiente"
        clickOn(org.testfx.matcher.control.TableViewMatchers.hasTableCell("pendiente"));
        clickOn("#btnFirmarContrato");
        assertEquals("Podrás firmar este contrato cuando un empleado valide la tarifa.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Solo se pide la baja de un contrato activo.
     */
    @Test
    public void testRescindirContratoPendiente() {
        // Selecciona la celda de la tabla que contiene el texto "pendiente"
        clickOn(org.testfx.matcher.control.TableViewMatchers.hasTableCell("pendiente"));
        clickOn("#btnRescindirContrato");
        assertEquals("Solo se puede pedir la baja de un contrato activo.", textoAlerta());
        aceptarAlerta();
    }
    /**
     * Si se cancela la baja el contrato sigue activo.
     */
    @Test
    public void testRescindirCancelar() {
        clickOn("activo");
        clickOn("#btnRescindirContrato");
        assertEquals("¿Estás seguro de solicitar la baja definitiva y rescindir tu contrato?", textoAlerta());
        cancelarAlerta();
        verifyThat("activo", isVisible());
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
        }
    }
}