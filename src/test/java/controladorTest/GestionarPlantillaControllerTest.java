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
 * Clase de pruebas de interfaz de usuario (GUI) para la vista {@code GestionarPlantilla.fxml}.
 * <p>
 * Emplea el marco de pruebas TestFX para verificar la correcta visualización de las tablas de
 * empleados y clientes, las columnas de cada una, el estado oculto/visible del formulario integrado
 * y la interacción con botones y campos de texto.
 * </p>
 * 
 * @author eneko
 * @version 1.0
 */
public class GestionarPlantillaControllerTest extends ApplicationTest {

    /**
     * Inicializa el entorno de la interfaz gráfica cargando la vista FXML antes de cada prueba.
     *
     * @param stage Escenario principal {@link Stage} gestionado por TestFX.
     * @throws IOException Si ocurre un error al intentar cargar el recurso FXML desde la ruta indicada.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/GestionarPlantilla.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Limpia la ventana y resetea el estado físico de periféricos (teclado y ratón) al finalizar cada test.
     *
     * @throws Exception Si se produce una falla al ocultar el escenario a través de FxToolkit.
     */
    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    
    //  VISIBILIDAD DE TABLAS Y PANELES

    /**
     * Comprueba que la tabla principal de empleados sea visible.
     */
    @Test
    public void testTablaEmpleados() {
        verifyThat("#tablaEmpleados", isVisible());
    }

    /**
     * Comprueba que la tabla de clientes sea visible.
     */
    @Test
    public void testTablaClientes() {
        verifyThat("#tablaClientes", isVisible());
    }

    /**
     * Verifica que el panel desplegable del formulario inicie oculto por defecto.
     */
    @Test
    public void testPanelFormularioSeaOculto() {
        verifyThat("#panelFormulario", isInvisible());
    }

    // TEXTO DE BOTONES DE ACCIÓN

    /**
     * Comprueba el texto del botón para crear un nuevo empleado.
     */
    @Test
    public void testBotonCrearEmpleadoTieneTexto() {
        verifyThat("#btnCrearEmpleado", hasText("+ Crear Empleado"));
    }

    /**
     * Comprueba el texto del botón para ascender a un empleado a administrador.
     */
    @Test
    public void testBotonAscenderAdminTieneTexto() {
        verifyThat("#btnAscenderAdmin", hasText("Ascender a Admin"));
    }

    /**
     * Comprueba el texto del botón para dar de baja a un empleado.
     */
    @Test
    public void testBotonbajaEmpleadoTieneTexto() {
        verifyThat("#btnBajaEmpleado", hasText("Dar de Baja Empleado"));
    }

    /**
     * Comprueba el texto del botón para eliminar datos sensibles de los clientes.
     */
    @Test
    public void testBotonEliminarClienteTieneTexto() {
        verifyThat("#btnEliminarCliente", hasText("Eliminar Datos Sensibles"));
    }

    // INTERACCIÓN CON EL FORMULARIO INTEGRADO

    /**
     * Verifica que al pulsar en "+ Crear Empleado" el panel del formulario pase a estar visible.
     */
    @Test
    public void testAlternarFormularioMuestraPanle() {
        clickOn("#btnCrearEmpleado");
        verifyThat("#panelFormulario", isVisible());
    }

    /**
     * Comprueba el despliegue del formulario y la escritura de datos en las cajas de texto de nombre y apellidos.
     */
    @Test
    public void testEscrituraDeCamposFormulario() {
        clickOn("#btnCrearEmpleado");

        clickOn("#txtNombre").write("Eneko");
        clickOn("#txtApellidos").write("Plaza Vallejo");
        clickOn("#txtDireccion").write("Lonbo kalea 1,4A");
        clickOn("#cmbDireccionSucursal");
        clickOn("Lonbo kalea");
        clickOn("#txtIban").write("ES9876543210987654201098");

       
        verifyThat("#txtNombre", TextInputControlMatchers.hasText("Eneko"));
        verifyThat("#txtApellidos", TextInputControlMatchers.hasText("Plaza Vallejo"));
        
        verifyThat("#txtDireccion", TextInputControlMatchers.hasText("Lonbo kalea 1,4A"));
        
        verifyThat("#cmbDireccionSucursal", ComboBoxMatchers.hasSelectedItem("Lonbo kalea"));
        
        verifyThat("#txtIban", TextInputControlMatchers.hasText("ES9876543210987654201098"));
                
    }

    /**
     * Comprueba que al pulsar en el botón "Cancelar" el panel del formulario vuelva a quedar oculto.
     */
    @Test
    public void testBotonCancelarFormulario() {
        clickOn("#btnCrearEmpleado");
        verifyThat("#panelFormulario", isVisible());

        clickOn("#btnCancelarFormulario");
        verifyThat("#panelFormulario", isInvisible());
    }

    //  COLUMNAS DE LA TABLA EMPLEADOS

    /**
     * Verifica la visibilidad de la columna Nombre de la tabla de empleados.
     */
    @Test
    public void testColumnaEmpleNombre() {
        verifyThat("#colEmpNombre", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Apellidos de la tabla de empleados.
     */
    @Test
    public void testColumnaApellidos() {
        verifyThat("#colEmpApellidos", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Usuario de la tabla de empleados.
     */
    @Test
    public void testColumnaEmpUsuario() {
        verifyThat("#colEmpUsuario", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Rol de la tabla de empleados.
     */
    @Test
    public void testColumnaEmpRol() {
        verifyThat("#colEmpRol", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Email de la tabla de empleados.
     */
    @Test
    public void testColumnaEmpEmail() {
        verifyThat("#colEmpEmail", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Dirección de la tabla de empleados.
     */
    @Test
    public void testColumnaEmpDireccion() {
        verifyThat("#colEmpDireccion", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Sucursal de la tabla de empleados.
     */
    @Test
    public void testColumnaEmpSucursal() {
        verifyThat("#colEmpSucursal", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna IBAN de la tabla de empleados.
     */
    @Test
    public void testColumnaEmpleadoIBAN() {
        verifyThat("#colEmpIBAN", isVisible());
    }

    // COLUMNAS DE LA TABLA CLIENTES

    /**
     * Verifica la visibilidad de la columna Nombre de la tabla de clientes.
     */
    @Test
    public void testColumnaClienteNombre() {
        verifyThat("#colCliNombre", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Apellidos de la tabla de clientes.
     */
    @Test
    public void testColumnaClienteApellidos() {
        verifyThat("#colCliApellidos", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Usuario de la tabla de clientes.
     */
    @Test
    public void testColumnaClienteUsuario() {
        verifyThat("#colCliUsuario", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Email de la tabla de clientes.
     */
    @Test
    public void testColumnaClienteEmail() {
        verifyThat("#colCliEmail", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Teléfono de la tabla de clientes.
     */
    @Test
    public void testColumnaClienteTelefono() {
        verifyThat("#colCliTelefono", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna DNI de la tabla de clientes.
     */
    @Test
    public void testColumnaClienteDni() {
        verifyThat("#colCliDni", isVisible());
    }

    /**
     * Verifica la visibilidad de la columna Dirección de la tabla de clientes.
     */
    @Test
    public void testColumnaClienteDireccion() {
        verifyThat("#colCliDireccion", isVisible());
    }
}