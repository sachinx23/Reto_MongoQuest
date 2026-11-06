package daocliente.implementacion;

import com.mongodb.MongoException;
import dao.implementacion.DAOImplementacion;
import daocliente.ClienteDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Cliente;
import modelo.Contrato;
import modelo.EstadoContrato;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Valoracion;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de MongoDB de la parte de cliente: perfil, tarifas, contratos, tickets y valoraciones.
 */
public class ClienteDAOImplementacion extends DAOImplementacion implements ClienteDAO {

    private static final Logger LOGGER = Logger.getLogger(ClienteDAOImplementacion.class.getName());

    @Override
    public void actualizarCliente(Cliente cliente) throws OurException {
        try {
            if (nombreUsuarioOcupado(cliente.getNombreUsuario(), cliente.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            // El DNI tampoco puede ser el de otro cliente
            Document otro = coleccion(COL_CLIENTES).find(new Document("DNI", cliente.getDni())).first();
            if (otro != null && !idTexto(otro, "_id").equals(cliente.getId())) {
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            Document cambios = new Document();
            cambios.put("nombre", cliente.getNombre());
            cambios.put("nombreUsuario", cliente.getNombreUsuario());
            cambios.put("DNI", cliente.getDni());
            cambios.put("correo", cliente.getCorreo());
            cambios.put("direccion", cliente.getDireccion());
            cambios.put("telefono", cliente.getTelefono());
            cambios.put("cuentaBancaria", cliente.getCuentaBancaria());

            if (!modificar(COL_CLIENTES, cliente.getId(), cambios)) {
                throw new OurException(ErrorMessages.UPDATE_USER);
            }
            LOGGER.info("Cliente modificado: " + cliente.getNombreUsuario() + " (id " + cliente.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando el cliente " + cliente.getId(), e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }

    @Override
    public List<Tarifa> obtenerTarifas() throws OurException {
        List<Tarifa> tarifas = new ArrayList<>();
        try {
            for (Document d : coleccion(COL_TARIFAS).find()) {
                // Solo se enseñan las tarifas activas (si no tiene el campo "activa", se considera activa)
                boolean activa = booleano(d, "activa", true);
                if (activa) {
                    tarifas.add(documentoATarifa(d));
                }
            }
            LOGGER.info("Consultadas " + tarifas.size() + " tarifas");
            return tarifas;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando tarifas", e);
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
    }

    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultados " + contratos.size() + " contratos del cliente " + idCliente);
            return contratos;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando contratos del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void insertarContrato(Contrato contrato) throws OurException {
        try {
            Document d = new Document();
            d.put("telefono", contrato.getTelefono());
            d.put("estado", contrato.getEstado().toString());
            d.put("idCliente", new ObjectId(contrato.getIdCliente()));
            d.put("idTarifa", new ObjectId(contrato.getIdTarifa()));
            d.put("idEmpleado", aObjectId(contrato.getIdEmpleado()));

            // Información embebida: los datos de la tarifa se guardan dentro del contrato
            Document tarifa = new Document();
            tarifa.put("nombreTarifa", contrato.getNombreTarifa());
            tarifa.put("precio", contrato.getPrecio());
            d.put("tarifa", tarifa);

            d.put("fechaComienzo", contrato.getFechaComienzo());
            d.put("fechaFinalizacion", contrato.getFechaFinalizacion());
            coleccion(COL_CONTRATOS).insertOne(d);

            contrato.setId(idTexto(d, "_id"));
            LOGGER.info("Contrato creado: id " + contrato.getId() + ", cliente " + contrato.getIdCliente()
                    + ", tarifa " + contrato.getIdTarifa() + ", estado " + contrato.getEstado());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error creando un contrato del cliente " + contrato.getIdCliente(), e);
            throw new OurException(ErrorMessages.INSERT_CONTRATO);
        }
    }

    @Override
    public boolean borrarPeticion(String idCliente, String idTarifa) throws OurException {
        try {
            Document filtro = new Document();
            filtro.put("idCliente", new ObjectId(idCliente));
            filtro.put("idTarifa", new ObjectId(idTarifa));
            filtro.put("estado", EstadoContrato.PENDIENTE.toString());
            long borrados = coleccion(COL_CONTRATOS).deleteOne(filtro).getDeletedCount();
            LOGGER.info("Petición de tarifa borrada: cliente " + idCliente + ", tarifa " + idTarifa + ", borrados " + borrados);
            return borrados > 0;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la petición del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        cambiarEstadoContrato(idContrato, EstadoContrato.BAJA_PENDIENTE, null, ErrorMessages.BAJA_CONTRATO);
        LOGGER.info("Baja solicitada del contrato " + idContrato);
    }

    @Override
    public List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException {
        try {
            List<TicketSoporte> tickets = buscarTickets(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultados " + tickets.size() + " tickets del cliente " + idCliente);
            return tickets;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando tickets del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
    }

    @Override
    public void insertarTicket(TicketSoporte ticket) throws OurException {
        try {
            Document d = new Document();
            d.put("asunto", ticket.getAsunto());
            d.put("descripcion", ticket.getDescripcion());
            d.put("fechaCreacion", ticket.getFechaCreacion());
            d.put("estado", ticket.getEstado());
            d.put("idCliente", new ObjectId(ticket.getIdCliente()));
            d.put("idEmpleado", null);
            // 1. Guardar el ticket
            coleccion(COL_TICKETS).insertOne(d);

            // 2. Array de _id: el cliente guarda los _id de sus tickets en "ticketSoporte".
            // "$push" añade un elemento al final del array
            Document filtroCliente = new Document("_id", new ObjectId(ticket.getIdCliente()));
            Document nuevoTicket = new Document("ticketSoporte", d.get("_id"));
            try {
                coleccion(COL_CLIENTES).updateOne(filtroCliente, new Document("$push", nuevoTicket));
            } catch (MongoException e) {
                // Si falla el paso 2, se deshace el 1: se borra el ticket
                coleccion(COL_TICKETS).deleteOne(new Document("_id", d.get("_id")));
                throw e;
            }
            ticket.setId(idTexto(d, "_id"));
            LOGGER.info("Ticket creado: id " + ticket.getId() + ", cliente " + ticket.getIdCliente() + ", asunto \"" + ticket.getAsunto() + "\"");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error creando un ticket del cliente " + ticket.getIdCliente(), e);
            throw new OurException(ErrorMessages.INSERT_TICKET);
        }
    }

    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        try {
            List<Valoracion> valoraciones = buscarValoraciones(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultadas " + valoraciones.size() + " valoraciones del cliente " + idCliente);
            return valoraciones;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando valoraciones del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document d = new Document();
            d.put("puntuacion", valoracion.getPuntuacion());
            d.put("texto", valoracion.getTexto());
            d.put("idCliente", new ObjectId(valoracion.getIdCliente()));
            d.put("idTarifa", aObjectId(valoracion.getIdTarifa()));
            coleccion(COL_VALORACIONES).insertOne(d);

            valoracion.setId(idTexto(d, "_id"));
            LOGGER.info("Valoración publicada: id " + valoracion.getId() + ", puntuación " + valoracion.getPuntuacion());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error publicando una valoración del cliente " + valoracion.getIdCliente(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void actualizarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("puntuacion", valoracion.getPuntuacion());
            cambios.put("texto", valoracion.getTexto());
            if (!modificar(COL_VALORACIONES, valoracion.getId(), cambios)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LOGGER.info("Valoración modificada: id " + valoracion.getId());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando la valoración " + valoracion.getId(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void borrarValoracion(String idValoracion) throws OurException {
        try {
            if (!borrar(COL_VALORACIONES, idValoracion)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LOGGER.info("Valoración borrada: id " + idValoracion);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la valoración " + idValoracion, e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    private List<Valoracion> buscarValoraciones(Document filtro) throws OurException {
        List<Valoracion> valoraciones = new ArrayList<>();
        for (Document d : coleccion(COL_VALORACIONES).find(filtro)) {
            Valoracion v = new Valoracion(idTexto(d, "_id"), (int) numero(d, "puntuacion"), texto(d, "texto"),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"));
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                v.setNombreCliente(texto(cliente, "nombre"));
            }
            valoraciones.add(v);
        }
        return valoraciones;
    }
}
