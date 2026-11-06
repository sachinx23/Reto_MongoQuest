package daoempleado;

import exception.OurException;
import java.util.List;
import modelo.TicketSoporte;

/**
 * Tickets pendientes que resuelve el empleado.
 */
public interface TicketDAO {

    List<TicketSoporte> obtenerTicketsPendientes() throws OurException;

    void resolverTicket(String idTicket, String idEmpleado) throws OurException;
}
