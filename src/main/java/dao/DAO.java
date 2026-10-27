package dao;

import exception.OurException;
import java.util.List;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.Tarifa;
import modelo.TicketSoporte;

/**
 * Operaciones de acceso a datos de la aplicación. Los controladores solo
 * conocen esta interfaz, nunca las clases de MongoDB.
 */
public interface DAO {

    /**
     * Busca un administrador con ese usuario y contraseña.
     *
     * @return el administrador, o null si no existe
     */
    Administrador loginAdministrador(String usuario, String contrasena) throws OurException;

    /**
     * Busca un empleado con ese usuario y contraseña.
     *
     * @return el empleado, o null si no existe
     */
    Empleado loginEmpleado(String usuario, String contrasena) throws OurException;

    /**
     * Busca un cliente con ese usuario y contraseña.
     *
     * @return el cliente, o null si no existe
     */
    Cliente loginCliente(String usuario, String contrasena) throws OurException;

    /**
     * Inserta un cliente nuevo. Al volver, el cliente tiene su id asignado.
     *
     * @throws OurException si el DNI o el nombre de usuario ya existen, o falla la inserción
     */
    void insertarCliente(Cliente cliente) throws OurException;

    /** Guarda los datos personales modificados de un cliente. */
    void actualizarCliente(Cliente cliente) throws OurException;

    /** Devuelve todas las tarifas activas. */
    List<Tarifa> obtenerTarifas() throws OurException;

    /** Devuelve los contratos de un cliente, con el nombre de la tarifa. */
    List<Contrato> obtenerContratosCliente(String idCliente) throws OurException;

    /** Inserta un contrato nuevo (el cliente lo crea en estado "pendiente"). */
    void insertarContrato(Contrato contrato) throws OurException;

    /** El cliente pide la baja de un contrato: pasa a estado "baja pendiente". */
    void solicitarBaja(String idContrato) throws OurException;

    /**
     * Devuelve los contratos con un estado concreto, con los datos del
     * cliente y de la tarifa. Lo usa el empleado.
     */
    List<Contrato> obtenerContratosPorEstado(String estado) throws OurException;

    /** El empleado valida una petición de tarifa: el contrato pasa a "activo". */
    void validarContrato(String idContrato, String idEmpleado) throws OurException;

    /**
     * El empleado aprueba una baja: el contrato pasa a "cancelado" y, si el
     * cliente ya no tiene más contratos, se marca como dadoDeBaja.
     */
    void aprobarBaja(Contrato contrato, String idEmpleado) throws OurException;

    /** Devuelve los tickets de soporte de un cliente. */
    List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException;

    /** Inserta un ticket de soporte nuevo. */
    void insertarTicket(TicketSoporte ticket) throws OurException;

    /** Devuelve los tickets pendientes de todos los clientes, con el nombre del cliente. */
    List<TicketSoporte> obtenerTicketsPendientes() throws OurException;

    /** El empleado marca un ticket como "resuelto". */
    void resolverTicket(String idTicket, String idEmpleado) throws OurException;
}
