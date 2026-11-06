/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

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
import daoadmin.ValoracionDAO;
import daoadmin.implementacion.ValoracionDAOImpl;

/**
 *
 * @author eneko
 */
public class ValoracionController {

    @FXML
    private ComboBox<String> cbPuntuacion;
    @FXML
    private TableView<Valoracion> tabla;
    @FXML
    private TableColumn<Valoracion, String> colCliente;
    @FXML
    private TableColumn<Valoracion, String> colTarifa;
    @FXML
    private TableColumn<Valoracion, Integer> colPuntuacion;
    @FXML
    private TableColumn<Valoracion, String> colTexto;
    @FXML
    private Label lblMedia;
    private final ValoracionDAO valoracionDAO = new ValoracionDAOImpl();

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

    private void cargarTabla() {
        String filtro = cbPuntuacion.getValue();
        ObservableList<Valoracion> lista;

        if (filtro.equals("Todas")) {
            lista = FXCollections.observableArrayList(valoracionDAO.listarValoraciones());
        } else {
            int puntuacion = Integer.parseInt(filtro);
            lista = FXCollections.observableArrayList(valoracionDAO.listarValoracionesPorPuntuacion(puntuacion));
        }

        tabla.setItems(lista);
        lblMedia.setText("Media: " + String.format("%.2f", valoracionDAO.mediaValoraciones()) + " / 5");
    }

    @FXML
    private void btnFiltro() {
        cargarTabla();
    }

    @FXML
    private void btnEliminar() {
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
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
