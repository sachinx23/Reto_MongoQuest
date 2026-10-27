package dao;

import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Valoracion;
import org.bson.types.ObjectId;

/**
 * Implementación simulada (Mock) de la interfaz DAO para realizar pruebas
 * sin necesidad de conectar con una instancia activa de MongoDB.
 */
public class MockModelDao implements DAO {

    private boolean shouldThrowException = false;
    private final Cliente clientePrueba;
    private final List<Contrato> contratos = new ArrayList<>();
    private final List<TicketSoporte> tickets = new ArrayList<>();

    public MockModelDao() {
        clientePrueba = new Cliente(new ObjectId().toHexString(), "", "Sachin", "sachinx23", "sachin",
                "12345678F", "sachin@gmail.com", "123444555", "calle miribilla 12 2 izq",
                new ArrayList<String>(), new ArrayList<Contrato>(), new ArrayList<Valoracion>());
    }

    public void setShouldThrowException(boolean shouldThrow) {
        this.shouldThrowException = shouldThrow;
    }

    private void comprobarError() throws OurException {
        if (shouldThrowException) {
            throw new OurException("Error simulado en la base de datos");
        }
    }

    @Override
    public Administrador loginAdministrador(String usuario, String contrasena) throws OurException {
        comprobarError();
        return null;
    }

    @Override
    public Empleado loginEmpleado(String usuario, String contrasena) throws OurException {
        comprobarError();
        return null;
    }

    @Override
    public Cliente loginCliente(String usuario, String contrasena) throws OurException {
        comprobarError();
        if (clientePrueba.getNombreUsuario().equals(usuario) && clientePrueba.getContrasena().equals(contrasena)) {
            return clientePrueba;
        }
        return null;
    }

    @Override
    public void insertarCliente(Cliente cliente) throws OurException {
        comprobarError();
        cliente.setId(new ObjectId().toHexString());
    }

    @Override
    public void actualizarCliente(Cliente cliente) throws OurException {
        comprobarError();
    }

    @Override
    public List<Tarifa> obtenerTarifas() throws OurException {
        comprobarError();
        List<Tarifa> tarifas = new ArrayList<>();
        tarifas.add(new Tarifa(new ObjectId().toHexString(), "Onda Fibra+Móvil 600", 40, "600 MB simétricos + 50 GB móvil", "Fibra + Móvil", true));
        return tarifas;
    }

    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        comprobarError();
        return contratos;
    }

    @Override
    public void insertarContrato(Contrato contrato) throws OurException {
        comprobarError();
        contrato.setId(new ObjectId().toHexString());
        contratos.add(contrato);
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        cambiarEstado(idContrato, DAOImplementacion.ESTADO_BAJA_PENDIENTE);
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
    public void validarContrato(String idContrato, String idEmpleado) throws OurException {
        cambiarEstado(idContrato, DAOImplementacion.ESTADO_ACTIVO);
    }

    @Override
    public void aprobarBaja(Contrato contrato, String idEmpleado) throws OurException {
        cambiarEstado(contrato.getId(), DAOImplementacion.ESTADO_CANCELADO);
    }

    private void cambiarEstado(String idContrato, String estado) throws OurException {
        comprobarError();
        for (Contrato c : contratos) {
            if (c.getId().equals(idContrato)) {
                c.setEstado(estado);
            }
        }
    }

    @Override
    public List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException {
        comprobarError();
        return tickets;
    }

    @Override
    public void insertarTicket(TicketSoporte ticket) throws OurException {
        comprobarError();
        ticket.setId(new ObjectId().toHexString());
        tickets.add(ticket);
    }

    @Override
    public List<TicketSoporte> obtenerTicketsPendientes() throws OurException {
        comprobarError();
        List<TicketSoporte> resultado = new ArrayList<>();
        for (TicketSoporte t : tickets) {
            if ("pendiente".equals(t.getEstado())) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    @Override
    public void resolverTicket(String idTicket, String idEmpleado) throws OurException {
        comprobarError();
        for (TicketSoporte t : tickets) {
            if (t.getId().equals(idTicket)) {
                t.setEstado("resuelto");
            }
        }
    }
}
