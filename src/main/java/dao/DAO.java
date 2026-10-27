package dao;

import exception.OurException;
import java.util.List;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.Sucursal;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Valoracion;

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

    /** Devuelve los clientes que tienen algún contrato en "baja pendiente". */
    List<Cliente> obtenerClientesConBajaPendiente() throws OurException;

    /**
     * El empleado aprueba la baja de un cliente: sus contratos en "baja pendiente"
     * pasan a "cancelado" y, si ya no le queda ninguno en vigor, se marca como dadoDeBaja.
     */
    void aprobarBaja(String idCliente, String idEmpleado) throws OurException;

    /** Devuelve los tickets de soporte de un cliente. */
    List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException;

    /** Inserta un ticket de soporte nuevo. */
    void insertarTicket(TicketSoporte ticket) throws OurException;

    /** Devuelve los tickets pendientes de todos los clientes, con el nombre del cliente. */
    List<TicketSoporte> obtenerTicketsPendientes() throws OurException;

    /** El empleado marca un ticket como "resuelto". */
    void resolverTicket(String idTicket, String idEmpleado) throws OurException;

    // ------------------------------------------------------------ EMPLEADOS Y ADMINISTRADORES

    /**
     * Guarda los datos modificados de un empleado. Si es un {@link Administrador}
     * se guarda en la colección de administradores.
     *
     * @throws OurException si el nombre de usuario ya lo usa otra persona o falla la actualización
     */
    void actualizarEmpleado(Empleado empleado) throws OurException;

    /** Devuelve todos los empleados. */
    List<Empleado> obtenerEmpleados() throws OurException;

    /** Da de alta un empleado nuevo. Al volver, el empleado tiene su id asignado. */
    void insertarEmpleado(Empleado empleado) throws OurException;

    /** Asciende un empleado a administrador: pasa de la colección Empleados a Administradores. */
    void ascenderEmpleado(Empleado empleado) throws OurException;

    /** Borra un empleado y lo quita de la plantilla de su sucursal. */
    void borrarEmpleado(String idEmpleado) throws OurException;

    // ------------------------------------------------------------ SUCURSALES

    /** Devuelve todas las sucursales. */
    List<Sucursal> obtenerSucursales() throws OurException;

    /** Crea una sucursal nueva. Al volver, la sucursal tiene su id asignado. */
    void insertarSucursal(Sucursal sucursal) throws OurException;

    /** Guarda los datos modificados de una sucursal. */
    void actualizarSucursal(Sucursal sucursal) throws OurException;

    /**
     * Borra una sucursal.
     *
     * @throws OurException si todavía tiene empleados asignados
     */
    void borrarSucursal(String idSucursal) throws OurException;

    // ------------------------------------------------------------ VALORACIONES

    /** Devuelve las valoraciones de un cliente. */
    List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException;

    /** Publica una valoración nueva. Al volver, la valoración tiene su id asignado. */
    void insertarValoracion(Valoracion valoracion) throws OurException;

    /** Guarda la puntuación y el texto modificados de una valoración. */
    void actualizarValoracion(Valoracion valoracion) throws OurException;

    /** Borra una valoración. */
    void borrarValoracion(String idValoracion) throws OurException;
}
