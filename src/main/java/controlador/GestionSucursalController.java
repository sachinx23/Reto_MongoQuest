package controlador;

import java.util.logging.Logger;
import exception.ShowAlert;
import exception.OurException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.concurrent.Worker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;
import modelo.Sucursal;
import modelo.Ubicacion;
import DAO.SucursalDAO;
import DAO.implementacion.SucursalDAOImplementacion;

public class GestionSucursalController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(GestionSucursalController.class.getName());

    @FXML
    private TextField txtNombre;
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
    @FXML
    private WebView webViewMapa;

    private final ObservableList<Sucursal> listaSucursalesData = FXCollections.observableArrayList();
    private WebEngine engine;
    private final SucursalDAO sucursalDAO = new SucursalDAOImplementacion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listSucursales.setItems(listaSucursalesData);

        listSucursales.setCellFactory(new Callback<ListView<Sucursal>, ListCell<Sucursal>>() {
            @Override
            public ListCell<Sucursal> call(ListView<Sucursal> param) {
                return new ListCell<Sucursal>() {
                    @Override
                    protected void updateItem(Sucursal item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            if (item == null) {
                                setText(null);
                            } else {
                                setText(item.getNombre() + " - " + item.getHorario() + " - " + item.getTelefono());
                            }
                        }
                    }
                };
            }
        });

        listSucursales.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sucursal>() {
            @Override
            public void changed(ObservableValue<? extends Sucursal> obs, Sucursal viejo, Sucursal nuevo) {
                if (nuevo != null) {
                    cargarCampos(nuevo);
                    actualizarMapa(nuevo.getUbicacion());
                }
            }
        });

        cargarMapa();
        cargarSucursalesBD();
    }

    private void cargarMapa() {
        engine = webViewMapa.getEngine();
        URL url = getClass().getResource("/html/mapa.html");

        if (url != null) {
            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> obs, Worker.State oldState, Worker.State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
                        engine.executeScript("refrescarMapa();");
                        mostrarTodasLasSucursalesEnMapa();
                    }
                }
            });
            engine.load(url.toExternalForm());
        }
    }

    private void mostrarTodasLasSucursalesEnMapa() {
        if (engine != null) {
            if (engine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                engine.executeScript("limpiarMapa();");
                for (int i = 0; i < listaSucursalesData.size(); i++) {
                    Sucursal s = listaSucursalesData.get(i);
                    if (s.getUbicacion() != null) {
                        String nomb = String.valueOf(s.getNombre());
                        String dir = s.getUbicacion().getDireccion();
                        String loc = s.getUbicacion().getLocalidad();
                        engine.executeScript("agregarSucursal('" + nomb + "', '" + dir + "', '" + loc + "');");
                    }
                }
            }
        }
    }

    private void actualizarMapa(Ubicacion ubicacion) {
        if (engine != null) {
            if (ubicacion != null) {
                String dir = ubicacion.getDireccion();
                String loc = ubicacion.getLocalidad();
                engine.executeScript("enfocarUbicacion('" + dir + "', '" + loc + "');");
            }
        }
    }

    private void cargarSucursalesBD() {
        try {
            listaSucursalesData.clear();
            List<Sucursal> sucursales = sucursalDAO.listarSucursales();
            listaSucursalesData.addAll(sucursales);

            mostrarTodasLasSucursalesEnMapa();
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private void cargarCampos(Sucursal sucursal) {
        txtNombre.setText(String.valueOf(sucursal.getNombre()));
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
        try {
            String nombre = txtNombre.getText().trim();
            String horario = txtHorario.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtUbicacion.getText().trim();

            if (nombre.isEmpty()) {
                mostrarAlerta("Atención", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
                return;
            }
            if (horario.isEmpty()) {
                mostrarAlerta("Atención", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
                return;
            }
            if (telefono.isEmpty()) {
                mostrarAlerta("Atención", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
                return;
            }
            if (direccion.isEmpty()) {
                mostrarAlerta("Atención", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
                return;
            }

            try {
                Ubicacion ubicacion = new Ubicacion(direccion, "Bilbao", 48480);
                Sucursal nueva = new Sucursal(nombre, horario, Integer.parseInt(telefono.trim()), ubicacion);

                if (sucursalDAO.insertarSucursal(nueva)) {
                    cargarSucursalesBD();
                    limpiar();
                    mostrarAlerta("Éxito", "Sucursal guardada correctamente.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "No se pudo guardar la sucursal.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El teléfono debe ser un número entero.", Alert.AlertType.ERROR);
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void gestionarSucursal(ActionEvent event) {
        try {
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
                    ShowAlert.error("Introduce un código postal válido (solo números).");
                }
            }

            if (sucursalDAO.actualizarSucursal(seleccionada)) {
                cargarSucursalesBD();
                limpiar();
                mostrarAlerta("Éxito", "Sucursal actualizada correctamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo actualizar la sucursal.", Alert.AlertType.ERROR);
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void eliminarSucursal(ActionEvent event) {
        try {
            Sucursal seleccionada = listSucursales.getSelectionModel().getSelectedItem();
            if (seleccionada == null) {
                mostrarAlerta("Atención", "Selecciona una sucursal para borrar.", Alert.AlertType.WARNING);
                return;
            }

            if (sucursalDAO.eliminarSucursal(seleccionada.getNombre())) {
                cargarSucursalesBD();
                limpiar();
                mostrarAlerta("Éxito", "Sucursal eliminada.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "No se pudo eliminar la sucursal.", Alert.AlertType.ERROR);
            }
    
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private void limpiar() {
        txtNombre.setText("");
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
