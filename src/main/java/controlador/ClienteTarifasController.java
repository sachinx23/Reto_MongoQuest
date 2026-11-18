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

/**
 * Controlador de la subvista de catálogo y contratación de tarifas del cliente en JavaFX.
 * Muestra dinámicamente las tarifas activas en formato de tarjetas interactivas ({@link VBox}),
 * permitiendo al cliente seleccionar una tarifa para solicitar su contratación o cancelar
 * una petición pendiente previamente realizada.
 *
 * @author Eneko.Plaza
 */
public class ClienteTarifasController {

    /**
     * Registrador de eventos (Logger) para trazar advertencias y operaciones del controlador.
     */
    private static final Logger LOGGER = Logger.getLogger(ClienteTarifasController.class.getName());

    /** Objeto de acceso a datos para consultar el catálogo de tarifas disponibles. */
    private final TarifaDAO tarifaDAO = new TarifaDAOImplementacion();

    /** Objeto de acceso a datos para registrar o cancelar solicitudes de contratación. */
    private final ContratoDAO contratoDAO = new ContratoDAOImplementacion();

    /** Instancia del cliente que tiene la sesión iniciada actualmente. */
    private Cliente cliente;

    /** Contenedor visual adaptable donde se renderizan las tarjetas de las tarifas activas. */
    @FXML private FlowPane contenedorTarifas;

    /** Referencia al modelo {@link Tarifa} de la tarjeta seleccionada actualmente por el usuario. */
    private Tarifa tarifaSeleccionada;

    /** Referencia al nodo visual {@link VBox} de la tarjeta seleccionada para gestionar su estilo CSS. */
    private VBox tarjetaSeleccionadaNode;

    /**
     * Inicializa el controlador tras cargar su archivo FXML.
     * Obtiene el cliente activo desde {@link ClienteController} y carga en pantalla
     * el catálogo de tarifas activas.
     */
    @FXML
    public void initialize() {
        cliente = ClienteController.getCliente();
        cargarTarifas();
    }

    /**
     * Consulta las tarifas activas a través de {@link TarifaDAO#tarifasActivas()} y delega
     * su renderizado en la interfaz gráfica.
     */
    private void cargarTarifas() {
        try {
            List<Tarifa> listaTarifas = tarifaDAO.tarifasActivas();
            mostrarTarifasEnTarjetas(listaTarifas);
        } catch (OurException e) {
            LOGGER.warning(e.getMessage());
            ShowAlert.error(e.getMessage());
        }
    }

    /**
     * Limpia el contenedor principal, reinicia el estado de selección y genera una tarjeta
     * visual por cada tarifa recibida en la lista.
     *
     * @param listaTarifas Lista de objetos {@link Tarifa} que se mostrarán en el panel.
     */
    private void mostrarTarifasEnTarjetas(List<Tarifa> listaTarifas) {
        contenedorTarifas.getChildren().clear();
        tarifaSeleccionada = null;
        tarjetaSeleccionadaNode = null;

        for (int i = 0; i < listaTarifas.size(); i++) {
            Tarifa t = listaTarifas.get(i);
            VBox card = crearTarjetaTarifa(t);
            contenedorTarifas.getChildren().add(card);
        }
    }

    /**
     * Construye el componente visual {@link VBox} que representa una tarjeta individual de tarifa
     * (tipo, nombre, precio mensual y detalles) y le asocia un evento de clic para marcarla como
     * seleccionada aplicando la clase CSS {@code "tarjeta-tarifa-seleccionada"}.
     *
     * @param tarifa Objeto {@link Tarifa} cuyos datos se mostrarán en la tarjeta.
     * @return Nodo {@link VBox} completamente configurado y listo para añadirse al contenedor.
     */
    private VBox crearTarjetaTarifa(final Tarifa tarifa) {
        final VBox card = new VBox();
        card.getStyleClass().add("tarjeta-tarifa");

        String textoTipo = "Tarifa";
        if (tarifa.getTipoTarifa() != null) {
            textoTipo = tarifa.getTipoTarifa();
        }
        Label lblTipo = new Label(textoTipo);
        lblTipo.getStyleClass().add("badge-tipo-tarifa");
        
        HBox topBox = new HBox();
        topBox.getChildren().add(lblTipo);
        topBox.setAlignment(Pos.TOP_LEFT);

        
        Label lblNombre = new Label(tarifa.getNombreTarifa());
        lblNombre.getStyleClass().add("titulo-tarifa-card");

        Label lblPrecio = new Label(tarifa.getPrecio() + " €");
        lblPrecio.getStyleClass().add("precio-tarifa-card");
        
        Label lblPeriodo = new Label("/ mes");
        lblPeriodo.getStyleClass().add("precio-periodo");
        
        HBox precioBox = new HBox();
        precioBox.getChildren().add(lblPrecio);
        precioBox.getChildren().add(lblPeriodo);
        precioBox.setAlignment(Pos.BASELINE_LEFT);
        precioBox.setSpacing(4);

        Label lblDetalles = new Label(tarifa.getDetalles());
        lblDetalles.getStyleClass().add("detalles-tarifa-card");

        card.getChildren().add(topBox);
        card.getChildren().add(lblNombre);
        card.getChildren().add(precioBox);
        card.getChildren().add(lblDetalles);

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

    /**
     * Manejador del botón para solicitar la contratación de la tarifa seleccionada.
     * Verifica que exista una tarjeta marcada, comprueba que el cliente no tenga ya esa misma
     * tarifa en estado vigente ({@code "pendiente"}, {@code "pendiente de firma"} o {@code "activo"}),
     * pide confirmación y crea un nuevo contrato en estado {@code "pendiente"} con vigencia de un año.
     *
     * @param event Evento de acción disparado al pulsar el botón de contratar/cambiar tarifa.
     */
    @FXML
    public void btnCambiarTarifa(ActionEvent event) {
        if (tarifaSeleccionada == null) {
            ShowAlert.info("Aviso", "Por favor, selecciona una tarifa haciendo clic en una tarjeta.");
            return;
        }
        try {
            List<Contrato> contratos = contratoDAO.obtenerContratosCliente(cliente.getId());
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

    /**
     * Manejador del botón para cancelar una solicitud de tarifa pendiente.
     * Verifica que haya una tarifa seleccionada, solicita confirmación al cliente e intenta
     * eliminar la petición mediante {@link ContratoDAO#borrarPeticion(String, String)}.
     *
     * @param event Evento de acción disparado al pulsar el botón de eliminar petición de tarifa.
     */
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