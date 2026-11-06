package daocliente;

import exception.OurException;
import java.util.List;
import modelo.TicketSoporte;

/**
 * Tickets de soporte del cliente.
 */
public interface TicketDAO {

    List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException;

    void insertarTicket(TicketSoporte ticket) throws OurException;
}
