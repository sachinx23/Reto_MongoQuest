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

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code AdministradorController}.
 * <p>
 * Utiliza el framework TestFX en conjunto con JUnit 5 para inicializar la escena FXML 
 * y verificar la presencia, visibilidad e interactividad de sus componentes visuales.
 * </p>
 * 
 * @author eneko
 * @version 1.0
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class AdministradorControllerTest extends ApplicationTest {

    /**
     * Inicializa y muestra el escenario ({@link Stage}) principal cargando el archivo FXML de la vista.
     * Este método se ejecuta antes de cada prueba de interfaz de usuario.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML desde la ruta especificada.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Admin.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia y oculta la ventana tras la finalización de cada método de prueba.
     * <p>
     * Garantiza la independencia de la ejecución de cada prueba evitando interferencias entre ventanas.
     * </p>
     *
     * @throws Exception Si ocurre un fallo al intentar cerrar o liberar el escenario.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
    }

    /**
     * Comprueba que el desplegable {@code cbOpcionesAdmin} se cargue de forma correcta y sea visible.
     */
    @Test
    public void comprobarCombobox() {
        verifyThat("#cbOpcionesAdmin", isVisible());
    }

    /**
     * Verifica que la etiqueta {@code lblNombreAdmin} contenga el texto por defecto esperado ("Admin: Usuario").
     */
    @Test
    public void comprobarNombre() {
        verifyThat("#lblNombreAdmin", LabeledMatchers.hasText("Admin: Usuario"));
    }

    /**
     * Comprueba que el contenedor o panel principal {@code contenidoDinamico} esté cargado y visible en la vista.
     */
    @Test
    public void comprobarPanelPrincipal() {
        verifyThat("#contenidoDinamico", isVisible());
    }

    /**
     * Simula la acción de hacer clic sobre el desplegable {@code cbOpcionesAdmin} 
     * y comprueba que mantenga su visibilidad tras la interacción.
     */
    @Test
    public void comprobarClickDespegable() {
        clickOn("#cbOpcionesAdmin");
        verifyThat("#cbOpcionesAdmin", isVisible());
    }
}