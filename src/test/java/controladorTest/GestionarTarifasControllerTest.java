/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import static org.testfx.matcher.base.NodeMatchers.isInvisible;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 *
 * @author Eneko.Plaza
 */
public class GestionarTarifasControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionarTarifas.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

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

    @Test
    public void testBotonLimpiarFormulario() {
        verifyThat("#btnLimpiar", hasText("Limpiar Formulario"));
    }

    @Test
    public void testBotonEliminar() {
        verifyThat("#btnEliminar", hasText("Eliminar"));
    }

    @Test
    public void testBotonActualizar() {
        verifyThat("#btnActualizar", hasText("Actualizar"));
    }

    @Test
    public void testBotonGuardar() {
        verifyThat("#btnGuardar", hasText("Guardar Tarifa"));
    }

    @Test
    public void testBotonRechazar(){
        verifyThat("#btnRechazar", hasText("Rechazar"));
    }
    
    @Test
    public void testBotonAceptar(){
        verifyThat("#btnConfirmar", hasText("Confirmar Tarifa"));
    }    
    @Test
    public void testColumnasTarifasNombre() {
        verifyThat("#colNombre", isVisible());
    }

    @Test
    public void testColumnasTarifasPrecio() {
        verifyThat("#colPrecio", isVisible());
    }

    @Test
    public void testColumnasTarifasTipo() {
        verifyThat("#colTipo", isVisible());
    }
    
    @Test
    public void testColumnasTarifasDetalles() {
        verifyThat("#colDetalles", isVisible());
    }
    
    @Test
    public void testColumnasTarifasActiva() {
        verifyThat("#colActiva", isVisible());
    }
}
