package controladorTest;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de pruebas de interfaz de usuario (GUI) para la vista {@code Empleado.fxml}.
 * <p>
 * Emplea la librería TestFX para automatizar la comprobación de componentes
 * visuales de la interfaz de la ventana principal del empleado, tales como
 * desplegables, etiquetas de texto e inicialización de paneles dinámicos.
 * </p>
 * 
 * @author eneko
 * @version 1.0
 */
public class EmpleadoControllerTest extends ApplicationTest {

    /**
     * Inicializa la interfaz gráfica antes de la ejecución de cada prueba unitaria.
     * Carga el archivo FXML correspondiente a la vista del empleado y la asigna a la escena.
     *
     * @param stage El escenario principal {@link Stage} proporcionado por el marco TestFX.
     * @throws IOException Si ocurre una excepción de lectura al intentar cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Empleado.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia el entorno y el estado de la ventana tras finalizar cada prueba.
     * Oculta el escenario activo y libera cualquier evento virtual residual de teclado y ratón.
     *
     * @throws Exception Si se produce un error durante el cierre o liberación del escenario.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * Comprueba que el desplegable {@code ComboBox} de opciones del empleado
     * sea visible correctamente en la interfaz.
     */
    @Test
    public void comprobarComboboxEmpleado() {
        verifyThat("#cbOpcionesEmpleado", isVisible());
    }

    /**
     * Verifica que la etiqueta {@code Label} destinada al nombre del empleado
     * muestre exactamente el texto esperado ("Empleado: Usuario").
     */
    @Test
    public void comprobarNombreEmpleado() {
        verifyThat("#lblNombreEmpleado", LabeledMatchers.hasText("Empleado: Usuario"));
    }

    /**
     * Verifica la visibilidad del contenedor destinado a alojar las sub-vistas o contenidos dinámicos.
     */
    @Test
    public void comprobarSubVistasEmpleado() {
        verifyThat("#contenidoDinamico", isVisible());
    }

    /**
     * Simula la interacción de hacer clic sobre el desplegable de opciones del empleado
     * y comprueba que dicho control siga estando disponible y visible en la vista.
     */
    @Test
    public void comprobarClickDespegable() {
        clickOn("#cbOpcionesEmpleado");
        verifyThat("#cbOpcionesEmpleado", isVisible());
    }
}