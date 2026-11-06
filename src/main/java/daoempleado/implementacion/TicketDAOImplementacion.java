package daoempleado.implementacion;

import com.mongodb.MongoException;
import daocliente.implementacion.DAOBase;
import daoempleado.TicketDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.TicketSoporte;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección TicketsSoporte (parte de empleado).
 */
public class TicketDAOImplementacion extends DAOBase implements TicketDAO {

    private static final Logger LOGGER = Logger.getLogger(TicketDAOImplementacion.class.getName());

    @Override
    public List<TicketSoporte> obtenerTicketsPendientes() throws OurException {
        try {
            List<TicketSoporte> tickets = buscarTickets(new Document("estado", "pendiente"));
            LOGGER.info("Consultados " + tickets.size() + " tickets pendientes");
            return tickets;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando tickets pendientes", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
    }

    @Override
    public void resolverTicket(String idTicket, String idEmpleado) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("estado", "cerrado");
            cambios.put("idEmpleado", new ObjectId(idEmpleado));
            if (!modificar(COL_TICKETS, idTicket, cambios)) {
                throw new OurException(ErrorMessages.RESOLVER_TICKET);
            }
            LOGGER.info("Ticket " + idTicket + " resuelto por el empleado " + idEmpleado);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error resolviendo el ticket " + idTicket, e);
            throw new OurException(ErrorMessages.RESOLVER_TICKET);
        }
    }
}
