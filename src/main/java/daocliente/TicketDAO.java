package daocliente;

import exception.OurException;
import java.util.List;
import modelocliente.TicketSoporte;

/**
 * Tickets de soporte del cliente.
 */
public interface TicketDAO {
    /**
     * Tickets de un cliente.
     *
     * @param idCliente id del cliente
     * @return tickets del cliente
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException;

    /**
     * Guarda un ticket nuevo y lo añade a la lista del cliente.
     *
     * @param ticket ticket con los datos
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void insertarTicket(TicketSoporte ticket) throws OurException;
}
