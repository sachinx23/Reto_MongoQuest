package controlador;

import java.util.logging.Logger;
import DAO.ContratoDAO;
import DAO.TarifaDAO;
import DAO.implementacion.ContratoDAOImplementacion;
import DAO.implementacion.TarifaDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Tarifa;

public class ClienteTarifasController {

    private static final Logger LOGGER = Logger.getLogger(ClienteTarifasController.class.getName());
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();
    private Cliente cliente;

    @FXML private TableView<Tarifa> tablaTarifas;
    @FXML private TableColumn<Tarifa, String> colTarifaNombre;
    @FXML private TableColumn<Tarifa, Double> colTarifaPrecio;
    @FXML private TableColumn<Tarifa, String> colTarifaDetalles;
    @FXML private TableColumn<Tarifa, String> colTarifaTipo;

    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        colTarifaNombre.setCellValueFactory(new PropertyValueFactory<>("nombreTarifa"));
        colTarifaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colTarifaDetalles.setCellValueFactory(new PropertyValueFactory<>("detalles"));
        colTarifaTipo.setCellValueFactory(new PropertyValueFactory<>("tipoTarifa"));
        cargarTarifas();
    }

    private void cargarTarifas() {
        try {
            tablaTarifas.setItems(FXCollections.observableArrayList(tarifaDAO.tarifasActivas()));
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
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
            // No repetir tarifa pedida
            for (Contrato c : contratoDAO.obtenerContratosCliente(cliente.getId())) {
                boolean mismaTarifa = tarifa.getId().equals(c.getIdTarifa());
                boolean enVigor = "pendiente".equals(c.getEstado())
                        || "pendiente de firma".equals(c.getEstado())
                        || "activo".equals(c.getEstado());
                if (mismaTarifa && enVigor) {
                    ShowAlert.info("Aviso", "Ya tienes la tarifa " + tarifa.getNombreTarifa() + " pedida o contratada.");
                    return;
                }
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
            return;
        }
        if (!ShowAlert.confirmar("Cambio de tarifa", "¿Estás seguro de que deseas contratar la tarifa " + tarifa.getNombreTarifa() + "?")) {
            return;
        }
        // Petición pendiente
        LocalDate hoy = LocalDate.now();
        Contrato contrato = new Contrato(null, cliente.getTelefono(), "pendiente", cliente.getId(),
                tarifa.getId(), null, hoy.toString(), hoy.plusYears(1).toString());
        try {
            contratoDAO.insertarContrato(contrato, tarifa);
            ShowAlert.info("Petición enviada", "Tu petición de la tarifa " + tarifa.getNombreTarifa()
                    + " está pendiente de validar por un empleado.");
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
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
            boolean borrada = contratoDAO.borrarPeticion(cliente.getId(), tarifa.getId());
            if (borrada) {
                ShowAlert.info("Tarifa eliminada", "Tu petición de la tarifa " + tarifa.getNombreTarifa() + " ha sido eliminada.");
            } else {
                ShowAlert.info("Aviso", "No tienes ninguna petición pendiente de esa tarifa.");
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }
}
