package dao;

import exception.OurException;
import java.util.List;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.EstadoContrato;
import modelo.Sucursal;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Usuario;
import modelo.Valoracion;

/**
 * Operaciones con la base de datos. Las pantallas solo usan esta interfaz.
 */
public interface DAO {

    // Devuelve un Cliente, Empleado o Administrador, o null si los datos no son correctos
    Usuario login(String nombreUsuario, String contrasena) throws OurException;

    void insertarCliente(Cliente cliente) throws OurException;

    void actualizarCliente(Cliente cliente) throws OurException;

    List<Tarifa> obtenerTarifas() throws OurException;

    List<Contrato> obtenerContratosCliente(String idCliente) throws OurException;

    List<Contrato> obtenerContratosPorEstado(EstadoContrato estado) throws OurException;

    void insertarContrato(Contrato contrato) throws OurException;

    // Borra la petición pendiente de una tarifa. Devuelve false si no había ninguna
    boolean borrarPeticion(String idCliente, String idTarifa) throws OurException;

    void solicitarBaja(String idContrato) throws OurException;

    void validarContrato(String idContrato, String idEmpleado) throws OurException;

    List<Cliente> obtenerClientesConBajaPendiente() throws OurException;

    void aprobarBaja(String idCliente, String idEmpleado) throws OurException;

    List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException;

    List<TicketSoporte> obtenerTicketsPendientes() throws OurException;

    void insertarTicket(TicketSoporte ticket) throws OurException;

    void resolverTicket(String idTicket, String idEmpleado) throws OurException;

    // Sirve para empleados y administradores
    void actualizarEmpleado(Empleado empleado) throws OurException;

    // ---------- ADMINISTRADOR ----------

    // Empleados y administradores (el administrador ve toda la plantilla)
    List<Empleado> obtenerEmpleados() throws OurException;

    void insertarEmpleado(Empleado empleado) throws OurException;

    // Pasa el empleado de la colección Empleados a Administradores
    void ascenderEmpleado(Empleado empleado) throws OurException;

    void borrarEmpleado(String idEmpleado) throws OurException;

    List<Cliente> obtenerClientes() throws OurException;

    // Borra la cuenta bancaria, el teléfono y la dirección de un cliente dado de baja
    void borrarDatosSensiblesCliente(String idCliente) throws OurException;

    List<Sucursal> obtenerSucursales() throws OurException;

    void insertarSucursal(Sucursal sucursal) throws OurException;

    void actualizarSucursal(Sucursal sucursal) throws OurException;

    void borrarSucursal(String idSucursal) throws OurException;

    // Todas las tarifas, también las desactivadas
    List<Tarifa> obtenerTodasLasTarifas() throws OurException;

    void insertarTarifa(Tarifa tarifa) throws OurException;

    void actualizarTarifa(Tarifa tarifa) throws OurException;

    void borrarTarifa(String idTarifa) throws OurException;

    List<Contrato> obtenerTodosLosContratos() throws OurException;

    // Una petición pendiente que el empleado o administrador no acepta pasa a cancelado
    void rechazarPeticion(String idContrato, String idEmpleado) throws OurException;

    // Un contrato activo pasa a cancelado sin que el cliente lo pida
    void rescindirContrato(String idContrato, String idEmpleado) throws OurException;

    List<TicketSoporte> obtenerTodosLosTickets() throws OurException;

    List<Valoracion> obtenerTodasLasValoraciones() throws OurException;

    // ---------- VALORACIONES DEL CLIENTE ----------

    List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException;

    void insertarValoracion(Valoracion valoracion) throws OurException;

    void actualizarValoracion(Valoracion valoracion) throws OurException;

    void borrarValoracion(String idValoracion) throws OurException;
}
