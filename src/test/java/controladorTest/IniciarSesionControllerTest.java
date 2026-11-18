package controladorTest;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code IniciarSesionController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code IniciarSesion.fxml} y comprobar los mensajes
 * del formulario, sin depender de los usuarios guardados en MongoDB.
 * </p>
 *
 * @author sachin
 * @version 1.0
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class IniciarSesionControllerTest extends ApplicationTest {

    /**
     * Inicializa y muestra el escenario cargando el archivo FXML de la vista.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/IniciarSesion.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
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
     * Comprueba que al abrir los campos estén vacíos y no haya mensaje de error.
     */
    @Test
    public void comprobarEstadoInicial() {
        verifyThat("#txtUsuario", TextInputControlMatchers.hasText(""));
        verifyThat("#txtContrasena", TextInputControlMatchers.hasText(""));
        verifyThat("#lblMensajeError", LabeledMatchers.hasText(""));
        verifyThat("Iniciar Sesión", isVisible());
        verifyThat("Registrarse", isVisible());
    }

    /**
     * Comprueba que sin rellenar nada se pida rellenar los campos.
     */
    @Test
    public void comprobarCamposVacios() {
        clickOn("Iniciar Sesión");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Comprueba que sin contraseña se pida rellenar los campos.
     */
    @Test
    public void comprobarFaltaContrasena() {
        clickOn("#txtUsuario").write("usuarioPrueba");
        clickOn("Iniciar Sesión");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Comprueba que sin usuario se pida rellenar los campos.
     */
    @Test
    public void comprobarFaltaUsuario() {
        clickOn("#txtContrasena").write("abcd");
        clickOn("Iniciar Sesión");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Comprueba que un usuario que no existe no pueda entrar.
     */
    @Test
    public void comprobarUsuarioIncorrecto() {
        clickOn("#txtUsuario").write("usuario_que_no_existe_9871");
        clickOn("#txtContrasena").write("clave_incorrecta");
        clickOn("Iniciar Sesión");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Usuario o contraseña incorrectos."));
    }

    /**
     * Comprueba que el botón Registrarse abra el formulario de registro.
     */
    @Test
    public void comprobarAbrirRegistro() {
        clickOn("Registrarse");
        verifyThat("#btnRegistrarse", isVisible());
        verifyThat("#txtDni", isVisible());
    }
}
