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

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.LabeledMatchers;

/**
 * Pruebas del panel del cliente y su menú.
 */
public class ClienteControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        sesionPedro();
        Parent root = FXMLLoader.load(getClass().getResource("/vista/Cliente.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Al entrar sale el nombre y se abre Mi Perfil.
     */
    @Test
    public void testEstadoInicial() {
        verifyThat("#lblBienvenida", LabeledMatchers.hasText("Cliente: Pedro"));
        verifyThat("#cmbOpciones", ComboBoxMatchers.hasSelectedItem("Mi Perfil"));
        verifyThat("#txtNombreUsuario", isVisible());
    }

    /**
     * El menú tiene las 6 opciones.
     */
    @Test
    public void testOpcionesMenu() {
        verifyThat("#cmbOpciones", ComboBoxMatchers.hasItems(6));
    }

    /**
     * Mis Tarifas abre la tabla de tarifas.
     */
    @Test
    public void testAbrirMisTarifas() {
        elegir("#cmbOpciones", "Mis Tarifas");
        verifyThat("#tablaTarifas", isVisible());
    }

    /**
     * Mis Contratos abre la tabla de contratos.
     */
    @Test
    public void testAbrirMisContratos() {
        elegir("#cmbOpciones", "Mis Contratos");
        verifyThat("#tablaContratos", isVisible());
    }

    /**
     * Soporte abre la tabla de tickets.
     */
    @Test
    public void testAbrirSoporte() {
        elegir("#cmbOpciones", "Soporte");
        verifyThat("#tablaSoporte", isVisible());
    }

    /**
     * Mis Valoraciones abre la tabla de valoraciones.
     */
    @Test
    public void testAbrirMisValoraciones() {
        elegir("#cmbOpciones", "Mis Valoraciones");
        verifyThat("#tablaValoraciones", isVisible());
    }

    /**
     * Si se cancela la salida, sigue en el panel.
     */
    @Test
    public void testCerrarSesionCancelar() {
        elegir("#cmbOpciones", "Cerrar Sesión");
        assertEquals("¿Deseas salir del panel de cliente?", textoAlerta());
        cancelarAlerta();
        verifyThat("#cmbOpciones", ComboBoxMatchers.hasSelectedItem("Mi Perfil"));
    }

    /**
     * Si se acepta la salida, vuelve al login.
     */
    @Test
    public void testCerrarSesionAceptar() {
        elegir("#cmbOpciones", "Cerrar Sesión");
        aceptarAlerta();
        verifyThat("#txtUsuario", isVisible());
        verifyThat("#txtContrasena", isVisible());
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
     * Elige una opción de un desplegable.
     */
    private void elegir(String id, String opcion) {
        clickOn(id);
        esperar(".list-cell");
        clickOn(lookup(".list-cell").match(LabeledMatchers.hasText(opcion)).queryLabeled());
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
