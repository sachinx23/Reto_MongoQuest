/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladorTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
 * Clase de pruebas funcionales de interfaz gráfica (UI) mediante TestFX y JUnit 5
 * para la vista {@code GestionarValoraciones.fxml} y su controlador asociado.
 * Verifica la visibilidad, estado inicial e interacción básica de los principales
 * controles de filtrado, tabla de valoraciones, etiqueta de media y botones de acción.
 *
 * @author eneko
 */
public class GestionarValoracionesControllerTest extends ApplicationTest {

    /**
     * Inicializa y muestra el escenario principal de JavaFX cargando la vista
     * {@code /vista/GestionarValoraciones.fxml} antes de ejecutar cada prueba.
     *
     * @param stage Escenario ({@link Stage}) principal proporcionado por TestFX.
     * @throws IOException Si ocurre un error al cargar el archivo FXML de la vista.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionarValoraciones.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia el entorno gráfico tras cada prueba ocultando la ventana activa y
     * liberando cualquier tecla o botón del ratón que pudiera haber quedado pulsado.
     *
     * @throws Exception Si se produce algún error durante el cierre del {@link Stage} en {@link FxToolkit}.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
    
    /**
     * Prueba la interacción y el estado por defecto del desplegable de puntuaciones
     * ({@code #cbPuntuacion}), comprobando que sea visible tras hacer clic y que
     * tenga seleccionada inicialmente la opción {@code "Todas"}.
     */
    @Test
    public void testComboPuntuacion(){
        clickOn("#cbPuntuacion");
        verifyThat("#cbPuntuacion",isVisible());
        verifyThat("#cbPuntuacion", ComboBoxMatchers.hasSelectedItem("Todas"));
    }
    
    /**
     * Verifica que el botón de filtrado ({@code #btnFiltro}) sea visible en pantalla
     * y responda correctamente al evento de clic.
     */
    @Test
    public void testBotonFiltro(){
        clickOn("#btnFiltro");
        verifyThat("#btnFiltro", isVisible());
    }
    
    /**
     * Comprueba la visibilidad de la tabla de valoraciones ({@code #tabla}), la etiqueta
     * de puntuación media ({@code #lblMedia}) y el botón de eliminar ({@code #btnEliminar}),
     * así como la interacción básica de clic sobre la tabla.
     */
    @Test
    public void testTablaPuntuacion(){
        verifyThat("#tabla", isVisible());
        verifyThat("#lblMedia", isVisible());
        verifyThat("#btnEliminar", isVisible());
        clickOn("#tabla");
    }
}