package controladorTest;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.testfx.api.FxAssert.verifyThat;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code RegistrarseController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code Registrarse.fxml} y comprobar las validaciones
 * del formulario. No se completa ningún registro, así que no se guarda nada en MongoDB.
 * </p>
 *
 * @author sachin
 * @version 1.0
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class RegistrarseControllerTest extends ApplicationTest {

    /**
     * Inicializa y muestra el escenario cargando el archivo FXML de la vista.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Registrarse.fxml"));
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
     * Comprueba que al abrir el formulario esté vacío y sin mensaje.
     */
    @Test
    public void comprobarEstadoInicial() {
        verifyThat("#txtUsuario", TextInputControlMatchers.hasText(""));
        verifyThat("#txtNombre", TextInputControlMatchers.hasText(""));
        verifyThat("#txtDni", TextInputControlMatchers.hasText(""));
        verifyThat("#lblMensajeError", LabeledMatchers.hasText(""));
        verifyThat("#btnRegistrarse", LabeledMatchers.hasText("Completar Registro"));
    }

    /**
     * Comprueba que sin rellenar nada se pida rellenar los campos.
     */
    @Test
    public void comprobarCamposVacios() {
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Comprueba que con un campo vacío se pida rellenar los campos.
     */
    @Test
    public void comprobarFaltaUnCampo() {
        rellenar("usuarioPrueba", "123456", "12345678Z", "prueba@gmail.com");
        vaciar("#txtDireccion");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("Rellena todos los campos."));
    }

    /**
     * Comprueba que un correo sin arroba no se acepte.
     */
    @Test
    public void comprobarCorreoIncorrecto() {
        rellenar("usuarioPrueba", "123456", "12345678Z", "correo-sin-arroba");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("El correo no es válido."));
    }

    /**
     * Comprueba que un DNI sin letra no se acepte.
     */
    @Test
    public void comprobarDniIncorrecto() {
        rellenar("usuarioPrueba", "123456", "12345678", "prueba@gmail.com");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("DNI introducido incorrectamente."));
    }

    /**
     * Comprueba que una contraseña de menos de 6 caracteres no se acepte.
     */
    @Test
    public void comprobarContrasenaCorta() {
        rellenar("usuarioPrueba", "123", "12345678Z", "prueba@gmail.com");
        clickOn("#btnRegistrarse");
        verifyThat("#lblMensajeError", LabeledMatchers.hasText("La contraseña debe tener mínimo 6 caracteres."));
    }

    /**
     * Rellena el formulario de registro.
     *
     * @param usuario nombre de usuario
     * @param contrasena contraseña
     * @param dni DNI
     * @param correo correo
     */
    private void rellenar(String usuario, String contrasena, String dni, String correo) {
        clickOn("#txtUsuario").write(usuario);
        clickOn("#txtContrasena").write(contrasena);
        clickOn("#txtNombre").write("Prueba");
        clickOn("#txtApellidos").write("Test");
        clickOn("#txtDni").write(dni);
        clickOn("#txtTelefono").write("666666666");
        clickOn("#txtCorreo").write(correo);
        clickOn("#txtDireccion").write("Calle Falsa 1");
    }

    /**
     * Vacía un campo de texto del formulario.
     *
     * @param id selector del campo
     */
    private void vaciar(String id) {
        final TextInputControl campo = lookup(id).queryAs(TextInputControl.class);
        interact(new Runnable() {
            @Override
            public void run() {
                campo.clear();
            }
        });
    }
}
