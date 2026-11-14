package test;


import org.testfx.util.WaitForAsyncUtils;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
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

import DAO.implementacion.ClienteDAOImplementacion;
import exception.OurException;
import java.util.Random;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

/**
 * Pruebas de la pantalla Registrarse.
 */
public class RegistrarseControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/Registrarse.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Rellena todos los campos del formulario.
     */
    private void rellenar(String usuario, String contrasena, String dni, String correo) {
        clickOn("#txtUsuario").write(usuario);
        clickOn("#txtContrasena").write(contrasena);
        clickOn("#txtNombre").write("Prueba");
        clickOn("#txtApellidos").write("Test");
        clickOn("#txtDni").write(dni);
        clickOn("#txtTelefono").write("600111222");
        clickOn("#txtCorreo").write(correo);
        clickOn("#txtDireccion").write("Calle Test 1");
    }

    /**
     * Al abrir, los campos están vacíos.
     */
    @Test
    public void testEstadoInicial() {
        verifyThat("#txtUsuario", TextInputControlMatchers.hasText(""));
        verifyThat("#txtContrasena", TextInputControlMatchers.hasText(""));
        verifyThat("#txtDni", TextInputControlMatchers.hasText(""));
        verifyThat("#btnRegistrarse", isVisible());
    }

    /**
     * Sin rellenar sale un error.
     */
    @Test
    public void testCamposVacios() {
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Un correo sin @ no vale.
     */
    @Test
    public void testCorreoIncorrecto() {
        rellenar("usuarioTest", "123456", "12345678Z", "correomal");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("El correo no es válido."));
    }

    /**
     * Un DNI sin letra no vale.
     */
    @Test
    public void testDniIncorrecto() {
        rellenar("usuarioTest", "123456", "1234", "test@gmail.com");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("DNI introducido incorrectamente."));
    }

    /**
     * La contraseña necesita 6 caracteres.
     */
    @Test
    public void testContrasenaCorta() {
        rellenar("usuarioTest", "123", "12345678Z", "test@gmail.com");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("La contraseña debe tener mínimo 6 caracteres."));
    }

    /**
     * Un usuario que ya existe no se registra.
     */
    @Test
    public void testUsuarioRepetido() {
        rellenar("PedroDiaz0", "123456", "12345678Z", "test@gmail.com");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("No se pudo registrar el usuario. Comprueba los datos."));
    }

    /**
     * Un usuario nuevo se registra y vuelve al login.
     *
     * @throws OurException si falla MongoDB al borrarlo
     */
    @Test
    public void testRegistroCorrecto() throws OurException {
        String usuario = "test" + new Random().nextInt(100000);
        rellenar(usuario, "123456", "12345678Z", "test@gmail.com");
        clickOn("#btnRegistrarse");
        assertEquals("¡Usuario registrado correctamente!", textoAlerta());
        aceptarAlerta();
        verifyThat("#lblMensajeError", isVisible());
        verifyThat("#txtUsuario", TextInputControlMatchers.hasText(""));
        // Deja la base de datos como estaba
        new ClienteDAOImplementacion().eliminarDatosSensiblesCliente(usuario);
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
