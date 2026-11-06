package controlador;

import daocliente.ContratoDAO;
import daocliente.TarifaDAO;
import daocliente.implementacion.ContratoDAOImplementacion;
import daocliente.implementacion.TarifaDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.time.LocalDate;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;
import modelo.Tarifa;

/**
 * Mis Tarifas: contratar una tarifa o borrar la petición.
 */
public class ClienteTarifasController {

    private static final Logger LOGGER = Logger.getLogger(ClienteTarifasController.class.getName());

    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();
    private Cliente cliente;

    @FXML private TableView<Tarifa> tablaTarifas;
    @FXML private TableColumn<Tarifa, String> colTarifaNombre;
    @FXML private TableColumn<Tarifa, String> colTarifaPrecio;
    @FXML private TableColumn<Tarifa, String> colTarifaDetalles;
    @FXML private TableColumn<Tarifa, String> colTarifaTipo;

    @FXML
    public void initialize() {
        cliente = (Cliente) Sesion.getUsuario();
        colTarifaNombre.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colTarifaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTexto"));
        colTarifaDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));
        colTarifaTipo.setCellValueFactory(new PropertyValueFactory<>("tipoTarifa"));
        cargarTarifas();
    }

    private void cargarTarifas() {
        try {
            tablaTarifas.setItems(FXCollections.observableArrayList(tarifaDAO.obtenerTarifas()));
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnCambiarTarifa(ActionEvent event) {
        Tarifa tarifa = tablaTarifas.getSelectionModel().getSelectedItem();
        if (tarifa == null) {
            ShowAlert.info("Aviso", "Por favor, selecciona una tarifa de la tabla primero.");
            return;
        }
        try {
            // No se puede pedir otra vez una tarifa que ya está pendiente o activa
            for (Contrato c : contratoDAO.obtenerContratosCliente(cliente.getId())) {
                boolean mismaTarifa = tarifa.getId().equals(c.getIdTarifa());
                boolean enVigor = c.getEstado() == EstadoContrato.PENDIENTE
                        || c.getEstado() == EstadoContrato.ACTIVO;
                if (mismaTarifa && enVigor) {
                    ShowAlert.info("Aviso", "Ya tienes la tarifa " + tarifa.getNombreTarifa() + " pedida o contratada.");
                    return;
                }
            }
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
            return;
        }
        if (!ShowAlert.confirmar("Cambio de tarifa", "¿Estás seguro de que deseas contratar la tarifa " + tarifa.getNombreTarifa() + "?")) {
            return;
        }
        // Se guarda como petición pendiente: un empleado la valida en "Validar tarifas"
        LocalDate hoy = LocalDate.now();
        Contrato contrato = new Contrato(null, cliente.getTelefono(), EstadoContrato.PENDIENTE, cliente.getId(),
                tarifa.getId(), null, hoy.toString(), hoy.plusYears(1).toString());
        contrato.setNombreTarifa(tarifa.getNombreTarifa());
        contrato.setPrecio(tarifa.getPrecio());
        try {
            contratoDAO.insertarContrato(contrato);
            ShowAlert.info("Petición enviada", "Tu petición de la tarifa " + tarifa.getNombreTarifa()
                    + " está pendiente de validar por un empleado.");
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }

    @FXML
    public void btnEliminarTarifa(ActionEvent event) {
        Tarifa tarifa = tablaTarifas.getSelectionModel().getSelectedItem();
        if (tarifa == null) {
            ShowAlert.info("Aviso", "Por favor, selecciona una tarifa de la tabla primero.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar eliminación", "¿Seguro que deseas eliminar tu petición de la tarifa " + tarifa.getNombreTarifa() + "?")) {
            return;
        }
        try {
            // Solo se puede eliminar una petición que todavía no ha validado ningún empleado
            boolean borrada = contratoDAO.borrarPeticion(cliente.getId(), tarifa.getId());
            if (borrada) {
                ShowAlert.info("Tarifa eliminada", "Tu petición de la tarifa " + tarifa.getNombreTarifa() + " ha sido eliminada.");
            } else {
                ShowAlert.info("Aviso", "No tienes ninguna petición pendiente de esa tarifa.");
            }
        } catch (OurException e) {
            ShowAlert.error(LOGGER, e);
        }
    }
}
