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

import exception.ErrorMessages;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

/**
 * Pruebas de la pantalla Mi Perfil.
 */
public class ClientePerfilControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        sesionPedro();
        // El panel abre Mi Perfil al entrar
        Parent root = FXMLLoader.load(getClass().getResource("/vista/Cliente.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Se cargan los datos del cliente.
     */
    @Test
    public void testDatosCargados() {
        verifyThat("#txtNombre", TextInputControlMatchers.hasText("Pedro"));
        verifyThat("#txtNombreUsuario", TextInputControlMatchers.hasText("PedroDiaz0"));
        verifyThat("#txtDni", TextInputControlMatchers.hasText("12345678Z"));
        verifyThat("#txtCorreo", TextInputControlMatchers.hasText("pedrodiaz@gmail.com"));
    }

    /**
     * Con un campo vacío sale un aviso.
     */
    @Test
    public void testCampoVacio() {
        escribir("#txtNombre", "");
        clickOn("#btnModificar");
        assertEquals("Por favor, rellena todos los campos.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Un DNI con la letra mal no vale.
     */
    @Test
    public void testDniIncorrecto() {
        escribir("#txtDni", "12345678A");
        clickOn("#btnModificar");
        assertEquals("DNI o NIE incorrecto (comprueba también la letra).", textoAlerta());
        aceptarAlerta();
    }

    /**
     * Un correo sin @ no vale.
     */
    @Test
    public void testCorreoIncorrecto() {
        escribir("#txtCorreo", "pedrogmail.com");
        clickOn("#btnModificar");
        assertEquals("El correo no es válido.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * El teléfono tiene que tener 9 números.
     */
    @Test
    public void testTelefonoIncorrecto() {
        escribir("#txtTelefono", "123");
        clickOn("#btnModificar");
        assertEquals("El teléfono debe tener 9 números.", textoAlerta());
        aceptarAlerta();
    }

    /**
     * La cuenta tiene que ser ES y 22 números.
     */
    @Test
    public void testCuentaIncorrecta() {
        escribir("#txtCuentaBancaria", "ES12");
        clickOn("#btnModificar");
        assertEquals("La cuenta bancaria debe ser ES seguido de 22 números (o dejarla vacía).", textoAlerta());
        aceptarAlerta();
    }

    /**
     * No deja usar el usuario de otra persona.
     */
    @Test
    public void testUsuarioRepetido() {
        escribir("#txtNombreUsuario", "Pablodiaz0");
        clickOn("#btnModificar");
        aceptarAlerta();
        assertEquals(ErrorMessages.USER_EXISTS, textoAlerta());
        aceptarAlerta();
    }

    /**
     * Si se cancela no cambia nada.
     */
    @Test
    public void testCancelarCambios() {
        escribir("#txtNombre", "Otro");
        clickOn("#btnModificar");
        assertEquals("¿Seguro que deseas guardar los cambios?", textoAlerta());
        cancelarAlerta();
        verifyThat("#lblBienvenida", LabeledMatchers.hasText("Cliente: Pedro"));
    }

    /**
     * Guarda los datos y actualiza el saludo.
     */
    @Test
    public void testGuardarCambios() {
        escribir("#txtNombre", "Pedro");
        clickOn("#btnModificar");
        aceptarAlerta();
        assertEquals("Los datos se han actualizado correctamente.", textoAlerta());
        aceptarAlerta();
        verifyThat("#lblBienvenida", LabeledMatchers.hasText("Cliente: Pedro"));
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
     * Borra un campo y escribe un texto nuevo.
     */
    private void escribir(String id, String texto) {
        clickOn(id);
        push(KeyCode.CONTROL, KeyCode.A);
        type(KeyCode.BACK_SPACE);
        write(texto);
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