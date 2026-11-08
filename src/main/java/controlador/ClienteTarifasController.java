package controlador;

import java.util.logging.Logger;
import DAO.ContratoDAO;
import DAO.TarifaDAO;
import DAO.implementacion.ContratoDAOImplementacion;
import DAO.implementacion.TarifaDAOImplementacion;
import exception.OurException;
import exception.ShowAlert;
import java.time.LocalDate;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Tarifa;

public class ClienteTarifasController {

    private static final Logger LOGGER = Logger.getLogger(ClienteTarifasController.class.getName());
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();
    private Cliente cliente;

    @FXML private FlowPane contenedorTarifas;

    private Tarifa tarifaSeleccionada;
    private VBox tarjetaSeleccionadaNode;

    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        cargarTarifas();
    }

    private void cargarTarifas() {
        try {
            List<Tarifa> listaTarifas = tarifaDAO.tarifasActivas();
            mostrarTarifasEnTarjetas(listaTarifas);
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    private void mostrarTarifasEnTarjetas(List<Tarifa> listaTarifas) {
        contenedorTarifas.getChildren().clear();
        tarifaSeleccionada = null;
        tarjetaSeleccionadaNode = null;

        // Bucle for tradicional sin la sintaxis :
        for (int i = 0; i < listaTarifas.size(); i++) {
            Tarifa t = listaTarifas.get(i);
            VBox card = crearTarjetaTarifa(t);
            contenedorTarifas.getChildren().add(card);
        }
    }

    private VBox crearTarjetaTarifa(final Tarifa tarifa) {
        final VBox card = new VBox();
        card.getStyleClass().add("tarjeta-tarifa");

        // 1. Tipo de tarifa
        String textoTipo = "Tarifa";
        if (tarifa.getTipoTarifa() != null) {
            textoTipo = tarifa.getTipoTarifa();
        }
        Label lblTipo = new Label(textoTipo);
        lblTipo.getStyleClass().add("badge-tipo-tarifa");
        
        HBox topBox = new HBox();
        topBox.getChildren().add(lblTipo);
        topBox.setAlignment(Pos.TOP_LEFT);

        // 2. Nombre
        Label lblNombre = new Label(tarifa.getNombreTarifa());
        lblNombre.getStyleClass().add("titulo-tarifa-card");

        // 3. Precio
        Label lblPrecio = new Label(tarifa.getPrecio() + " €");
        lblPrecio.getStyleClass().add("precio-tarifa-card");
        
        Label lblPeriodo = new Label("/ mes");
        lblPeriodo.getStyleClass().add("precio-periodo");
        
        HBox precioBox = new HBox();
        precioBox.getChildren().add(lblPrecio);
        precioBox.getChildren().add(lblPeriodo);
        precioBox.setAlignment(Pos.BASELINE_LEFT);
        precioBox.setSpacing(4);

        // 4. Detalles
        Label lblDetalles = new Label(tarifa.getDetalles());
        lblDetalles.getStyleClass().add("detalles-tarifa-card");

        card.getChildren().add(topBox);
        card.getChildren().add(lblNombre);
        card.getChildren().add(precioBox);
        card.getChildren().add(lblDetalles);

        // Evento de ratón con clase anónima tradicional (sin ->)
        card.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (tarjetaSeleccionadaNode != null) {
                    tarjetaSeleccionadaNode.getStyleClass().remove("tarjeta-tarifa-seleccionada");
                }
                card.getStyleClass().add("tarjeta-tarifa-seleccionada");
                tarjetaSeleccionadaNode = card;
                tarifaSeleccionada = tarifa;
            }
        });

        return card;
    }

    @FXML
    public void btnCambiarTarifa(ActionEvent event) {
        if (tarifaSeleccionada == null) {
            ShowAlert.info("Aviso", "Por favor, selecciona una tarifa haciendo clic en una tarjeta.");
            return;
        }
        try {
            List<Contrato> contratos = contratoDAO.obtenerContratosCliente(cliente.getId());
            // Bucle for tradicional sin la sintaxis :
            for (int i = 0; i < contratos.size(); i++) {
                Contrato c = contratos.get(i);
                boolean mismaTarifa = tarifaSeleccionada.getId().equals(c.getIdTarifa());
                
                String estado = c.getEstado();
                boolean enVigor = "pendiente".equals(estado)
                        || "pendiente de firma".equals(estado)
                        || "activo".equals(estado);

                if (mismaTarifa && enVigor) {
                    ShowAlert.info("Aviso", "Ya tienes la tarifa " + tarifaSeleccionada.getNombreTarifa() + " pedida o contratada.");
                    return;
                }
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
            return;
        }

        if (!ShowAlert.confirmar("Cambio de tarifa", "¿Estás seguro de que deseas contratar la tarifa " + tarifaSeleccionada.getNombreTarifa() + "?")) {
            return;
        }

        LocalDate hoy = LocalDate.now();
        Contrato contrato = new Contrato(null, cliente.getTelefono(), "pendiente", cliente.getId(),
                tarifaSeleccionada.getId(), null, hoy.toString(), hoy.plusYears(1).toString());
        try {
            contratoDAO.insertarContrato(contrato, tarifaSeleccionada);
            ShowAlert.info("Petición enviada", "Tu petición de la tarifa " + tarifaSeleccionada.getNombreTarifa()
                    + " está pendiente de validar por un empleado.");
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    @FXML
    public void btnEliminarTarifa(ActionEvent event) {
        if (tarifaSeleccionada == null) {
            ShowAlert.info("Aviso", "Por favor, selecciona una tarifa haciendo clic en una tarjeta.");
            return;
        }
        if (!ShowAlert.confirmar("Confirmar eliminación", "¿Seguro que deseas eliminar tu petición de la tarifa " + tarifaSeleccionada.getNombreTarifa() + "?")) {
            return;
        }
        try {
            boolean borrada = contratoDAO.borrarPeticion(cliente.getId(), tarifaSeleccionada.getId());
            if (borrada) {
                ShowAlert.info("Tarifa eliminada", "Tu petición de la tarifa " + tarifaSeleccionada.getNombreTarifa() + " ha sido eliminada.");
            } else {
                ShowAlert.info("Aviso", "No tienes ninguna petición pendiente de esa tarifa.");
            }
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }
}