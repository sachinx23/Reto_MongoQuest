package daoempleado;

import exception.OurException;
import java.util.List;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.EstadoContrato;
import modelo.TicketSoporte;

/**
 * Operaciones con la base de datos de la parte de empleado.
 */
public interface EmpleadoDAO {

    // Sirve para empleados y administradores
    void actualizarEmpleado(Empleado empleado) throws OurException;

    List<Contrato> obtenerContratosPorEstado(EstadoContrato estado) throws OurException;

    void validarContrato(String idContrato, String idEmpleado) throws OurException;

    List<Cliente> obtenerClientesConBajaPendiente() throws OurException;

    void aprobarBaja(String idCliente, String idEmpleado) throws OurException;

    List<TicketSoporte> obtenerTicketsPendientes() throws OurException;

    void resolverTicket(String idTicket, String idEmpleado) throws OurException;
}
