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
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isDisabled;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * Clase de pruebas de integración e interfaz de usuario para la vista {@code GestionarContrato.fxml}.
 * <p>
 * Emplea el marco de pruebas TestFX para verificar la correcta carga de la escena,
 * la visibilidad de los controles de la interfaz, el estado inicial de los botones
 * y el comportamiento ante eventos de entrada del usuario.
 * </p>
 * 
 * @author eneko
 * @version 1.0
 */
public class GestionarContratosTest extends ApplicationTest {

    /**
     * Inicializa la interfaz gráfica antes de ejecutar cada prueba individual.
     * Carga el archivo FXML correspondiente a la ventana de gestión de contratos
     * y la muestra en la ventana principal.
     *
     * @param stage El escenario principal {@link Stage} proporcionado por TestFX.
     * @throws IOException Si ocurre un error al intentar cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionarContrato.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia el entorno tras la ejecución de cada prueba.
     * Oculta la ventana y libera los periféricos (teclado y ratón) para evitar
     * interferencias o dependencias entre pruebas consecutivas.
     *
     * @throws Exception Si ocurre un fallo al ocultar la ventana en FxToolkit.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    //  VISIBILIDAD DE CONTROLES GENERALES
    

    /**
     * Verifica que el componente {@code TableView} de contratos sea visible en la interfaz.
     */
    @Test
    public void testComprobarTablaContrato() {
        verifyThat("#tablaContratos", isVisible());
    }

    /**
     * Verifica que el campo de texto para filtrar por teléfono sea visible.
     */
    @Test
    public void testComprobarFiltroTelefono() {
        verifyThat("#txtFiltroTelefono", isVisible());
    }

    /**
     * Verifica que el desplegable {@code ComboBox} de filtro por estado sea visible.
     */
    @Test
    public void testComprobarFiltroEstado() {
        verifyThat("#comboFiltroEstado", isVisible());
    }

    /**
     * Comprueba que el botón de filtrado contenga la etiqueta de texto adecuada.
     */
    @Test
    public void testBotonFiltrarTieneTextoCorrecto() {
        verifyThat("#btnFiltrar", hasText("Filtrar"));
    }

    /**
     * Comprueba la interacción al hacer clic en el botón de refrescar y verifica su texto.
     */
    @Test
    public void comprobarBotonRefrescar() {
        clickOn("#btnRefrescar");
        verifyThat("#btnRefrescar", hasText("Refrescar"));
    }

    // ESTRUCTURA DE COLUMNAS DE LA TABLA

    /**
     * Verifica que la columna del ID de contrato sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaIdContrato() {
        verifyThat("#colIdContrato", isVisible());
    }

    /**
     * Verifica que la columna de teléfono sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaTelefono() {
        verifyThat("#colTelefono", isVisible());
    }

    /**
     * Verifica que la columna del ID de cliente sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaIdCliente() {
        verifyThat("#colIdCliente", isVisible());
    }

    /**
     * Verifica que la columna del ID de tarifa sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaIdTarifa() {
        verifyThat("#colIdTarifa", isVisible());
    }

    /**
     * Verifica que la columna del ID de empleado sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaIdEmpleado() {
        verifyThat("#colIdEmpleado", isVisible());
    }

    /**
     * Verifica que la columna de estado del contrato sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaEstado() {
        verifyThat("#colEstado", isVisible());
    }

    /**
     * Verifica que la columna de fecha de inicio del contrato sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaFechaInicio() {
        verifyThat("#colFechaInicio", isVisible());
    }

    /**
     * Verifica que la columna de fecha de fin del contrato sea visible en la tabla.
     */
    @Test
    public void comprobarColumnaFechaFin() {
        verifyThat("#colFechaFin", isVisible());
    }

    //ESTADO INICIAL DE BOTONES DE ACCIÓN

    /**
     * Verifica que el botón para aceptar una petición inicie deshabilitado al no haber filas seleccionadas.
     */
    @Test
    public void comprobarBotonAceptarPeticion() {
        verifyThat("#btnAceptarPeticion", isDisabled());
    }

    /**
     * Verifica que el botón para rechazar una petición inicie deshabilitado al no haber filas seleccionadas.
     */
    @Test
    public void comprobarBotonRechazarPeticion() {
        verifyThat("#btnRechazarPeticion", isDisabled());
    }

    /**
     * Verifica que el botón para rescindir un contrato inicie deshabilitado al no haber filas seleccionadas.
     */
    @Test
    public void comprobarBotonRescindirContrato() {
        verifyThat("#btnRescindirContrato", isDisabled());
    }

    // ==========================================
    //  PRUEBAS DE INTERACCIÓN Y ENTRADA DE DATOS
    // ==========================================

    /**
     * Simula la escritura de un número telefónico en el campo de texto de filtro
     * y comprueba que el control refleje el valor introducido.
     */
    @Test
    public void testEscrituraEnFiltroTelefono() {
        clickOn("#txtFiltroTelefono").write("600123456");
        verifyThat("#txtFiltroTelefono", TextInputControlMatchers.hasText("600123456"));
    }
    


    /**
     * Simula la selección del valor "activo" dentro del desplegable {@code ComboBox} de estado
     * y valida que quede marcado correctamente.
     */
    @Test
    public void testSeleccionFiltroEstado() {
        clickOn("#comboFiltroEstado");
        clickOn("activo");
        verifyThat("#comboFiltroEstado", ComboBoxMatchers.hasSelectedItem("activo"));
    }

    /**
     * Valida la funcionalidad del botón de refresco comprobando que limpie la entrada del teléfono
     * y restablezca la opción predeterminada ("Todos") en el filtro de estado.
     */
    @Test
    public void testBotonRefrescarLimpiarCampos() {
        clickOn("#txtFiltroTelefono").write("999999999");
        clickOn("#comboFiltroEstado");
        clickOn("pendiente");
        
        clickOn("#btnRefrescar");
        
        verifyThat("#txtFiltroTelefono", TextInputControlMatchers.hasText(""));
        verifyThat("#comboFiltroEstado", ComboBoxMatchers.hasSelectedItem("Todos"));
    }
}