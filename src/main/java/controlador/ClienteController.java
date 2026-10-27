package controlador;

import dao.MongoConnection;
import dao.DAOImplementacion;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.bson.Document;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import dao.DAO;

public class ClienteController implements Initializable {

    private static Document usuarioActual;

    public static void setUsuario(Document u) {
        usuarioActual = u;
    }

    private static final String OP_TARIFAS = "Mis Tarifas";
    private static final String OP_CONTRATOS = "Mis Contratos";
    private static final String OP_SOPORTE = "Soporte";
    private static final String OP_CERRAR = "Cerrar Sesion";

    @FXML private Label lblBienvenida;
    @FXML private ComboBox<String> cmbOpciones;

    @FXML private AnchorPane panelPerfil;
    @FXML private AnchorPane panelTarifas;
    @FXML private AnchorPane panelContratos;
    @FXML private AnchorPane panelSoporte;

    // Perfil
    @FXML private TextField txtNombre;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtDni;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCuentaBancaria;
    
    // Soporte
    @FXML private TextField txtSoporteAsunto;
    @FXML private TextField txtSoporteMensaje;

    // Tablas
    @FXML private TableView<Document> tablaTarifas;
    @FXML private TableColumn<Document, String> colTarifaNombre;
    @FXML private TableColumn<Document, String> colTarifaPrecio;
    @FXML private TableColumn<Document, String> colTarifaDetalles;
    @FXML private TableColumn<Document, String> colTarifaTipo;

    @FXML private TableView<Document> tablaContratos;
    @FXML private TableColumn<Document, String> colContratoTelefono;
    @FXML private TableColumn<Document, String> colContratoEstado;
    @FXML private TableColumn<Document, String> colContratoFecha;
    @FXML private TableColumn<Document, String> colContratoIdTarifa;

    @FXML private TableView<Document> tablaSoporte;
    @FXML private TableColumn<Document, String> colSoporteAsunto;
    @FXML private TableColumn<Document, String> colSoporteFecha;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (cmbOpciones != null) {
            cmbOpciones.getItems().addAll(OP_TARIFAS, OP_CONTRATOS, OP_SOPORTE, OP_CERRAR);
        }

        if (usuarioActual != null) {
            String nombre = usuarioActual.getString("nombre");
            if (nombre == null) {
                nombre = usuarioActual.getString("nombreUsuario");
            }
            if (lblBienvenida != null) {
                lblBienvenida.setText("Bienvenido/a " + (nombre != null ? nombre : ""));
            }
        }

        configurarColumnasSinLambdas();
        ocultarTodo();
        if (panelPerfil != null) panelPerfil.setVisible(true);
        cargarDatosPerfil();
    }

    private void configurarColumnasSinLambdas() {
        if(colTarifaNombre != null) {
            colTarifaNombre.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("nombreTarifa")));
                }
            });
        }
        if(colTarifaPrecio != null) {
            colTarifaPrecio.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("precio")));
                }
            });
        }
        if(colTarifaDetalles != null) {
            colTarifaDetalles.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("detalles")));
                }
            });
        }
        if(colTarifaTipo != null) {
            colTarifaTipo.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("tipotarifa")));
                }
            });
        }

        if(colContratoTelefono != null) {
            colContratoTelefono.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("telefono")));
                }
            });
        }
        if(colContratoEstado != null) {
            colContratoEstado.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("estado")));
                }
            });
        }
        if(colContratoFecha != null) {
            colContratoFecha.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("fechaFinalizacion")));
                }
            });
        }
        if(colContratoIdTarifa != null) {
            colContratoIdTarifa.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("idTarifa")));
                }
            });
        }

        if(colSoporteAsunto != null) {
            colSoporteAsunto.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("asunto")));
                }
            });
        }
        if(colSoporteFecha != null) {
            colSoporteFecha.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Document, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Document, String> param) {
                    return new SimpleStringProperty(String.valueOf(param.getValue().get("fechaCreacion")));
                }
            });
        }
    }

    private void ocultarTodo() {
        if (panelPerfil != null) panelPerfil.setVisible(false);
        if (panelTarifas != null) panelTarifas.setVisible(false);
        if (panelContratos != null) panelContratos.setVisible(false);
        if (panelSoporte != null) panelSoporte.setVisible(false);
    }

    private void cargarDatosPerfil() {
        if (usuarioActual != null) {
            if (txtNombre != null) txtNombre.setText(usuarioActual.getString("nombre") != null ? usuarioActual.getString("nombre") : "");
            if (txtNombreUsuario != null) txtNombreUsuario.setText(usuarioActual.getString("nombreUsuario") != null ? usuarioActual.getString("nombreUsuario") : "");
            if (txtDni != null) txtDni.setText(usuarioActual.getString("dni") != null ? usuarioActual.getString("dni") : "");
            if (txtCorreo != null) txtCorreo.setText(usuarioActual.getString("correo") != null ? usuarioActual.getString("correo") : "");
            if (txtDireccion != null) txtDireccion.setText(usuarioActual.getString("direccion") != null ? usuarioActual.getString("direccion") : "");
            
            Object tel = usuarioActual.get("telefono");
            if (txtTelefono != null) txtTelefono.setText(tel != null ? String.valueOf(tel) : "");
            
            Object cuenta = usuarioActual.get("cuentaBancaria");
            if (txtCuentaBancaria != null) txtCuentaBancaria.setText(cuenta != null ? String.valueOf(cuenta) : "");
        }
    }

    private void cargarTarifasDesdeBD() {
        ObservableList<Document> listaTarifas = FXCollections.observableArrayList();
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("Tarifas");
            try (MongoCursor<Document> cursor = col.find().iterator()) {
                while (cursor.hasNext()) {
                    listaTarifas.add(cursor.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tablaTarifas != null) tablaTarifas.setItems(listaTarifas);
    }

    private void cargarContratosDesdeBD() {
        ObservableList<Document> listaContratos = FXCollections.observableArrayList();
        if (usuarioActual != null) {
            try {
                MongoDatabase db = MongoConnection.getDatabase();
                MongoCollection<Document> col = db.getCollection("Contratos");
                try (MongoCursor<Document> cursor = col.find(Filters.eq("idCliente", usuarioActual.getObjectId("_id"))).iterator()) {
                    while (cursor.hasNext()) {
                        listaContratos.add(cursor.next());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tablaContratos != null) tablaContratos.setItems(listaContratos);
    }

    private void cargarTicketsDesdeBD() {
        ObservableList<Document> listaTickets = FXCollections.observableArrayList();
        if (usuarioActual != null) {
            try {
                MongoDatabase db = MongoConnection.getDatabase();
                MongoCollection<Document> col = db.getCollection("TicketsSoportes");
                try (MongoCursor<Document> cursor = col.find(Filters.eq("idCliente", usuarioActual.getObjectId("_id"))).iterator()) {
                    while (cursor.hasNext()) {
                        listaTickets.add(cursor.next());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tablaSoporte != null) tablaSoporte.setItems(listaTickets);
    }

    @FXML
    private void btnMiPerfil(ActionEvent event) {
        ocultarTodo();
        if (panelPerfil != null) panelPerfil.setVisible(true);
        cargarDatosPerfil();
        if (cmbOpciones != null) cmbOpciones.getSelectionModel().clearSelection();
    }

    @FXML
    public void cmbOpcionSeleccionada(ActionEvent event) {
        String seleccion = cmbOpciones.getValue();
        if (seleccion != null) {
            ocultarTodo();
            switch (seleccion) {
                case OP_TARIFAS:
                    if (panelTarifas != null) panelTarifas.setVisible(true);
                    cargarTarifasDesdeBD();
                    break;
                case OP_CONTRATOS:
                    if (panelContratos != null) panelContratos.setVisible(true);
                    cargarContratosDesdeBD();
                    break;
                case OP_SOPORTE:
                    if (panelSoporte != null) panelSoporte.setVisible(true);
                    cargarTicketsDesdeBD();
                    break;
                case OP_CERRAR:
                    cerrarSesion(event);
                    break;
            }
            cmbOpciones.getSelectionModel().clearSelection();
        }
    }

    private void cerrarSesion(ActionEvent event) {
        if (mostrarConfirmacion("Cerrar sesión", "¿Seguro que deseas cerrar sesión?")) {
            usuarioActual = null;
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void btnModificar(ActionEvent event) {
        if (usuarioActual == null) {
            mostrarInfo("Error", "No hay un usuario en sesión.");
            return;
        }

        if (mostrarConfirmacion("Confirmar cambios", "¿Seguro que deseas guardar los cambios?")) {
            try {
                usuarioActual.put("nombre", txtNombre.getText().trim());
                usuarioActual.put("nombreUsuario", txtNombreUsuario.getText().trim());
                usuarioActual.put("dni", txtDni.getText().trim());
                usuarioActual.put("correo", txtCorreo.getText().trim());
                usuarioActual.put("direccion", txtDireccion.getText().trim());
                usuarioActual.put("telefono", txtTelefono.getText().trim());
                usuarioActual.put("cuentaBancaria", txtCuentaBancaria.getText().trim());

                DAO dao = new DAOImplementacion();
                boolean actualizado = dao.actualizarCliente(usuarioActual);

                if (actualizado) {
                    mostrarInfo("Modificación exitosa", "Los datos se han actualizado correctamente en la base de datos.");
                } else {
                    mostrarInfo("Error", "No se pudo actualizar en la base de datos.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarInfo("Error", "Ocurrió un error al actualizar.");
            }
        }
    }

    @FXML 
    public void btnCambiarTarifa(ActionEvent event) {
        if (usuarioActual == null) {
            mostrarInfo("Error", "No hay un usuario en sesión.");
            return;
        }

        Document tarifaSel = tablaTarifas.getSelectionModel().getSelectedItem();
        
        if (tarifaSel == null) {
            mostrarInfo("Aviso", "Por favor, selecciona una tarifa de la tabla primero.");
            return;
        }

        String nombreTarifa = tarifaSel.getString("nombreTarifa");

        if (mostrarConfirmacion("Confirmar contratación", "¿Seguro que deseas contratar la tarifa: " + nombreTarifa + "?")) {
            try {
                org.bson.types.ObjectId idTarifa = tarifaSel.getObjectId("_id");
                org.bson.types.ObjectId idCliente = usuarioActual.getObjectId("_id");
                String telefonoCliente = String.valueOf(usuarioActual.get("telefono"));

                DAO dao = new DAOImplementacion();
                boolean exito = dao.insertarContrato(telefonoCliente, idTarifa, idCliente);

                if (exito) {
                    mostrarInfo("Éxito", "La tarifa " + nombreTarifa + " se ha añadido a tu cuenta correctamente.");
                    cargarContratosDesdeBD(); 
                } else {
                    mostrarInfo("Error", "No se pudo añadir la tarifa en la base de datos.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarInfo("Error", "Ocurrió un error al procesar la contratación.");
            }
        }
    }

    @FXML public void btnEliminarTarifa(ActionEvent event) {
        if (mostrarConfirmacion("Confirmar eliminación", "¿Seguro que deseas eliminar esta tarifa?")) {
            mostrarInfo("Tarifa eliminada", "La tarifa ha sido eliminada.");
        }
    }

    @FXML public void btnFirmarContrato(ActionEvent event) {
        if (mostrarConfirmacion("Confirmar firma", "¿Seguro que deseas firmar este contrato?")) {
            mostrarInfo("Contrato firmado", "El contrato ha sido firmado y activado.");
        }
    }

    @FXML public void btnRescindirContrato(ActionEvent event) {
        if (mostrarConfirmacion("Confirmar rescisión", "¿Seguro que deseas rescindir este contrato?")) {
            mostrarInfo("Solicitud enviada", "La solicitud de baja ha sido enviada.");
        }
    }

    @FXML 
    public void btnEnviarTicket(ActionEvent event) {
        if (usuarioActual == null) {
            mostrarInfo("Error", "No hay un usuario en sesión.");
            return;
        }

        String asunto = txtSoporteAsunto.getText().trim();
        String descripcion = txtSoporteMensaje.getText().trim();

        if(asunto.isEmpty() || descripcion.isEmpty()) {
            mostrarInfo("Aviso", "Rellena el asunto y la descripción del ticket.");
            return;
        }

        if (mostrarConfirmacion("Confirmar envío", "¿Seguro que deseas enviar este ticket?")) {
            try {
                org.bson.types.ObjectId idCliente = usuarioActual.getObjectId("_id");
                DAO dao = new DAOImplementacion();
                boolean exito = dao.insertarTicket(asunto, descripcion, idCliente);
                
                if(exito) {
                    mostrarInfo("Ticket enviado", "Tu ticket de soporte ha sido guardado.");
                    txtSoporteAsunto.setText("");
                    txtSoporteMensaje.setText("");
                    cargarTicketsDesdeBD();
                } else {
                    mostrarInfo("Error", "No se pudo guardar el ticket.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarInfo("Error", "Ocurrió un error al procesar el ticket.");
            }
        }
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Optional<ButtonType> respuesta = alert.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}