package dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import log.LogApp;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Empleado;
import modelo.Tarifa;
import modelo.TicketSoporte;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Implementación del DAO contra MongoDB. Es la única clase que trabaja con
 * Documents; hacia fuera solo devuelve objetos del modelo.
 */
public class DAOImplementacion implements DAO {

    private static final String COL_ADMINISTRADORES = "Administradores";
    private static final String COL_EMPLEADOS = "Empleados";
    private static final String COL_CLIENTES = "Clientes";
    private static final String COL_TARIFAS = "Tarifas";
    private static final String COL_CONTRATOS = "Contratos";
    private static final String COL_TICKETS = "TicketSoportes";

    /** Estados de un contrato. */
    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_ACTIVO = "activo";
    public static final String ESTADO_BAJA_PENDIENTE = "baja pendiente";
    public static final String ESTADO_CANCELADO = "cancelado";

    private MongoCollection<Document> coleccion(String nombre) throws OurException {
        return MongoConnection.getDatabase().getCollection(nombre);
    }

    // ------------------------------------------------------------ LOGIN

    @Override
    public Administrador loginAdministrador(String usuario, String contrasena) throws OurException {
        try {
            Document d = coleccion(COL_ADMINISTRADORES).find(filtroLogin(usuario, contrasena)).first();
            if (d == null) {
                return null;
            }
            LogApp.info("Login de administrador: " + usuario);
            return new Administrador(id(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"),
                    texto(d, "contrasena"), texto(d, "correo"), texto(d, "cuentaBancaria"));
        } catch (MongoException e) {
            LogApp.error("Error en login de administrador " + usuario, e);
            throw new OurException(ErrorMessages.LOGIN);
        }
    }

    @Override
    public Empleado loginEmpleado(String usuario, String contrasena) throws OurException {
        try {
            Document d = coleccion(COL_EMPLEADOS).find(filtroLogin(usuario, contrasena)).first();
            if (d == null) {
                return null;
            }
            LogApp.info("Login de empleado: " + usuario);
            return new Empleado(id(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "rol"),
                    texto(d, "contrasena"), texto(d, "correo"), texto(d, "cuentaBancaria"), id(d, "idSucursal"));
        } catch (MongoException e) {
            LogApp.error("Error en login de empleado " + usuario, e);
            throw new OurException(ErrorMessages.LOGIN);
        }
    }

    @Override
    public Cliente loginCliente(String usuario, String contrasena) throws OurException {
        try {
            Document d = coleccion(COL_CLIENTES).find(filtroLogin(usuario, contrasena)).first();
            if (d == null) {
                LogApp.info("Login fallido: " + usuario);
                return null;
            }
            LogApp.info("Login de cliente: " + usuario);
            return documentoACliente(d);
        } catch (MongoException e) {
            LogApp.error("Error en login de cliente " + usuario, e);
            throw new OurException(ErrorMessages.LOGIN);
        }
    }

    private Bson filtroLogin(String usuario, String contrasena) {
        return Filters.and(Filters.eq("nombreUsuario", usuario), Filters.eq("contrasena", contrasena));
    }

    // ------------------------------------------------------------ CLIENTES

    @Override
    public void insertarCliente(Cliente cliente) throws OurException {
        try {
            MongoCollection<Document> clientes = coleccion(COL_CLIENTES);
            if (clientes.find(Filters.eq("DNI", cliente.getDni())).first() != null) {
                LogApp.info("Registro rechazado, DNI repetido: " + cliente.getDni());
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            if (clientes.find(Filters.eq("nombreUsuario", cliente.getNombreUsuario())).first() != null) {
                LogApp.info("Registro rechazado, usuario repetido: " + cliente.getNombreUsuario());
                throw new OurException(ErrorMessages.USER_EXISTS);
            }

            ObjectId nuevoId = new ObjectId();
            Document d = new Document("_id", nuevoId)
                    .append("nombreUsuario", cliente.getNombreUsuario())
                    .append("nombre", cliente.getNombre())
                    .append("contrasena", cliente.getContrasena())
                    .append("DNI", cliente.getDni())
                    .append("correo", cliente.getCorreo())
                    .append("telefono", cliente.getTelefono())
                    .append("direccion", cliente.getDireccion())
                    .append("cuentaBancaria", cliente.getCuentaBancaria())
                    .append("dadoDeBaja", false);
            clientes.insertOne(d);
            cliente.setId(nuevoId.toHexString());
            LogApp.info("Cliente registrado: " + cliente.getNombreUsuario() + " (id " + cliente.getId() + ")");
        } catch (MongoException e) {
            LogApp.error("Error registrando cliente " + cliente.getNombreUsuario(), e);
            throw new OurException(ErrorMessages.REGISTER_USER);
        }
    }

    @Override
    public void actualizarCliente(Cliente cliente) throws OurException {
        try {
            Bson cambios = Updates.combine(
                    Updates.set("nombre", cliente.getNombre()),
                    Updates.set("nombreUsuario", cliente.getNombreUsuario()),
                    Updates.set("DNI", cliente.getDni()),
                    Updates.set("correo", cliente.getCorreo()),
                    Updates.set("direccion", cliente.getDireccion()),
                    Updates.set("telefono", cliente.getTelefono()),
                    Updates.set("cuentaBancaria", cliente.getCuentaBancaria()));
            long modificados = coleccion(COL_CLIENTES)
                    .updateOne(Filters.eq("_id", new ObjectId(cliente.getId())), cambios)
                    .getMatchedCount();
            if (modificados == 0) {
                throw new OurException(ErrorMessages.UPDATE_USER);
            }
            LogApp.info("Cliente actualizado: " + cliente.getNombreUsuario() + " (id " + cliente.getId() + ")");
        } catch (MongoException e) {
            LogApp.error("Error actualizando cliente " + cliente.getId(), e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }

    private Cliente documentoACliente(Document d) {
        Cliente c = new Cliente(id(d, "_id"), texto(d, "cuentaBancaria"), texto(d, "nombre"), texto(d, "nombreUsuario"),
                texto(d, "contrasena"), texto(d, "DNI"), texto(d, "correo"), texto(d, "telefono"),
                texto(d, "direccion"), new ArrayList<String>(), new ArrayList<Contrato>(), new ArrayList<modelo.Valoracion>());
        c.setDadoDeBaja(d.getBoolean("dadoDeBaja", false));
        return c;
    }

    // ------------------------------------------------------------ TARIFAS

    @Override
    public List<Tarifa> obtenerTarifas() throws OurException {
        List<Tarifa> tarifas = new ArrayList<>();
        // Las tarifas sin el campo "activa" también se muestran
        Bson filtro = Filters.ne("activa", false);
        try (MongoCursor<Document> cursor = coleccion(COL_TARIFAS).find(filtro).iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                Number precio = (Number) d.get("precio");
                tarifas.add(new Tarifa(id(d, "_id"), texto(d, "nombreTarifa"),
                        precio != null ? precio.doubleValue() : 0,
                        texto(d, "detalles"), texto(d, "tipotarifa"), d.getBoolean("activa", true)));
            }
            LogApp.info("Consultadas " + tarifas.size() + " tarifas");
            return tarifas;
        } catch (MongoException e) {
            LogApp.error("Error consultando tarifas", e);
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
    }

    // ------------------------------------------------------------ CONTRATOS

    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(Filters.eq("idCliente", new ObjectId(idCliente)));
            LogApp.info("Consultados " + contratos.size() + " contratos del cliente " + idCliente);
            return contratos;
        } catch (MongoException e) {
            LogApp.error("Error consultando contratos del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public List<Contrato> obtenerContratosPorEstado(String estado) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(Filters.eq("estado", estado));
            LogApp.info("Consultados " + contratos.size() + " contratos en estado " + estado);
            return contratos;
        } catch (MongoException e) {
            LogApp.error("Error consultando contratos en estado " + estado, e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    /**
     * Busca contratos y, con $lookup, les une los datos de su tarifa y de su
     * cliente en una sola consulta (en vez de una consulta por contrato).
     */
    private List<Contrato> buscarContratos(Bson filtro) throws OurException {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(filtro),
                Aggregates.lookup(COL_TARIFAS, "idTarifa", "_id", "tarifa"),
                Aggregates.lookup(COL_CLIENTES, "idCliente", "_id", "cliente"));
        List<Contrato> contratos = new ArrayList<>();
        try (MongoCursor<Document> cursor = coleccion(COL_CONTRATOS).aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                Contrato c = new Contrato(id(d, "_id"), texto(d, "telefono"), texto(d, "estado"),
                        id(d, "idCliente"), id(d, "idTarifa"), id(d, "idEmpleado"),
                        texto(d, "fechaComienzo"), texto(d, "fechaFinalizacion"));
                Document tarifa = primero(d, "tarifa");
                if (tarifa != null) {
                    c.setNombreTarifa(texto(tarifa, "nombreTarifa"));
                    Number precio = (Number) tarifa.get("precio");
                    c.setPrecio(precio != null ? precio.doubleValue() : 0);
                }
                Document cliente = primero(d, "cliente");
                if (cliente != null) {
                    c.setNombreCliente(texto(cliente, "nombre"));
                    c.setDniCliente(texto(cliente, "DNI"));
                }
                contratos.add(c);
            }
        }
        return contratos;
    }

    @Override
    public void insertarContrato(Contrato contrato) throws OurException {
        try {
            ObjectId nuevoId = new ObjectId();
            Document d = new Document("_id", nuevoId)
                    .append("telefono", contrato.getTelefono())
                    .append("estado", contrato.getEstado())
                    .append("idCliente", new ObjectId(contrato.getIdCliente()))
                    .append("idTarifa", new ObjectId(contrato.getIdTarifa()))
                    .append("idEmpleado", contrato.getIdEmpleado() != null ? new ObjectId(contrato.getIdEmpleado()) : null)
                    .append("fechaComienzo", contrato.getFechaComienzo())
                    .append("fechaFinalizacion", contrato.getFechaFinalizacion());
            coleccion(COL_CONTRATOS).insertOne(d);
            contrato.setId(nuevoId.toHexString());
            LogApp.info("Contrato creado: id " + contrato.getId() + ", cliente " + contrato.getIdCliente()
                    + ", tarifa " + contrato.getIdTarifa() + ", estado " + contrato.getEstado());
        } catch (MongoException e) {
            LogApp.error("Error creando contrato del cliente " + contrato.getIdCliente(), e);
            throw new OurException(ErrorMessages.INSERT_CONTRATO);
        }
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        cambiarEstadoContrato(idContrato, ESTADO_BAJA_PENDIENTE, null, ErrorMessages.BAJA_CONTRATO);
        LogApp.info("Baja solicitada para el contrato " + idContrato);
    }

    @Override
    public void validarContrato(String idContrato, String idEmpleado) throws OurException {
        cambiarEstadoContrato(idContrato, ESTADO_ACTIVO, idEmpleado, ErrorMessages.VALIDAR_CONTRATO);
        LogApp.info("Contrato " + idContrato + " validado por el empleado " + idEmpleado);
    }

    @Override
    public void aprobarBaja(Contrato contrato, String idEmpleado) throws OurException {
        cambiarEstadoContrato(contrato.getId(), ESTADO_CANCELADO, idEmpleado, ErrorMessages.BAJA_CONTRATO);
        LogApp.info("Baja del contrato " + contrato.getId() + " aprobada por el empleado " + idEmpleado);
        try {
            // Si al cliente no le queda ningún contrato en vigor, queda dado de baja (se mantiene su perfil)
            ObjectId idCliente = new ObjectId(contrato.getIdCliente());
            long enVigor = coleccion(COL_CONTRATOS).countDocuments(Filters.and(
                    Filters.eq("idCliente", idCliente),
                    Filters.in("estado", ESTADO_PENDIENTE, ESTADO_ACTIVO, ESTADO_BAJA_PENDIENTE)));
            if (enVigor == 0) {
                coleccion(COL_CLIENTES).updateOne(Filters.eq("_id", idCliente), Updates.set("dadoDeBaja", true));
                LogApp.info("Cliente " + contrato.getIdCliente() + " dado de baja (sin contratos en vigor)");
            }
        } catch (MongoException e) {
            LogApp.error("Error marcando como dado de baja al cliente " + contrato.getIdCliente(), e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    /** Cambia el estado de un contrato y, si se indica, el empleado que lo ha gestionado. */
    private void cambiarEstadoContrato(String idContrato, String estado, String idEmpleado, String mensajeError) throws OurException {
        try {
            Bson cambios = Updates.set("estado", estado);
            if (idEmpleado != null) {
                cambios = Updates.combine(cambios, Updates.set("idEmpleado", new ObjectId(idEmpleado)));
            }
            long encontrados = coleccion(COL_CONTRATOS)
                    .updateOne(Filters.eq("_id", new ObjectId(idContrato)), cambios)
                    .getMatchedCount();
            if (encontrados == 0) {
                throw new OurException(mensajeError);
            }
        } catch (MongoException e) {
            LogApp.error("Error cambiando el contrato " + idContrato + " a estado " + estado, e);
            throw new OurException(mensajeError);
        }
    }

    // ------------------------------------------------------------ TICKETS

    @Override
    public List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException {
        List<TicketSoporte> tickets = new ArrayList<>();
        Bson filtro = Filters.eq("idCliente", new ObjectId(idCliente));
        try (MongoCursor<Document> cursor = coleccion(COL_TICKETS).find(filtro).iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                tickets.add(new TicketSoporte(id(d, "_id"), texto(d, "asunto"), texto(d, "descripcion"),
                        texto(d, "fechaCreacion"), texto(d, "estado"), id(d, "idCliente"), id(d, "idEmpleado")));
            }
            LogApp.info("Consultados " + tickets.size() + " tickets del cliente " + idCliente);
            return tickets;
        } catch (MongoException e) {
            LogApp.error("Error consultando tickets del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
    }

    @Override
    public void insertarTicket(TicketSoporte ticket) throws OurException {
        try {
            ObjectId nuevoId = new ObjectId();
            Document d = new Document("_id", nuevoId)
                    .append("asunto", ticket.getAsunto())
                    .append("descripcion", ticket.getDescripcion())
                    .append("fechaCreacion", ticket.getFechaCreacion())
                    .append("estado", ticket.getEstado())
                    .append("idCliente", new ObjectId(ticket.getIdCliente()))
                    .append("idEmpleado", null);
            coleccion(COL_TICKETS).insertOne(d);
            ticket.setId(nuevoId.toHexString());
            LogApp.info("Ticket creado: id " + ticket.getId() + ", cliente " + ticket.getIdCliente()
                    + ", asunto \"" + ticket.getAsunto() + "\"");
        } catch (MongoException e) {
            LogApp.error("Error creando ticket del cliente " + ticket.getIdCliente(), e);
            throw new OurException(ErrorMessages.INSERT_TICKET);
        }
    }

    @Override
    public List<TicketSoporte> obtenerTicketsPendientes() throws OurException {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.eq("estado", "pendiente")),
                Aggregates.lookup(COL_CLIENTES, "idCliente", "_id", "cliente"));
        List<TicketSoporte> tickets = new ArrayList<>();
        try (MongoCursor<Document> cursor = coleccion(COL_TICKETS).aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                TicketSoporte t = new TicketSoporte(id(d, "_id"), texto(d, "asunto"), texto(d, "descripcion"),
                        texto(d, "fechaCreacion"), texto(d, "estado"), id(d, "idCliente"), id(d, "idEmpleado"));
                Document cliente = primero(d, "cliente");
                if (cliente != null) {
                    t.setNombreCliente(texto(cliente, "nombre"));
                }
                tickets.add(t);
            }
            LogApp.info("Consultados " + tickets.size() + " tickets pendientes");
            return tickets;
        } catch (MongoException e) {
            LogApp.error("Error consultando tickets pendientes", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
    }

    @Override
    public void resolverTicket(String idTicket, String idEmpleado) throws OurException {
        try {
            Bson cambios = Updates.combine(Updates.set("estado", "resuelto"),
                    Updates.set("idEmpleado", new ObjectId(idEmpleado)));
            long encontrados = coleccion(COL_TICKETS)
                    .updateOne(Filters.eq("_id", new ObjectId(idTicket)), cambios)
                    .getMatchedCount();
            if (encontrados == 0) {
                throw new OurException(ErrorMessages.RESOLVER_TICKET);
            }
            LogApp.info("Ticket " + idTicket + " resuelto por el empleado " + idEmpleado);
        } catch (MongoException e) {
            LogApp.error("Error resolviendo el ticket " + idTicket, e);
            throw new OurException(ErrorMessages.RESOLVER_TICKET);
        }
    }

    // ------------------------------------------------------------ AYUDANTES

    /** Devuelve el campo como texto (vale para String y números), o "" si no existe. */
    private String texto(Document d, String campo) {
        Object valor = d.get(campo);
        return valor != null ? String.valueOf(valor) : "";
    }

    /** Devuelve el primer documento del array que crea un $lookup, o null si está vacío. */
    private Document primero(Document d, String campo) {
        List<Document> lista = d.getList(campo, Document.class);
        return lista != null && !lista.isEmpty() ? lista.get(0) : null;
    }

    /** Devuelve un ObjectId como String hexadecimal, o null si no existe. */
    private String id(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor instanceof ObjectId) {
            return ((ObjectId) valor).toHexString();
        }
        return valor != null ? String.valueOf(valor) : null;
    }
}
