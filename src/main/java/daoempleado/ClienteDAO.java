package daoempleado;

import exception.OurException;
import java.util.List;
import modelo.Cliente;

/**
 * Solicitudes de baja de los clientes.
 */
public interface ClienteDAO {

    List<Cliente> obtenerClientesConBajaPendiente() throws OurException;

    void aprobarBaja(String idCliente, String idEmpleado) throws OurException;
}
