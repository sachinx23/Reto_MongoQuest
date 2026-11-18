package controladorTest;

import controlador.ClienteController;
import controlador.ClienteValoracionesController;
import controlador.IniciarSesionController;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import modelo.Cliente;
import modelo.Tarifa;
import modelo.Valoracion;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * Clase de prueba automatizada para validar la interfaz gráfica del controlador {@code ClienteValoracionesController}.
 * <p>
 * Utiliza TestFX con JUnit 5 para cargar la vista {@code ClienteValoraciones.fxml}. Las tarifas del
 * desplegable y la valoración de la tabla se cargan desde el propio test, por lo que las pruebas
 * no dependen de los datos guardados en MongoDB.
 * </p>
 * <p>
 * Los desplegables se rellenan por código y, antes de leer un diálogo, el test espera a que aparezca,
 * para evitar fallos intermitentes de sincronización.
 * </p>
 *
 * @author sachin
 * @version 1.1
 * @see org.testfx.framework.junit5.ApplicationTest
 */
public class ClienteValoracionesControllerTest extends ApplicationTest {

    /**
     * Inicializa el escenario cargando la vista con un cliente, dos tarifas y una valoración de prueba.
     *
     * @param stage Escenario sobre el cual se montará la escena cargada desde el FXML.
     * @throws Exception Si ocurre un error al cargar el FXML o al preparar los datos.
     */
    @Override
    public void start(Stage stage) throws Exception {
        IniciarSesionController.idUsuarioActual = null;
        ClienteController.setCliente(new Cliente(new ObjectId().toHexString(), "ES7921000813610123456789",
                "usuarioPrueba", "Prueba", "Test", "abcd", "prueba@gmail.com", "12345678Z", "666666666", "Calle Falsa 1"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/ClienteValoraciones.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();

        List<Tarifa> tarifas = new ArrayList<>();
        tarifas.add(new Tarifa("t1", "Tarifa Prueba Fibra", 40, "600 Mb", "Fibra", true));
        tarifas.add(new Tarifa("t2", "Tarifa Prueba Movil", 15, "30 GB", "Móvil", true));
        ClienteValoracionesController controlador = loader.getController();
        Field campo = ClienteValoracionesController.class.getDeclaredField("tarifas");
        campo.setAccessible(true);
        campo.set(controlador, tarifas);
        ComboBox<String> cmbTarifa = lookup("#cmbTarifa").queryComboBox();
        cmbTarifa.setItems(FXCollections.observableArrayList("Tarifa Prueba Fibra", "Tarifa Prueba Movil"));

        List<Valoracion> valoraciones = new ArrayList<>();
        valoraciones.add(new Valoracion("v1", 4, "Va muy bien", "x", "t1", "Prueba", "Tarifa Prueba Fibra"));
        TableView<Valoracion> tabla = lookup("#tablaValoraciones").queryTableView();
        tabla.setItems(FXCollections.observableArrayList(valoraciones));
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
     * Selecciona por código una tarifa en el desplegable de tarifas.
     *
     * @param nombre Nombre de la tarifa a seleccionar.
     */
    private void seleccionarTarifa(final String nombre) {
        final ComboBox<String> combo = lookup("#cmbTarifa").queryComboBox();
        interact(new Runnable() {
            @Override
            public void run() {
                combo.getSelectionModel().select(nombre);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Selecciona por código una puntuación en el desplegable de puntuaciones.
     *
     * @param valor Puntuación a seleccionar.
     */
    private void seleccionarPuntuacion(final int valor) {
        final ComboBox<Integer> combo = lookup("#cmbPuntuacion").queryComboBox();
        interact(new Runnable() {
            @Override
            public void run() {
                combo.getSelectionModel().select(Integer.valueOf(valor));
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Espera hasta 5 segundos a que aparezca un diálogo y lo devuelve.
     *
     * @return Panel del diálogo mostrado.
     * @throws Exception Si el diálogo no aparece en el tiempo indicado.
     */
    private DialogPane esperarDialogo() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return lookup(".dialog-pane").tryQuery().isPresent();
            }
        });
        return lookup(".dialog-pane").queryAs(DialogPane.class);
    }

    /**
     * Pulsa un botón del diálogo y espera a que la interfaz termine de actualizarse.
     *
     * @param dialog Diálogo que contiene el botón.
     * @param boton Tipo de botón a pulsar.
     */
    private void pulsarBotonDialogo(DialogPane dialog, ButtonType boton) {
        clickOn(dialog.lookupButton(boton));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Comprueba los botones, los desplegables y la valoración de la tabla.
     */
    @Test
    public void comprobarEstadoInicial() {
        verifyThat("#btnValorar", LabeledMatchers.hasText("_Valorar"));
        verifyThat("#btnEditarValoracion", LabeledMatchers.hasText("E_ditar"));
        verifyThat("#btnEliminarValoracion", LabeledMatchers.hasText("E_liminar"));
        verifyThat("#cmbPuntuacion", ComboBoxMatchers.hasItems(5));
        verifyThat("#cmbTarifa", ComboBoxMatchers.hasItems(2));
        verifyThat("#tablaValoraciones", TableViewMatchers.hasNumRows(1));
        verifyThat("#tablaValoraciones", TableViewMatchers.hasTableCell("Va muy bien"));
    }

    /**
     * Comprueba que al valorar sin elegir tarifa aparezca un aviso.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarValorarSinTarifa() throws Exception {
        clickOn("#btnValorar");
        DialogPane dialog = esperarDialogo();
        assertEquals("Elige la tarifa que quieres valorar.", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.OK);
    }

    /**
     * Comprueba que al valorar sin puntuación ni opinión aparezca un aviso.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarValorarSinPuntuacion() throws Exception {
        seleccionarTarifa("Tarifa Prueba Fibra");
        clickOn("#btnValorar");
        DialogPane dialog = esperarDialogo();
        assertEquals("Elige una puntuación y escribe tu opinión.", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.OK);
    }

    /**
     * Comprueba que al valorar sin escribir opinión aparezca un aviso.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarValorarSinOpinion() throws Exception {
        seleccionarTarifa("Tarifa Prueba Fibra");
        seleccionarPuntuacion(5);
        clickOn("#btnValorar");
        DialogPane dialog = esperarDialogo();
        assertEquals("Elige una puntuación y escribe tu opinión.", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.OK);
    }

    /**
     * Comprueba que con todo relleno se pida confirmación y que al cancelar no se publique.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarValorarCancelar() throws Exception {
        seleccionarTarifa("Tarifa Prueba Movil");
        seleccionarPuntuacion(3);
        clickOn("#txtValoracion").write("Opinion de prueba");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnValorar");
        DialogPane dialog = esperarDialogo();
        assertEquals("¿Seguro que deseas publicar esta valoración?", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.CANCEL);
        verifyThat("#tablaValoraciones", TableViewMatchers.hasNumRows(1));
    }

    /**
     * Comprueba que al pulsar una valoración de la tabla se carguen sus datos en el formulario.
     */
    @Test
    public void comprobarSeleccionarValoracion() {
        clickOn("Va muy bien");
        verifyThat("#txtValoracion", TextInputControlMatchers.hasText("Va muy bien"));
        verifyThat("#cmbPuntuacion", ComboBoxMatchers.hasSelectedItem(4));
    }

    /**
     * Comprueba que al editar sin elegir valoración aparezca un aviso.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarEditarSinSeleccion() throws Exception {
        clickOn("#btnEditarValoracion");
        DialogPane dialog = esperarDialogo();
        assertEquals("Selecciona una valoración de la tabla primero.", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.OK);
    }

    /**
     * Comprueba que al editar se pida confirmación y que al cancelar no se cambie.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarEditarCancelar() throws Exception {
        clickOn("Va muy bien");
        clickOn("#btnEditarValoracion");
        DialogPane dialog = esperarDialogo();
        assertEquals("¿Seguro que deseas guardar los cambios de tu valoración?", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.CANCEL);
        verifyThat("#tablaValoraciones", TableViewMatchers.hasTableCell("Va muy bien"));
    }

    /**
     * Comprueba que al eliminar sin elegir valoración aparezca un aviso.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarEliminarSinSeleccion() throws Exception {
        clickOn("#btnEliminarValoracion");
        DialogPane dialog = esperarDialogo();
        assertEquals("Selecciona una valoración de la tabla primero.", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.OK);
    }

    /**
     * Comprueba que al eliminar se pida confirmación y que al cancelar la valoración siga.
     *
     * @throws Exception Si el diálogo no aparece.
     */
    @Test
    public void comprobarEliminarCancelar() throws Exception {
        clickOn("Va muy bien");
        clickOn("#btnEliminarValoracion");
        DialogPane dialog = esperarDialogo();
        assertEquals("¿Seguro que deseas eliminar esta valoración?", dialog.getContentText());
        pulsarBotonDialogo(dialog, ButtonType.CANCEL);
        verifyThat("#tablaValoraciones", TableViewMatchers.hasNumRows(1));
    }
}
