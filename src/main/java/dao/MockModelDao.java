package dao;

import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import modelo.Administrador;
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
 * DAO "falso" para los tests: guarda los datos en listas en memoria en vez de en MongoDB.
 * Usuarios de prueba (contraseña "abcd"): cliente, empleado y admin.
 */
public class MockModelDao implements DAO {

    public static final String ERROR_SIMULADO = "Error simulado en la base de datos";

    // Si error = true, todos los métodos lanzan una excepción (para probar los avisos de error)
    private boolean error = false;
    private int contadorIds = 0;

    private List<Cliente> clientes = new ArrayList<>();
    private List<Empleado> empleados = new ArrayList<>();
    private List<Administrador> administradores = new ArrayList<>();
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Contrato> contratos = new ArrayList<>();
    private List<TicketSoporte> tickets = new ArrayList<>();
    private List<Valoracion> valoraciones = new ArrayList<>();

    public MockModelDao() {
        clientes.add(new Cliente(nuevoId(), "Pedro", "cliente", "abcd", "pedro@gmail.com", "",
                "12345678Z", "666666666", "calle abando 2"));
        empleados.add(new Empleado(nuevoId(), "Pablo", "empleado", "abcd", "pablo@gmail.com", "", null));
        administradores.add(new Administrador(nuevoId(), "Aitor", "admin", "abcd", "aitor@gmail.com", ""));
        tarifas.add(new Tarifa(nuevoId(), "Onda Fibra 600", 40, "600 MB simétricos", "Fibra", true));
        tarifas.add(new Tarifa(nuevoId(), "Onda Móvil 30", 15, "30 GB", "Móvil", true));
    }

    public void setError(boolean error) {
        this.error = error;
    }

    // Para que los tests puedan mirar qué se ha guardado
    public List<Contrato> getContratos() {
        return contratos;
    }

    public List<TicketSoporte> getTickets() {
        return tickets;
    }

    private void comprobarError() throws OurException {
        if (error) {
            throw new OurException(ERROR_SIMULADO);
        }
    }

    // En MongoDB el _id lo pone la base de datos; aquí se inventa uno: id1, id2, id3...
    private String nuevoId() {
        contadorIds++;
        return "id" + contadorIds;
    }

    private Cliente buscarCliente(String idCliente) {
        for (Cliente c : clientes) {
            if (c.getId().equals(idCliente)) {
                return c;
            }
        }
        return null;
    }

    private Contrato buscarContrato(String idContrato) {
        for (Contrato c : contratos) {
            if (c.getId().equals(idContrato)) {
                return c;
            }
        }
        return null;
    }

    // Mira si otra persona ya usa ese nombre de usuario (idPropio = null al dar de alta)
    private boolean nombreUsuarioOcupado(String nombreUsuario, String idPropio) {
        List<Usuario> todos = new ArrayList<>();
        todos.addAll(clientes);
        todos.addAll(empleados);
        todos.addAll(administradores);
        for (Usuario u : todos) {
            if (u.getNombreUsuario().equals(nombreUsuario) && !u.getId().equals(idPropio)) {
                return true;
            }
        }
        return false;
    }

    // ---------- LOGIN Y CLIENTES ----------

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
        for (Cliente c : clientes) {
            if (c.getDni().equals(cliente.getDni()) && !c.getId().equals(cliente.getId())) {
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
        }
        // Se cambia el cliente guardado por el modificado
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId().equals(cliente.getId())) {
                clientes.set(i, cliente);
            }
        }
    }

    // ---------- TARIFAS Y CONTRATOS ----------

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
    public List<Contrato> obtenerContratosPorEstado(EstadoContrato estado) throws OurException {
        comprobarError();
        List<Contrato> resultado = new ArrayList<>();
        for (Contrato c : contratos) {
            if (c.getEstado() == estado) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public void insertarContrato(Contrato contrato) throws OurException {
        comprobarError();
        contrato.setId(nuevoId());
        Cliente cliente = buscarCliente(contrato.getIdCliente());
        if (cliente != null) {
            contrato.setNombreCliente(cliente.getNombre());
            contrato.setDniCliente(cliente.getDni());
        }
        contratos.add(contrato);
    }

    @Override
    public boolean borrarPeticion(String idCliente, String idTarifa) throws OurException {
        comprobarError();
        for (int i = 0; i < contratos.size(); i++) {
            Contrato c = contratos.get(i);
            if (c.getIdCliente().equals(idCliente) && c.getIdTarifa().equals(idTarifa)
                    && c.getEstado() == EstadoContrato.PENDIENTE) {
                contratos.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        comprobarError();
        Contrato contrato = buscarContrato(idContrato);
        if (contrato == null) {
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
        contrato.setEstado(EstadoContrato.BAJA_PENDIENTE);
    }

    @Override
    public void validarContrato(String idContrato, String idEmpleado) throws OurException {
        comprobarError();
        Contrato contrato = buscarContrato(idContrato);
        if (contrato == null) {
            throw new OurException(ErrorMessages.VALIDAR_CONTRATO);
        }
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setIdEmpleado(idEmpleado);
        Cliente cliente = buscarCliente(contrato.getIdCliente());
        if (cliente != null) {
            cliente.setDadoDeBaja(false);
        }
    }

    @Override
    public List<Cliente> obtenerClientesConBajaPendiente() throws OurException {
        comprobarError();
        List<Cliente> resultado = new ArrayList<>();
        for (Contrato c : contratos) {
            if (c.getEstado() == EstadoContrato.BAJA_PENDIENTE) {
                Cliente cliente = buscarCliente(c.getIdCliente());
                if (cliente != null && !resultado.contains(cliente)) {
                    resultado.add(cliente);
                }
            }
        }
        return resultado;
    }

    @Override
    public void aprobarBaja(String idCliente, String idEmpleado) throws OurException {
        comprobarError();
        boolean tieneContratos = false;
        for (Contrato c : contratos) {
            if (c.getIdCliente().equals(idCliente)) {
                if (c.getEstado() == EstadoContrato.BAJA_PENDIENTE) {
                    c.setEstado(EstadoContrato.CANCELADO);
                    c.setIdEmpleado(idEmpleado);
                }
                if (c.getEstado() != EstadoContrato.CANCELADO) {
                    tieneContratos = true;
                }
            }
        }
        Cliente cliente = buscarCliente(idCliente);
        if (cliente != null && !tieneContratos) {
            cliente.setDadoDeBaja(true);
        }
    }

    // ---------- TICKETS ----------

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
        Cliente cliente = buscarCliente(ticket.getIdCliente());
        if (cliente != null) {
            ticket.setNombreCliente(cliente.getNombre());
        }
        tickets.add(ticket);
    }

    @Override
    public void resolverTicket(String idTicket, String idEmpleado) throws OurException {
        comprobarError();
        for (TicketSoporte t : tickets) {
            if (t.getId().equals(idTicket)) {
                t.setEstado("resuelto");
                t.setIdEmpleado(idEmpleado);
                return;
            }
        }
        throw new OurException(ErrorMessages.RESOLVER_TICKET);
    }

    // ---------- EMPLEADOS Y ADMINISTRADORES (los métodos vacíos son de la parte del administrador) ----------

    @Override
    public void actualizarEmpleado(Empleado empleado) throws OurException {
        comprobarError();
        if (nombreUsuarioOcupado(empleado.getNombreUsuario(), empleado.getId())) {
            throw new OurException(ErrorMessages.USER_EXISTS);
        }
        for (int i = 0; i < empleados.size(); i++) {
            if (empleados.get(i).getId().equals(empleado.getId())) {
                empleados.set(i, empleado);
            }
        }
    }

    @Override
    public List<Empleado> obtenerEmpleados() throws OurException {
        // TODO parte del administrador
        return new ArrayList<>();
    }

    @Override
    public void insertarEmpleado(Empleado empleado) throws OurException {
        // TODO parte del administrador
    }

    @Override
    public void ascenderEmpleado(Empleado empleado) throws OurException {
        // TODO parte del administrador
    }

    @Override
    public void borrarEmpleado(String idEmpleado) throws OurException {
        // TODO parte del administrador
    }

    // ---------- SUCURSALES (parte del administrador) ----------

    @Override
    public List<Sucursal> obtenerSucursales() throws OurException {
        // TODO parte del administrador
        return new ArrayList<>();
    }

    @Override
    public void insertarSucursal(Sucursal sucursal) throws OurException {
        // TODO parte del administrador
    }

    @Override
    public void actualizarSucursal(Sucursal sucursal) throws OurException {
        // TODO parte del administrador
    }

    @Override
    public void borrarSucursal(String idSucursal) throws OurException {
        // TODO parte del administrador
    }

    // ---------- VALORACIONES ----------

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
        // La valoración de la lista es el mismo objeto que se ha modificado en la pantalla: no hay que hacer nada más
    }

    @Override
    public void borrarValoracion(String idValoracion) throws OurException {
        comprobarError();
        for (int i = 0; i < valoraciones.size(); i++) {
            if (valoraciones.get(i).getId().equals(idValoracion)) {
                valoraciones.remove(i);
                return;
            }
        }
        throw new OurException(ErrorMessages.SAVE_VALORACION);
    }
}
