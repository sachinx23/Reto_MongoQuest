package dao;

import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.Sucursal;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Usuario;
import modelo.Valoracion;

/**
 * DAO falso que guarda los datos en listas. Se usa en los tests para no necesitar MongoDB.
 * Usuarios de prueba (contraseña "abcd"): cliente, empleado y admin.
 */
public class MockModelDao implements DAO {

    public static final String ERROR_SIMULADO = "Error simulado en la base de datos";

    private boolean error = false;
    private int contador = 0;

    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Administrador> administradores = new ArrayList<>();
    private final List<Tarifa> tarifas = new ArrayList<>();
    private final List<Contrato> contratos = new ArrayList<>();
    private final List<TicketSoporte> tickets = new ArrayList<>();
    private final List<Sucursal> sucursales = new ArrayList<>();
    private final List<Valoracion> valoraciones = new ArrayList<>();

    public MockModelDao() {
        clientes.add(new Cliente(nuevoId(), "Pedro", "cliente", "abcd", "pedro@gmail.com", "ES79",
                "12345678F", "666666666", "calle abando 2"));
        empleados.add(new Empleado(nuevoId(), "Pablo", "empleado", "abcd", "pablo@gmail.com", "ES79", null));
        administradores.add(new Administrador(nuevoId(), "Aitor", "admin", "abcd", "aitor@gmail.com", "ES79"));
        tarifas.add(new Tarifa(nuevoId(), "Onda Fibra 600", 40, "600 MB", "Fibra", true));
        tarifas.add(new Tarifa(nuevoId(), "Onda Móvil 30", 15, "30 GB", "Móvil", true));
        sucursales.add(new Sucursal(nuevoId(), "Bilbao", "9-20", "944000000", "Lonbo Kalea", "Bilbao", 48480,
                new ArrayList<String>()));
    }

    // Si se activa, todos los métodos lanzan una excepción (para probar los errores)
    public void setError(boolean error) {
        this.error = error;
    }

    public List<Contrato> getContratos() {
        return contratos;
    }

    public List<TicketSoporte> getTickets() {
        return tickets;
    }

    public List<Empleado> getEmpleados() {
        return empleados;
    }

    public List<Sucursal> getSucursales() {
        return sucursales;
    }

    private void comprobarError() throws OurException {
        if (error) {
            throw new OurException(ERROR_SIMULADO);
        }
    }

    private String nuevoId() {
        contador++;
        return "id" + contador;
    }

    private boolean nombreUsuarioOcupado(String nombreUsuario, String idExcluir) {
        List<Usuario> todos = new ArrayList<>();
        todos.addAll(clientes);
        todos.addAll(empleados);
        todos.addAll(administradores);
        for (Usuario u : todos) {
            if (u.getNombreUsuario().equals(nombreUsuario) && !u.getId().equals(idExcluir)) {
                return true;
            }
        }
        return false;
    }

    private Contrato buscarContrato(String idContrato) {
        for (Contrato c : contratos) {
            if (c.getId().equals(idContrato)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Usuario login(String nombreUsuario, String contrasena) throws OurException {
        comprobarError();
        List<Usuario> todos = new ArrayList<>();
        todos.addAll(administradores);
        todos.addAll(empleados);
        todos.addAll(clientes);
        for (Usuario u : todos) {
            if (u.getNombreUsuario().equals(nombreUsuario) && u.getContrasena().equals(contrasena)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void insertarCliente(Cliente cliente) throws OurException {
        comprobarError();
        for (Cliente c : clientes) {
            if (c.getDni().equals(cliente.getDni())) {
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
        }
        if (nombreUsuarioOcupado(cliente.getNombreUsuario(), null)) {
            throw new OurException(ErrorMessages.USER_EXISTS);
        }
        cliente.setId(nuevoId());
        clientes.add(cliente);
    }

    @Override
    public void actualizarCliente(Cliente cliente) throws OurException {
        comprobarError();
        if (nombreUsuarioOcupado(cliente.getNombreUsuario(), cliente.getId())) {
            throw new OurException(ErrorMessages.USER_EXISTS);
        }
    }

    @Override
    public List<Tarifa> obtenerTarifas() throws OurException {
        comprobarError();
        return tarifas;
    }

    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        comprobarError();
        List<Contrato> resultado = new ArrayList<>();
        for (Contrato c : contratos) {
            if (c.getIdCliente().equals(idCliente)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public List<Contrato> obtenerContratosPorEstado(String estado) throws OurException {
        comprobarError();
        List<Contrato> resultado = new ArrayList<>();
        for (Contrato c : contratos) {
            if (c.getEstado().equals(estado)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public void insertarContrato(Contrato contrato) throws OurException {
        comprobarError();
        contrato.setId(nuevoId());
        for (Tarifa t : tarifas) {
            if (t.getId().equals(contrato.getIdTarifa())) {
                contrato.setNombreTarifa(t.getNombreTarifa());
                contrato.setPrecio(t.getPrecio());
            }
        }
        contratos.add(contrato);
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        comprobarError();
        buscarContrato(idContrato).setEstado(DAOImplementacion.ESTADO_BAJA_PENDIENTE);
    }

    @Override
    public void validarContrato(String idContrato, String idEmpleado) throws OurException {
        comprobarError();
        Contrato c = buscarContrato(idContrato);
        c.setEstado(DAOImplementacion.ESTADO_ACTIVO);
        c.setIdEmpleado(idEmpleado);
    }

    @Override
    public List<Cliente> obtenerClientesConBajaPendiente() throws OurException {
        comprobarError();
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente cliente : clientes) {
            for (Contrato c : contratos) {
                if (c.getIdCliente().equals(cliente.getId())
                        && c.getEstado().equals(DAOImplementacion.ESTADO_BAJA_PENDIENTE)
                        && !resultado.contains(cliente)) {
                    resultado.add(cliente);
                }
            }
        }
        return resultado;
    }

    @Override
    public void aprobarBaja(String idCliente, String idEmpleado) throws OurException {
        comprobarError();
        for (Contrato c : contratos) {
            if (c.getIdCliente().equals(idCliente) && c.getEstado().equals(DAOImplementacion.ESTADO_BAJA_PENDIENTE)) {
                c.setEstado(DAOImplementacion.ESTADO_CANCELADO);
                c.setIdEmpleado(idEmpleado);
            }
        }
    }

    @Override
    public List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException {
        comprobarError();
        List<TicketSoporte> resultado = new ArrayList<>();
        for (TicketSoporte t : tickets) {
            if (t.getIdCliente().equals(idCliente)) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    @Override
    public List<TicketSoporte> obtenerTicketsPendientes() throws OurException {
        comprobarError();
        List<TicketSoporte> resultado = new ArrayList<>();
        for (TicketSoporte t : tickets) {
            if (t.getEstado().equals("pendiente")) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    @Override
    public void insertarTicket(TicketSoporte ticket) throws OurException {
        comprobarError();
        ticket.setId(nuevoId());
        tickets.add(ticket);
    }

    @Override
    public void resolverTicket(String idTicket, String idEmpleado) throws OurException {
        comprobarError();
        for (TicketSoporte t : tickets) {
            if (t.getId().equals(idTicket)) {
                t.setEstado("resuelto");
                t.setIdEmpleado(idEmpleado);
            }
        }
    }

    @Override
    public void actualizarEmpleado(Empleado empleado) throws OurException {
        comprobarError();
        if (nombreUsuarioOcupado(empleado.getNombreUsuario(), empleado.getId())) {
            throw new OurException(ErrorMessages.USER_EXISTS);
        }
    }

    @Override
    public List<Empleado> obtenerEmpleados() throws OurException {
        comprobarError();
        return empleados;
    }

    @Override
    public void insertarEmpleado(Empleado empleado) throws OurException {
        comprobarError();
        if (nombreUsuarioOcupado(empleado.getNombreUsuario(), null)) {
            throw new OurException(ErrorMessages.USER_EXISTS);
        }
        empleado.setId(nuevoId());
        empleados.add(empleado);
    }

    @Override
    public void ascenderEmpleado(Empleado empleado) throws OurException {
        comprobarError();
        empleados.remove(empleado);
        administradores.add(new Administrador(empleado.getId(), empleado.getNombre(), empleado.getNombreUsuario(),
                empleado.getContrasena(), empleado.getCorreo(), empleado.getCuentaBancaria()));
    }

    @Override
    public void borrarEmpleado(String idEmpleado) throws OurException {
        comprobarError();
        for (int i = empleados.size() - 1; i >= 0; i--) {
            if (empleados.get(i).getId().equals(idEmpleado)) {
                empleados.remove(i);
            }
        }
    }

    @Override
    public List<Sucursal> obtenerSucursales() throws OurException {
        comprobarError();
        return sucursales;
    }

    @Override
    public void insertarSucursal(Sucursal sucursal) throws OurException {
        comprobarError();
        sucursal.setId(nuevoId());
        sucursales.add(sucursal);
    }

    @Override
    public void actualizarSucursal(Sucursal sucursal) throws OurException {
        comprobarError();
    }

    @Override
    public void borrarSucursal(String idSucursal) throws OurException {
        comprobarError();
        for (Empleado e : empleados) {
            if (idSucursal.equals(e.getIdSucursal())) {
                throw new OurException(ErrorMessages.SUCURSAL_CON_EMPLEADOS);
            }
        }
        for (int i = sucursales.size() - 1; i >= 0; i--) {
            if (sucursales.get(i).getId().equals(idSucursal)) {
                sucursales.remove(i);
            }
        }
    }

    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        comprobarError();
        List<Valoracion> resultado = new ArrayList<>();
        for (Valoracion v : valoraciones) {
            if (v.getIdCliente().equals(idCliente)) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        comprobarError();
        valoracion.setId(nuevoId());
        valoraciones.add(valoracion);
    }

    @Override
    public void actualizarValoracion(Valoracion valoracion) throws OurException {
        comprobarError();
    }

    @Override
    public void borrarValoracion(String idValoracion) throws OurException {
        comprobarError();
        for (int i = valoraciones.size() - 1; i >= 0; i--) {
            if (valoraciones.get(i).getId().equals(idValoracion)) {
                valoraciones.remove(i);
            }
        }
    }
}
