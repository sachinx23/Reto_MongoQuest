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
import org.testfx.matcher.control.LabeledMatchers;

import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

/**
 * Pruebas de la pantalla Mis Valoraciones.
 */
public class ClienteValoracionesControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        sesionPedro();
        Parent root = FXMLLoader.load(getClass().getResource("/vista/ClienteValoraciones.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Número de valoraciones de la tabla.
     */
    private int filas() {
        TableView<?> tabla = lookup("#tablaValoraciones").queryTableView();
        return tabla.getItems().size();
    }

    /**
     * Pedro tiene valoraciones y hay tarifas para elegir.
     */
    @Test
    public void testEstadoInicial() {
        assertTrue(filas() > 0);
        verifyThat("#cmbPuntuacion", ComboBoxMatchers.hasItems(5));
        verifyThat("#txtValoracion", TextInputControlMatchers.hasText(""));
    }

    /**
     * Sin rellenar nada pide primero la tarifa.
     */
    @Test
    public void testValorarVacio() {
        clickOn("#btnValorar");
        assertEquals("Elige la tarifa que quieres valorar.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Con tarifa pero sin puntuación ni texto avisa.
     */
    @Test
    public void testValorarSinPuntuacion() {
        elegir("#cmbTarifa", "Onda Fibra 1G");
        clickOn("#btnValorar");
        assertEquals("Elige una puntuación y escribe tu opinión.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Hay que elegir la tarifa.
     */
    @Test
    public void testValorarSinTarifa() {
        elegir("#cmbPuntuacion", "4");
        clickOn("#txtValoracion").write("Sin tarifa");
        clickOn("#btnValorar");
        assertEquals("Elige la tarifa que quieres valorar.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Editar sin elegir valoración avisa.
     */
    @Test
    public void testEditarSinSeleccion() {
        clickOn("#btnEditarValoracion");
        assertEquals("Selecciona una valoración de la tabla primero.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Eliminar sin elegir valoración avisa.
     */
    @Test
    public void testEliminarSinSeleccion() {
        clickOn("#btnEliminarValoracion");
        assertEquals("Selecciona una valoración de la tabla primero.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Al elegir una fila se cargan sus datos.
     */
    @Test
    public void testSeleccionCargaDatos() {
        clickOn("Muy buena velocidad");
        verifyThat("#cmbPuntuacion", ComboBoxMatchers.hasSelectedItem(5));
        verifyThat("#txtValoracion", TextInputControlMatchers.hasText("Muy buena velocidad"));
    }

    /**
     * Si se cancela no se publica.
     */
    @Test
    public void testCancelarValorar() {
        int antes = filas();
        elegir("#cmbTarifa", "Onda Fibra 1G");
        elegir("#cmbPuntuacion", "4");
        clickOn("#txtValoracion").write("No se publica");
        clickOn("#btnValorar");
        cancelarAlerta();
        assertEquals(antes, filas());
    }

    /**
     * Publica una valoración y luego la elimina.
     */
    @Test
    public void testValorarYEliminar() {
        int antes = filas();
        elegir("#cmbTarifa", "Onda Fibra 1G");
        elegir("#cmbPuntuacion", "4");
        clickOn("#txtValoracion").write("Prueba test");
        clickOn("#btnValorar");
        aceptarAlerta();
        assertEquals("Tu valoración ha sido registrada correctamente. ¡Gracias por tu opinión!", textoAlerta());
        aceptarAlerta();
        assertEquals(antes + 1, filas());
        clickOn("Prueba test");
        clickOn("#btnEliminarValoracion");
        aceptarAlerta();
        assertEquals("La valoración ha sido eliminada.", textoAlerta());
        aceptarAlerta();
        assertEquals(antes, filas());
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