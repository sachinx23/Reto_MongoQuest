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
import org.testfx.matcher.control.ComboBoxMatchers;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de pruebas de integración y UI para el controlador de gestión de
 * tickets ({@code TicketsSoporteController}) utilizando la librería TestFX.
 *
 * <p>
 * Esta clase se encarga de verificar la correcta inicialización de la interfaz
 * gráfica expuesta en {@code GestionarTickets.fxml}, probando la visibilidad y
 * comportamiento de componentes como botones, desplegables y tablas.</p>
 *
 * @author eneko
 * @version 1.0
 */
public class GestionarTicketsControllerTest extends ApplicationTest {

    /**
     * Inicializa el entorno de prueba de JavaFX cargando el archivo FXML
     * correspondiente a la vista de gestión de tickets y mostrándolo en el
     * {@link Stage}.
     *
     * @param stage El escenario principal proporcionado por la infraestructura
     * de TestFX.
     * @throws IOException Si ocurre un error al intentar cargar la vista FXML
     * desde el classpath.
     */
@Override
public void start(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionarTickets.fxml"));
    Parent root = loader.load();
    stage.setScene(new Scene(root));
    stage.show();
}

    /**
     * Limpia y restablece el estado del entorno de prueba después de la
     * ejecución de cada test.
     *
     * <p>
     * Oculta el escenario de la aplicación mediante {@link FxToolkit} y libera
     * cualquier tecla o botón del ratón que haya podido quedar pulsado durante
     * las interacciones.</p>
     *
     * @throws Exception Si surge algún error durante la liberación de recursos
     * de la ventana.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * Comprueba el estado inicial de los desplegables
     * ({@link javafx.scene.control.ComboBox}).
     *
     * <p>
     * Verifica que tanto el filtro de estados como el selector de nuevo estado
     * sean visibles y que el filtro de estados tenga seleccionada por defecto
     * la opción "Todos".</p>
     */
    @Test
    public void testEstadoInicialComboBox() {
        verifyThat("#comboFiltroEstado", isVisible());
        verifyThat("#comboFiltroEstado", ComboBoxMatchers.hasSelectedItem("Todos"));
        verifyThat("#comboNuevoEstado", isVisible());
    }

    /**
     * Verifica el correcto funcionamiento visual e interacción del botón de
     * refrescar.
     *
     * <p>
     * Simula un clic del usuario sobre el botón {@code #btnRefrescar} y
     * comprueba que este continúe estando visible en la interfaz.</p>
     */
    @Test
    public void testBotonRefrescar() {
        clickOn("#btnRefrescar");
        verifyThat("#btnRefrescar", isVisible());
    }

    /**
     * Valida la presencia de la tabla de tickets y el área de descripción
     * detallada.
     *
     * <p>
     * Comprueba que la
     * {@link javafx.scene.control.TableView} ({@code #tablaTickets}) y el campo
     * de texto ({@code #txtDescripcion}) se muestren correctamente, e
     * interactúa con la tabla simulando una selección de celda/fila.</p>
     */
    @Test
    public void testTablaTicketsYComponentes() {
        verifyThat("#tablaTickets", isVisible());
        verifyThat("#txtDescripcion", isVisible());
        clickOn("#tablaTickets");
    }
}
