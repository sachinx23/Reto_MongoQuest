package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.TicketDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.TicketSoporte;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección TicketsSoporte (parte de cliente).
 */
public class TicketDAOImplementacion extends DAOBase implements TicketDAO {

    private static final Logger LOGGER = Logger.getLogger(TicketDAOImplementacion.class.getName());

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
            // "incidencia": así lo ve también la pantalla de tickets del administrador
            d.put("tipo", "incidencia");
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
}
