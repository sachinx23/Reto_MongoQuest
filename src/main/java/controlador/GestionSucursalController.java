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

/**
 * Controlador de la vista de gestión y consulta de sucursales en JavaFX.
 * Administra el listado de sucursales mediante un {@link ListView}, permite realizar
 * operaciones CRUD (alta, modificación y eliminación) sobre las mismas e integra un
 * mapa interactivo en un {@link WebView} para visualizar y enfocar sus ubicaciones.
 *
 * @author Eneko.Plaza
 */
public class GestionSucursalController implements Initializable {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(GestionSucursalController.class.getName());

    /** Campo de texto para el nombre identificador de la sucursal. */
    @FXML
    private TextField txtNombre;

    /** Campo de texto para el horario de atención al público de la sucursal. */
    @FXML
    private TextField txtHorario;

    /** Campo de texto para el número de teléfono de contacto de la sucursal. */
    @FXML
    private TextField txtTelefono;

    /** Campo de texto para la dirección física de la sucursal. */
    @FXML
    private TextField txtUbicacion;

    /** Campo de texto para el código postal de la ubicación. */
    @FXML
    private TextField txtCodigoPostal;

    /** Componente visual de lista que muestra las sucursales registradas. */
    @FXML
    private ListView<Sucursal> listSucursales;

    /** Componente web embebido donde se renderiza el mapa interactivo ({@code mapa.html}). */
    @FXML
    private WebView webViewMapa;

    /** Lista observable que respalda los elementos mostrados en {@code listSucursales}. */
    private final ObservableList<Sucursal> listaSucursalesData = FXCollections.observableArrayList();

    /** Motor web asociado a {@code webViewMapa} para ejecutar funciones JavaScript del mapa. */
    private WebEngine engine;

    /** Objeto de acceso a datos para gestionar la persistencia de las sucursales en MongoDB. */
    private final SucursalDAO sucursalDAO = new SucursalDAOImplementacion();

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Configura la celda personalizada y el listener de selección de {@code listSucursales},
     * inicializa el motor del mapa HTML y carga las sucursales desde la base de datos.
     *
     * @param url Ubicación utilizada para resolver rutas relativas del objeto raíz, o {@code null} si no se conoce.
     * @param rb  Recursos utilizados para localizar el objeto raíz, o {@code null} si no se localizó.
     */
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

    /**
     * Carga el recurso local {@code /html/mapa.html} en el {@link WebEngine} del {@link WebView}
     * y registra un listener que refresca el mapa y dibuja todos los marcadores de sucursales
     * una vez que la página ha terminado de cargarse ({@link Worker.State#SUCCEEDED}).
     */
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

    /**
     * Limpia los marcadores existentes en el mapa web e invoca la función JavaScript
     * {@code agregarSucursal} para posicionar cada sucursal de {@code listaSucursalesData}.
     */
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

    /**
     * Centra y enfoca el mapa web en la dirección y localidad de una ubicación concreta
     * ejecutando la función JavaScript {@code enfocarUbicacion}.
     *
     * @param ubicacion Objeto {@link Ubicacion} hacia el cual se desplazará la vista del mapa.
     */
    private void actualizarMapa(Ubicacion ubicacion) {
        if (engine != null) {
            if (ubicacion != null) {
                String dir = ubicacion.getDireccion();
                String loc = ubicacion.getLocalidad();
                engine.executeScript("enfocarUbicacion('" + dir + "', '" + loc + "');");
            }
        }
    }

    /**
     * Recupera el listado actualizado de sucursales desde MongoDB, actualiza la lista
     * observable de la interfaz y vuelve a pintar todos los marcadores en el mapa.
     */
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

    /**
     * Rellena los campos de texto del formulario con los datos de la sucursal seleccionada.
     *
     * @param sucursal Objeto {@link Sucursal} cuyos atributos se mostrarán en los controles.
     */
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

    /**
     * Manejador del botón para registrar una nueva sucursal.
     * Valida que los campos obligatorios no estén vacíos, construye el objeto {@link Sucursal}
     * con ubicación por defecto en Bilbao (CP 48480), lo persiste mediante
     * {@link SucursalDAO#insertarSucursal(Sucursal)} y refresca la vista.
     *
     * @param event Evento de acción disparado al pulsar el botón de añadir sucursal.
     */
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

    /**
     * Manejador del botón para modificar los datos de la sucursal seleccionada en la lista.
     * Actualiza el horario, teléfono, dirección y código postal del objeto {@link Sucursal}
     * seleccionado y guarda los cambios mediante {@link SucursalDAO#actualizarSucursal(Sucursal)}.
     *
     * @param event Evento de acción disparado al pulsar el botón de actualizar/gestionar sucursal.
     */
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

    /**
     * Manejador del botón para eliminar la sucursal seleccionada en {@code listSucursales}
     * a través de {@link SucursalDAO#eliminarSucursal(String)}, recargando el listado y el mapa.
     *
     * @param event Evento de acción disparado al pulsar el botón de eliminar sucursal.
     */
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

    /**
     * Limpia el contenido de los campos de texto del formulario y deselecciona cualquier
     * elemento marcado en {@code listSucursales}.
     */
    private void limpiar() {
        txtNombre.setText("");
        txtHorario.setText("");
        txtTelefono.setText("");
        txtUbicacion.setText("");

        listSucursales.getSelectionModel().clearSelection();
    }

    /**
     * Muestra un cuadro de diálogo modal de tipo {@link Alert} con el título, mensaje y severidad indicados.
     *
     * @param titulo Título de la ventana de alerta.
     * @param texto  Mensaje descriptivo que se mostrará al usuario.
     * @param tipo   Tipo de alerta JavaFX ({@link Alert.AlertType}).
     */
    private void mostrarAlerta(String titulo, String texto, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}