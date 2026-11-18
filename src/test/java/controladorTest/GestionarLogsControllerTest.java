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

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de pruebas de integración y UI para el controlador de logs 
 * ({@code GestionLogController}) utilizando el framework TestFX.
 * 
 * <p>Esta clase se encarga de verificar que la interfaz visual de logs 
 * declarada en {@code GestionLogs.fxml} se cargue correctamente y muestre 
 * la lista de registros ({@link javafx.scene.control.ListView}).</p>
 * 
 * @author eneko
 * @version 1.0
 */
public class GestionarLogsControllerTest extends ApplicationTest {

    /**
     * Inicializa el entorno de prueba de JavaFX cargando la vista FXML 
     * asociada a los logs de la aplicación.
     *
     * @param stage El escenario principal proporcionado por TestFX.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML desde el classpath.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionLogs.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Restablece el estado del entorno de prueba al finalizar cada test.
     * 
     * <p>Oculta el escenario activo y libera cualquier recurso de teclado o ratón.</p>
     *
     * @throws Exception Si surge un fallo durante la liberación de la ventana.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * Verifica la visibilidad del componente de lista de logs en la interfaz.
     * 
     * <p>Comprueba que el {@link javafx.scene.control.ListView} identificativo 
     * ({@code #verListaLogs}) esté correctamente renderizado y visible.</p>
     */
    @Test
    public void testVerLogs() {
        verifyThat("#verListaLogs", isVisible());
    }
}