package controladorTest;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

/**
 * Clase de pruebas de integración e interfaz gráfica de usuario (GUI) utilizando TestFX.
 * Evalúa la vista {@code GestionarTarifas.fxml} y las interacciones correspondientes 
 * en {@link controlador.GestionTarifasController}.
 * 
 * @author Eneko.Plaza
 */
public class GestionarTarifasControllerTest extends ApplicationTest {

    /**
     * Inicializa la interfaz gráfica antes de ejecutar cada prueba individual.
     * Carga el archivo FXML desde la ruta de recursos y configura la escena principal.
     * 
     * @param stage El escenario principal suministrado por la infraestructura de TestFX.
     * @throws IOException Si ocurre un error al cargar la vista FXML desde los recursos.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionarTarifas.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia el entorno de prueba después de la ejecución de cada test.
     * Oculta el escenario y libera los eventos de teclado o ratón que hayan quedado activos.
     * 
     * @throws Exception Si ocurre una anomalía al liberar los recursos o cerrar la ventana.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    /**
     * Comprueba la interacción directa de escritura y selección en los elementos del formulario.
     * Verifica que los valores ingresados en los {@code TextField}, {@code TextArea} y el 
     * {@code ComboBox} coincidan exactamente con la entrada del usuario.
     */
    @Test
    public void testEscrituraCamposFormulario() {
        clickOn("#txtNombre").write("Onda Fibra 600Mb");
        clickOn("#txtPrecio").write("130.0");
        clickOn("#comboTipo");
        clickOn("Fibra");
        clickOn("#txtDetalles").write("Descripción detallada de la velocidad, gigas, minutos...");

        verifyThat("#txtNombre", TextInputControlMatchers.hasText("Onda Fibra 600Mb"));
        verifyThat("#txtPrecio", TextInputControlMatchers.hasText("130.0"));
        verifyThat("#comboTipo", ComboBoxMatchers.hasSelectedItem("Fibra"));
        verifyThat("#txtDetalles", TextInputControlMatchers.hasText("Descripción detallada de la velocidad, gigas, minutos..."));
    }

    /**
     * Comprueba la funcionalidad del botón de limpieza del formulario.
     * Inserta valores temporales y confirma que los campos quedan vacíos tras pulsar el botón.
     */
    @Test
    public void testAccionBotonLimpiar() {
        clickOn("#txtNombre").write("Tarifa Temporal");
        clickOn("#txtPrecio").write("49.99");

        clickOn("#btnLimpiar");

        verifyThat("#txtNombre", TextInputControlMatchers.hasText(""));
        verifyThat("#txtPrecio", TextInputControlMatchers.hasText(""));
    }

    /**
     * Comprueba la validación del formulario cuando se intenta guardar sin ingresar datos requeridos.
     * Verifica la aparición del diálogo de alerta ({@code Alert}), valida el mensaje de error 
     * mostrado y cierra el diálogo pulsando su botón de confirmación.
     */
    @Test
    public void testErrorGuardarConCamposVaciosMuestraAlert() {
        clickOn("#btnGuardar");

        verifyThat(".dialog-pane", isVisible());

        DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
        assertEquals("Introduce el nombre.", dialog.getContentText());

        clickOn(dialog.lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba la selección de elementos en la tabla de tarifas y la confirmación de la misma.
     * Simula un clic sobre la primera fila de la tabla, pulsa el botón de confirmar tarifa y 
     * gestiona la alerta emergente.
     */
    @Test
    public void testSeleccionarFilaYConfirmarTarifa() {
        clickOn(".table-row-cell");
        clickOn("#btnConfirmar");
        verifyThat(".dialog-pane", isVisible());
        clickOn(lookup(".dialog-pane").queryAs(DialogPane.class).lookupButton(ButtonType.OK));
    }

    /**
     * Comprueba que los botones del formulario contengan los textos descriptivos correspondientes.
     */
    @Test
    public void testtextoBotones() {
        verifyThat("#btnLimpiar", LabeledMatchers.hasText("Limpiar Formulario"));
        verifyThat("#btnEliminar", LabeledMatchers.hasText("Eliminar"));
        verifyThat("#btnActualizar", LabeledMatchers.hasText("Actualizar"));
        verifyThat("#btnGuardar", LabeledMatchers.hasText("Guardar Tarifa"));
        verifyThat("#btnRechazar", LabeledMatchers.hasText("Rechazar"));
        verifyThat("#btnConfirmar", LabeledMatchers.hasText("Confirmar Tarifa"));
    }

    /**
     * Comprueba el estado por defecto del {@code CheckBox} de activación de tarifa.
     * Verifica que al cargar la pantalla la casilla esté seleccionada.
     */
    @Test
    public void testEstadoInicialCheckBox() {
        CheckBox chk = lookup("#chkActiva").queryAs(CheckBox.class);
        assertTrue(chk.isSelected());
    }

    /**
     * Comprueba el cambio de estado del {@code CheckBox} al hacer clic sobre él.
     * Verifica que pase de estar marcado a desmarcado tras la interacción.
     */
    @Test
    public void testInteraccionCheckBox() {
        clickOn("#chkActiva");

        CheckBox chk = lookup("#chkActiva").queryAs(CheckBox.class);
        assertFalse(chk.isSelected());
    }

    /**
     * Comprueba la visibilidad de todas las columnas pertenecientes a la tabla de tarifas.
     */
    @Test
    public void testColumnastarifas() {
        verifyThat("#colNombre", isVisible());
        verifyThat("#colPrecio", isVisible());
        verifyThat("#colTipo", isVisible());
        verifyThat("#colDetalles", isVisible());
        verifyThat("#colActiva", isVisible());
    }
}