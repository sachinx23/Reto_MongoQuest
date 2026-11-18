package controladorTest;

import controlador.ClienteController;
import controlador.IniciarSesionController;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import modelo.Cliente;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code ClientePerfilController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code ClientePerfil.fxml} con un cliente de prueba
 * creado en memoria y comprobar las validaciones del formulario sin guardar nada en MongoDB.
 * </p>
 *
 * @author sachin
 * @version 1.0
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class ClientePerfilControllerTest extends ApplicationTest {

    /**
     * Inicializa el escenario cargando la vista con un cliente de prueba.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        IniciarSesionController.idUsuarioActual = null;
        ClienteController.setCliente(new Cliente(new ObjectId().toHexString(), "ES7921000813610123456789",
                "usuarioPrueba", "Prueba", "Test", "abcd", "prueba@gmail.com", "12345678Z", "666666666", "Calle Falsa 1"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/ClientePerfil.fxml"));
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
     * Comprueba que al abrir se vean los datos del cliente.
     */
    @Test
    public void comprobarDatosCargados() {
        verifyThat("#txtNombre", TextInputControlMatchers.hasText("Prueba"));
        verifyThat("#txtNombreUsuario", TextInputControlMatchers.hasText("usuarioPrueba"));
        verifyThat("#txtDni", TextInputControlMatchers.hasText("12345678Z"));
        verifyThat("#txtCorreo", TextInputControlMatchers.hasText("prueba@gmail.com"));
        verifyThat("#txtTelefono", TextInputControlMatchers.hasText("666666666"));
        verifyThat("#txtCuentaBancaria", TextInputControlMatchers.hasText("ES7921000813610123456789"));
        verifyThat("#txtDireccion", TextInputControlMatchers.hasText("Calle Falsa 1"));
        verifyThat("#btnModificar", LabeledMatchers.hasText("_Guardar cambios"));
    }

    /**
     * Comprueba que con un campo vacío aparezca un aviso.
     */
    @Test
    public void comprobarCampoVacio() {
        vaciar("#txtNombre");
        clickOn("#btnModificar");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Por favor, rellena todos los campos.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que un DNI con la letra mal no se guarde.
     */
    @Test
    public void comprobarDniIncorrecto() {
        vaciar("#txtDni");
        clickOn("#txtDni").write("12345678A");
        clickOn("#btnModificar");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("DNI o NIE incorrecto (comprueba también la letra).", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que un correo sin formato válido no se guarde.
     */
    @Test
    public void comprobarCorreoIncorrecto() {
        vaciar("#txtCorreo");
        clickOn("#txtCorreo").write("correo-sin-arroba");
        clickOn("#btnModificar");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("El correo no es válido.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que un teléfono que no tiene 9 números no se guarde.
     */
    @Test
    public void comprobarTelefonoIncorrecto() {
        vaciar("#txtTelefono");
        clickOn("#txtTelefono").write("12345");
        clickOn("#btnModificar");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("El teléfono debe tener 9 números.", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que una cuenta que no es un IBAN español no se guarde.
     */
    @Test
    public void comprobarCuentaIncorrecta() {
        vaciar("#txtCuentaBancaria");
        clickOn("#txtCuentaBancaria").write("ES12");
        clickOn("#btnModificar");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("La cuenta bancaria debe ser ES seguido de 22 números (o dejarla vacía).", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que un NIE correcto se acepte y se pida confirmación.
     */
    @Test
    public void comprobarNieCorrecto() {
        vaciar("#txtDni");
        clickOn("#txtDni").write("X1234567L");
        clickOn("#btnModificar");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("¿Seguro que deseas guardar los cambios?", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.CANCEL));
    }

    /**
     * Comprueba que al cancelar la confirmación los datos sigan en pantalla.
     */
    @Test
    public void comprobarCancelarCambios() {
        vaciar("#txtNombre");
        clickOn("#txtNombre").write("Nombre Nuevo");
        clickOn("#btnModificar");
        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("¿Seguro que deseas guardar los cambios?", dialog.getContentText());
        clickOn(dialog.lookupButton(ButtonType.CANCEL));
        verifyThat("#txtNombre", TextInputControlMatchers.hasText("Nombre Nuevo"));
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
