/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.util.logging.Logger;
import exception.ShowAlert;
import exception.OurException;
import com.mongodb.client.MongoDatabase;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Valoracion;
import DAO.ValoracionDAO;
import DAO.implementacion.ValoracionDAOImplementacion;

/**
 * Controlador de la vista de administración y consulta de valoraciones en JavaFX.
 * Permite visualizar todas las opiniones registradas por los clientes, filtrarlas
 * por puntuación (de 1 a 5 estrellas), consultar la valoración media global y
 * eliminar valoraciones concretas previa confirmación.
 *
 * @author eneko
 */
public class ValoracionController {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(ValoracionController.class.getName());

    /** Selector desplegable para filtrar las valoraciones según su puntuación. */
    @FXML
    private ComboBox<String> cbPuntuacion;

    /** Tabla principal donde se muestran las valoraciones registradas en el sistema. */
    @FXML
    private TableView<Valoracion> tabla;

    /** Columna de la tabla que muestra el nombre del cliente autor de la valoración. */
    @FXML
    private TableColumn<Valoracion, String> colCliente;

    /** Columna de la tabla que muestra el nombre de la tarifa valorada. */
    @FXML
    private TableColumn<Valoracion, String> colTarifa;

    /** Columna de la tabla que muestra la puntuación numérica otorgada (1 a 5). */
    @FXML
    private TableColumn<Valoracion, Integer> colPuntuacion;

    /** Columna de la tabla que muestra el comentario textual de la valoración. */
    @FXML
    private TableColumn<Valoracion, String> colTexto;

    /** Etiqueta de cabecera que muestra la puntuación media global de todas las valoraciones. */
    @FXML
    private Label lblMedia;

    /** Objeto de acceso a datos para listar, calcular la media y eliminar valoraciones en MongoDB. */
    private final ValoracionDAO valoracionDAO = new ValoracionDAOImplementacion();

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Configura las factorías de celdas de las columnas de {@code tabla}, rellena
     * el desplegable de puntuaciones ({@code "Todas"} y valores del 1 al 5) y carga
     * los datos iniciales junto con la media global.
     */
    @FXML
    public void initialize() {
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colTarifa.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colPuntuacion.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colTexto.setCellValueFactory(new PropertyValueFactory<>("texto"));

        cbPuntuacion.getItems().addAll("Todas", "1", "2", "3", "4", "5");
        cbPuntuacion.setValue("Todas");

        cargarTabla();
    }

    /**
     * Recupera las valoraciones desde MongoDB mediante {@link ValoracionDAO#listarValoraciones()},
     * aplica el filtro de puntuación seleccionado en {@code cbPuntuacion} sobre {@code tabla}
     * y actualiza la etiqueta {@code lblMedia} con la puntuación media global formateada a dos decimales.
     */
    private void cargarTabla() {
        try {
            String filtro = cbPuntuacion.getValue();

            ObservableList<Valoracion> listaCompleta = FXCollections.observableArrayList(valoracionDAO.listarValoraciones());

            if (filtro == null || filtro.equals("Todas")) {
                tabla.setItems(listaCompleta);
            } else {
                int puntuacion = Integer.parseInt(filtro);
                ObservableList<Valoracion> listaFiltrada = FXCollections.observableArrayList();

                for (int i = 0; i < listaCompleta.size(); i++) {
                    Valoracion v = listaCompleta.get(i);
                    if (v.getPuntuacion() == puntuacion) {
                        listaFiltrada.add(v);
                    }
                }

                tabla.setItems(listaFiltrada);
            }

            lblMedia.setText("Media: " + String.format("%.2f", valoracionDAO.mediaValoraciones()) + " / 5");
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Manejador de eventos del selector de puntuación que actualiza el listado de la tabla
     * aplicando el criterio seleccionado en {@code cbPuntuacion}.
     */
    @FXML
    private void btnFiltro() {
        cargarTabla();
    }

    /**
     * Manejador del botón para eliminar la valoración seleccionada en {@code tabla}.
     * Verifica que exista una fila marcada, solicita confirmación mediante un cuadro
     * de diálogo y ejecuta {@link ValoracionDAO#eliminarValoracion(Valoracion)}, recargando
     * posteriormente la tabla y la media.
     */
    @FXML
    private void btnEliminar() {
        try {
            Valoracion seleccionada = tabla.getSelectionModel().getSelectedItem();

            if (seleccionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecciona una valoracion de la tabla para eliminarla");
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Eliminar valoración");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Seguro que quieres eliminar la valoracion de " + seleccionada.getNombreCliente() + "sobre la tarifa " + seleccionada.getNombreTarifa() + "?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (valoracionDAO.eliminarValoracion(seleccionada)) {
                    cargarTabla();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se ha podido eliminar la valoración.");
                }
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Muestra un cuadro de diálogo modal de tipo {@link Alert} sin encabezado con la
     * severidad y el mensaje especificados.
     *
     * @param tipo    Tipo de alerta JavaFX ({@link Alert.AlertType}).
     * @param mensaje Contenido textual que se mostrará al usuario.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}