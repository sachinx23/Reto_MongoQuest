package daocliente;

import exception.OurException;
import java.util.List;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Usuario;
import modelo.Valoracion;

/**
 * Operaciones con la base de datos de la parte de cliente, más el login y el registro (comunes a todos).
 */
public interface ClienteDAO {

    // Devuelve un Cliente, Empleado o Administrador, o null si los datos no son correctos
    Usuario login(String nombreUsuario, String contrasena) throws OurException;

    void insertarCliente(Cliente cliente) throws OurException;

    void actualizarCliente(Cliente cliente) throws OurException;

    List<Tarifa> obtenerTarifas() throws OurException;

    List<Contrato> obtenerContratosCliente(String idCliente) throws OurException;

    void insertarContrato(Contrato contrato) throws OurException;

    // Borra la petición pendiente de una tarifa. Devuelve false si no había ninguna
    boolean borrarPeticion(String idCliente, String idTarifa) throws OurException;

    void solicitarBaja(String idContrato) throws OurException;

    List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException;

    void insertarTicket(TicketSoporte ticket) throws OurException;

    List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException;

    void insertarValoracion(Valoracion valoracion) throws OurException;

    void actualizarValoracion(Valoracion valoracion) throws OurException;

    void borrarValoracion(String idValoracion) throws OurException;
}
