package test;


import org.testfx.util.WaitForAsyncUtils;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.AfterEach;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

/**
 * Pruebas de la pantalla Iniciar Sesión.
 */
public class IniciarSesionControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Al abrir, los campos y el mensaje de error están vacíos.
     */
    @Test
    public void testEstadoInicial() {
        verifyThat("#txtUsuario", TextInputControlMatchers.hasText(""));
        verifyThat("#txtContrasena", TextInputControlMatchers.hasText(""));
        verifyThat("#lblMensajeError", LabeledMatchers.hasText(""));
    }

    /**
     * Sin rellenar los campos sale un error.
     */
    @Test
    public void testCamposVacios() {
        pulsarBoton("Iniciar Sesión");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Solo con el usuario también sale el error.
     */
    @Test
    public void testFaltaContrasena() {
        clickOn("#txtUsuario").write("PedroDiaz0");
        pulsarBoton("Iniciar Sesión");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Con una contraseña mala no entra.
     */
    @Test
    public void testContrasenaIncorrecta() {
        clickOn("#txtUsuario").write("PedroDiaz0");
        clickOn("#txtContrasena").write("mala");
        pulsarBoton("Iniciar Sesión");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Usuario o contraseña incorrectos."));
    }

    /**
     * Un cliente entra y se abre su panel.
     */
    @Test
    public void testLoginCliente() {
        clickOn("#txtUsuario").write("PedroDiaz0");
        clickOn("#txtContrasena").write("abcd");
        pulsarBoton("Iniciar Sesión");
        assertTrue(textoAlerta().contains("Rol: Cliente"));
        aceptarAlerta();
        verifyThat("#cmbOpciones", isVisible());
        verifyThat("#lblBienvenida", LabeledMatchers.hasText("Cliente: Pedro"));
    }

    /**
     * El botón Registrarse abre la pantalla de registro.
     */
    @Test
    public void testAbrirRegistro() {
        pulsarBoton("Registrarse");
        verifyThat("#btnRegistrarse", isVisible());
    }

    /**
     * Pulsa el botón que tiene ese texto.
     */
    private void pulsarBoton(String texto) {
        Button boton = lookup(".button").match(LabeledMatchers.hasText(texto)).queryButton();
        clickOn(boton);
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
