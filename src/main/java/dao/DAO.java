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
     * @throws OurException si el nombre de usuario ya existe o falla la inserción
     */
    void insertarCliente(Cliente cliente) throws OurException;

    /** Guarda los datos personales modificados de un cliente. */
    void actualizarCliente(Cliente cliente) throws OurException;

    /** Devuelve todas las tarifas activas. */
    List<Tarifa> obtenerTarifas() throws OurException;

    /** Devuelve los contratos de un cliente. */
    List<Contrato> obtenerContratosCliente(String idCliente) throws OurException;

    /** Inserta un contrato nuevo. */
    void insertarContrato(Contrato contrato) throws OurException;

    /** Cambia el estado de un contrato a "rescindido". */
    void rescindirContrato(String idContrato) throws OurException;

    /** Devuelve los tickets de soporte de un cliente. */
    List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException;

    /** Inserta un ticket de soporte nuevo. */
    void insertarTicket(TicketSoporte ticket) throws OurException;
}
