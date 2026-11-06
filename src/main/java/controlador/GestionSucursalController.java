package controlador;

import com.mycompany.reto_1.Reto_1;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import modeloadmin.Sucursal;
import modeloadmin.Ubicacion;
import daoadmin.SucursalDAO;
import daoadmin.implementacion.SucursalDaoImplementacion;

/**
 * Controlador de Gestión de Sucursales.
 *
 * @author Eneko.Plaza
 */
public class GestionSucursalController implements Initializable {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtHorario;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtUbicacion;
    @FXML
    private TextField txtCodigoPostal;
    @FXML
    private ListView<Sucursal> listSucursales;

    private final ObservableList<Sucursal> listaSucursalesData = FXCollections.observableArrayList();
    private final SucursalDAO sucursalDAO = new SucursalDaoImplementacion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Asignar lista a la interfaz
        listSucursales.setItems(listaSucursalesData);

        // Formato visual de los elementos en la lista
        listSucursales.setCellFactory(param -> new ListCell<Sucursal>() {
            @Override
            protected void updateItem(Sucursal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getHorario() + " - " + item.getTelefono());
                }
            }
        });

        // Evento al seleccionar un elemento de la lista
        listSucursales.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null) {
                cargarCampos(nuevo);
            }
        });

        // Cargar datos al arrancar
        cargarSucursalesBD();
    }

    // Abre la sucursal seleccionada en Google Maps, en el navegador del ordenador
    @FXML
    public void verEnMapa(ActionEvent event) {
        Sucursal seleccionada = listSucursales.getSelectionModel().getSelectedItem();
        if (seleccionada == null || seleccionada.getUbicacion() == null) {
            mostrarAlerta("Atención", "Selecciona una sucursal de la lista primero.", Alert.AlertType.WARNING);
            return;
        }
        Ubicacion u = seleccionada.getUbicacion();
        String direccion = u.getDireccion() + ", " + u.getCodigoPostal() + " " + u.getLocalidad() + ", España";
        Reto_1.abrirEnNavegador("https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(direccion, StandardCharsets.UTF_8));
    }

    // Recupera la lista desde la BBDD y actualiza la GUI
    private void cargarSucursalesBD() {
        listaSucursalesData.clear();
        List<Sucursal> sucursales = sucursalDAO.listarSucursales();
        listaSucursalesData.addAll(sucursales);
    }

    // Rellena los campos del formulario
    private void cargarCampos(Sucursal sucursal) {
        txtId.setText(String.valueOf(sucursal.getId()));
        txtHorario.setText(sucursal.getHorario());
        txtTelefono.setText(String.valueOf(sucursal.getTelefono()));
        if (sucursal.getUbicacion() != null) {
            txtUbicacion.setText(sucursal.getUbicacion().getDireccion());
            txtCodigoPostal.setText(String.valueOf(sucursal.getUbicacion().getCodigoPostal()));
        } else {
            txtUbicacion.setText("");
            txtCodigoPostal.setText("");
        }

    }

    @FXML
    public void añadirSucursal(ActionEvent event) {
        String idTexto = txtId.getText().trim();
        String horario = txtHorario.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtUbicacion.getText().trim();

        if (idTexto.isEmpty() || horario.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
            mostrarAlerta("Atención", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int id = Integer.parseInt(idTexto);

            // Objeto embebido Ubicacion con datos por defecto si solo meten la dirección
            Ubicacion ubicacion = new Ubicacion(direccion, "Bilbao", 48480);
            Sucursal nueva = new Sucursal(id, horario, Integer.parseInt(telefono.trim()), ubicacion);

            if (sucursalDAO.insertarSucursal(nueva)) {
                cargarSucursalesBD();
                limpiar();
                mostrarAlerta("Éxito", "Sucursal guardada correctamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo guardar la sucursal.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El ID debe ser un número entero.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void gestionarSucursal(ActionEvent event) {
        Sucursal seleccionada = listSucursales.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una sucursal de la lista.", Alert.AlertType.WARNING);
            return;
        }

        seleccionada.setHorario(txtHorario.getText().trim());
        seleccionada.setTelefono(Integer.parseInt(txtTelefono.getText().trim()));
        if (seleccionada.getUbicacion() != null) {
            seleccionada.getUbicacion().setDireccion(txtUbicacion.getText().trim());
            try {
                int codigoPostal = Integer.parseInt(txtCodigoPostal.getText().trim());
                seleccionada.getUbicacion().setCodigoPostal(codigoPostal);
            } catch (NumberFormatException e) {
                // Manejo de error: el usuario ingresó letras o caracteres inválidos
                System.out.println("Por favor, introduce un código postal válido (solo números).");
            }
        }

        if (sucursalDAO.actualizarSucursal(seleccionada)) {
            cargarSucursalesBD();
            limpiar();
            mostrarAlerta("Éxito", "Sucursal actualizada correctamente.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se pudo actualizar la sucursal.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void eliminarSucursal(ActionEvent event) {
        Sucursal seleccionada = listSucursales.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una sucursal para borrar.", Alert.AlertType.WARNING);
            return;
        }

        if (sucursalDAO.eliminarSucursal(seleccionada.getId())) {
            cargarSucursalesBD();
            limpiar();
            mostrarAlerta("Éxito", "Sucursal eliminada.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se pudo eliminar la sucursal.", Alert.AlertType.ERROR);
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtHorario.setText("");
        txtTelefono.setText("");
        txtUbicacion.setText("");

        listSucursales.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String texto, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}
