package daoempleado.implementacion;

import com.mongodb.MongoException;
import dao.implementacion.DAOImplementacion;
import daoempleado.EmpleadoDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.EstadoContrato;
import modelo.TicketSoporte;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de MongoDB de la parte de empleado: perfil, validar tarifas, bajas y tickets.
 */
public class EmpleadoDAOImplementacion extends DAOImplementacion implements EmpleadoDAO {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoDAOImplementacion.class.getName());

    @Override
    public void actualizarEmpleado(Empleado empleado) throws OurException {
        String nombreColeccion = COL_EMPLEADOS;
        if (empleado instanceof Administrador) {
            nombreColeccion = COL_ADMINISTRADORES;
        }
        try {
            if (nombreUsuarioOcupado(empleado.getNombreUsuario(), empleado.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            Document cambios = new Document();
            cambios.put("nombre", empleado.getNombre());
            cambios.put("nombreUsuario", empleado.getNombreUsuario());
            cambios.put("contrasena", empleado.getContrasena());
            cambios.put("correo", empleado.getCorreo());
            cambios.put("cuentaBancaria", empleado.getCuentaBancaria());
            if (!modificar(nombreColeccion, empleado.getId(), cambios)) {
                throw new OurException(ErrorMessages.UPDATE_USER);
            }
            LOGGER.info("Datos modificados en " + nombreColeccion + ": " + empleado.getNombreUsuario());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando " + empleado.getNombreUsuario() + " en " + nombreColeccion, e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }

    @Override
    public List<Contrato> obtenerContratosPorEstado(EstadoContrato estado) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(new Document("estado", estado.toString()));
            LOGGER.info("Consultados " + contratos.size() + " contratos en estado " + estado);
            return contratos;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando contratos en estado " + estado, e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void validarContrato(String idContrato, String idEmpleado) throws OurException {
        try {
            Document contrato = buscarPorId(COL_CONTRATOS, new ObjectId(idContrato));
            if (contrato == null) {
                throw new OurException(ErrorMessages.VALIDAR_CONTRATO);
            }
            // 1. El contrato pasa a activo
            cambiarEstadoContrato(idContrato, EstadoContrato.ACTIVO, idEmpleado, ErrorMessages.VALIDAR_CONTRATO);
            // 2. Si el cliente estaba dado de baja, al tener otra vez un contrato activo deja de estarlo
            try {
                modificar(COL_CLIENTES, idTexto(contrato, "idCliente"), new Document("dadoDeBaja", false));
            } catch (MongoException e) {
                // Si falla el paso 2, se deshace el 1: el contrato vuelve a pendiente
                cambiarEstadoContrato(idContrato, EstadoContrato.PENDIENTE, null, ErrorMessages.VALIDAR_CONTRATO);
                throw e;
            }
            LOGGER.info("Contrato " + idContrato + " validado por el empleado " + idEmpleado);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error validando el contrato " + idContrato, e);
            throw new OurException(ErrorMessages.VALIDAR_CONTRATO);
        }
    }

    @Override
    public List<Cliente> obtenerClientesConBajaPendiente() throws OurException {
        List<Cliente> clientes = new ArrayList<>();
        List<String> idsVistos = new ArrayList<>();
        try {
            for (Document contrato : coleccion(COL_CONTRATOS).find(new Document("estado", EstadoContrato.BAJA_PENDIENTE.toString()))) {
                String idCliente = idTexto(contrato, "idCliente");
                // Un cliente puede tener varias bajas pendientes: solo se añade una vez
                if (!idsVistos.contains(idCliente)) {
                    idsVistos.add(idCliente);
                    Document d = buscarPorId(COL_CLIENTES, new ObjectId(idCliente));
                    if (d != null) {
                        clientes.add(documentoACliente(d));
                    }
                }
            }
            LOGGER.info("Consultados " + clientes.size() + " clientes con baja pendiente");
            return clientes;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando las bajas pendientes", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void aprobarBaja(String idCliente, String idEmpleado) throws OurException {
        try {
            // 1. Mirar si después de la baja le quedará algún contrato (uno que no esté cancelado ni en baja pendiente)
            boolean quedanContratos = false;
            for (Contrato c : buscarContratos(new Document("idCliente", new ObjectId(idCliente)))) {
                if (c.getEstado() != EstadoContrato.CANCELADO && c.getEstado() != EstadoContrato.BAJA_PENDIENTE) {
                    quedanContratos = true;
                }
            }

            // 2. Si no le queda ninguno, el cliente queda dado de baja (se mantiene su perfil)
            if (!quedanContratos) {
                modificar(COL_CLIENTES, idCliente, new Document("dadoDeBaja", true));
            }

            // 3. Cancelar todos los contratos del cliente que están en "baja pendiente"
            Document filtro = new Document();
            filtro.put("idCliente", new ObjectId(idCliente));
            filtro.put("estado", EstadoContrato.BAJA_PENDIENTE.toString());
            Document cambios = new Document();
            cambios.put("estado", EstadoContrato.CANCELADO.toString());
            cambios.put("idEmpleado", new ObjectId(idEmpleado));
            try {
                coleccion(COL_CONTRATOS).updateMany(filtro, new Document("$set", cambios));
            } catch (MongoException e) {
                // Si falla el paso 3, se deshace el 2: el cliente deja de estar dado de baja
                if (!quedanContratos) {
                    modificar(COL_CLIENTES, idCliente, new Document("dadoDeBaja", false));
                }
                throw e;
            }
            LOGGER.info("Baja aprobada por el empleado " + idEmpleado + " del cliente " + idCliente);
            if (!quedanContratos) {
                LOGGER.info("Cliente " + idCliente + " dado de baja");
            }
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error aprobando la baja del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    @Override
    public List<TicketSoporte> obtenerTicketsPendientes() throws OurException {
        try {
            List<TicketSoporte> tickets = buscarTickets(new Document("estado", "pendiente"));
            LOGGER.info("Consultados " + tickets.size() + " tickets pendientes");
            return tickets;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando tickets pendientes", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
    }

    @Override
    public void resolverTicket(String idTicket, String idEmpleado) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("estado", "resuelto");
            cambios.put("idEmpleado", new ObjectId(idEmpleado));
            if (!modificar(COL_TICKETS, idTicket, cambios)) {
                throw new OurException(ErrorMessages.RESOLVER_TICKET);
            }
            LOGGER.info("Ticket " + idTicket + " resuelto por el empleado " + idEmpleado);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error resolviendo el ticket " + idTicket, e);
            throw new OurException(ErrorMessages.RESOLVER_TICKET);
        }
    }
}
