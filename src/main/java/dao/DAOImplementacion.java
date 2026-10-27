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
import modelo.Sucursal;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Usuario;
import modelo.Valoracion;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class DAOImplementacion implements DAO {

    private static final String COL_ADMINISTRADORES = "Administradores";
    private static final String COL_EMPLEADOS = "Empleados";
    private static final String COL_CLIENTES = "Clientes";
    private static final String COL_TARIFAS = "Tarifas";
    private static final String COL_CONTRATOS = "Contratos";
    private static final String COL_TICKETS = "TicketsSoportes";
    private static final String COL_SUCURSALES = "Sucursales";
    private static final String COL_VALORACIONES = "Valoraciones";

    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_ACTIVO = "activo";
    public static final String ESTADO_BAJA_PENDIENTE = "baja pendiente";
    public static final String ESTADO_CANCELADO = "cancelado";

    private MongoCollection<Document> coleccion(String nombre) throws OurException {
        return MongoConnection.getDatabase().getCollection(nombre);
    }

    @Override
    public Usuario login(String nombreUsuario, String contrasena) throws OurException {
        try {
            Bson filtro = Filters.and(Filters.eq("nombreUsuario", nombreUsuario), Filters.eq("contrasena", contrasena));

            Document d = coleccion(COL_ADMINISTRADORES).find(filtro).first();
            if (d != null) {
                LogApp.info("Login de administrador: " + nombreUsuario);
                return new Administrador(id(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"),
                        texto(d, "contrasena"), texto(d, "correo"), texto(d, "cuentaBancaria"));
            }

            d = coleccion(COL_EMPLEADOS).find(filtro).first();
            if (d != null) {
                LogApp.info("Login de empleado: " + nombreUsuario);
                return documentoAEmpleado(d);
            }

            d = coleccion(COL_CLIENTES).find(filtro).first();
            if (d != null) {
                LogApp.info("Login de cliente: " + nombreUsuario);
                return documentoACliente(d);
            }

            LogApp.info("Login fallido: " + nombreUsuario);
            return null;
        } catch (MongoException e) {
            LogApp.error("Error en el login de " + nombreUsuario, e);
            throw new OurException(ErrorMessages.LOGIN);
        }
    }

    // El nombre de usuario no puede repetirse entre clientes, empleados y administradores
    private boolean nombreUsuarioOcupado(String usuario, String idExcluir) throws OurException {
        Bson filtro = Filters.eq("nombreUsuario", usuario);
        if (idExcluir != null) {
            filtro = Filters.and(filtro, Filters.ne("_id", new ObjectId(idExcluir)));
        }
        return coleccion(COL_ADMINISTRADORES).countDocuments(filtro) > 0
                || coleccion(COL_EMPLEADOS).countDocuments(filtro) > 0
                || coleccion(COL_CLIENTES).countDocuments(filtro) > 0;
    }

    @Override
    public void insertarCliente(Cliente cliente) throws OurException {
        try {
            MongoCollection<Document> clientes = coleccion(COL_CLIENTES);
            if (clientes.find(Filters.eq("DNI", cliente.getDni())).first() != null) {
                LogApp.info("Registro rechazado, DNI repetido: " + cliente.getDni());
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            if (nombreUsuarioOcupado(cliente.getNombreUsuario(), null)) {
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
            if (nombreUsuarioOcupado(cliente.getNombreUsuario(), cliente.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
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
        Cliente c = new Cliente(id(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"), texto(d, "DNI"), texto(d, "telefono"), texto(d, "direccion"));
        c.setDadoDeBaja(d.getBoolean("dadoDeBaja", false));
        return c;
    }

    @Override
    public List<Tarifa> obtenerTarifas() throws OurException {
        List<Tarifa> tarifas = new ArrayList<>();
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

    // Con $lookup se traen la tarifa y el cliente de cada contrato en una sola consulta
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
    public List<Cliente> obtenerClientesConBajaPendiente() throws OurException {
        try {
            // Ids de los clientes con alguna baja pendiente (sin repetir) y luego sus datos en una sola consulta
            List<ObjectId> ids = new ArrayList<>();
            try (MongoCursor<ObjectId> cursor = coleccion(COL_CONTRATOS)
                    .distinct("idCliente", Filters.eq("estado", ESTADO_BAJA_PENDIENTE), ObjectId.class).iterator()) {
                while (cursor.hasNext()) {
                    ids.add(cursor.next());
                }
            }
            List<Cliente> clientes = new ArrayList<>();
            try (MongoCursor<Document> cursor = coleccion(COL_CLIENTES).find(Filters.in("_id", ids)).iterator()) {
                while (cursor.hasNext()) {
                    clientes.add(documentoACliente(cursor.next()));
                }
            }
            LogApp.info("Consultados " + clientes.size() + " clientes con baja pendiente");
            return clientes;
        } catch (MongoException e) {
            LogApp.error("Error consultando clientes con baja pendiente", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void aprobarBaja(String idCliente, String idEmpleado) throws OurException {
        try {
            ObjectId id = new ObjectId(idCliente);
            long cancelados = coleccion(COL_CONTRATOS).updateMany(
                    Filters.and(Filters.eq("idCliente", id), Filters.eq("estado", ESTADO_BAJA_PENDIENTE)),
                    Updates.combine(Updates.set("estado", ESTADO_CANCELADO), Updates.set("idEmpleado", new ObjectId(idEmpleado))))
                    .getModifiedCount();
            LogApp.info("Baja aprobada por el empleado " + idEmpleado + ": " + cancelados
                    + " contratos cancelados del cliente " + idCliente);

            // Si al cliente no le queda ningún contrato en vigor, queda dado de baja (se mantiene su perfil)
            long enVigor = coleccion(COL_CONTRATOS).countDocuments(Filters.and(
                    Filters.eq("idCliente", id),
                    Filters.in("estado", ESTADO_PENDIENTE, ESTADO_ACTIVO, ESTADO_BAJA_PENDIENTE)));
            if (enVigor == 0) {
                coleccion(COL_CLIENTES).updateOne(Filters.eq("_id", id), Updates.set("dadoDeBaja", true));
                LogApp.info("Cliente " + idCliente + " dado de baja (sin contratos en vigor)");
            }
        } catch (MongoException e) {
            LogApp.error("Error aprobando la baja del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

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

    @Override
    public void actualizarEmpleado(Empleado empleado) throws OurException {
        String nombreColeccion = empleado instanceof Administrador ? COL_ADMINISTRADORES : COL_EMPLEADOS;
        try {
            if (nombreUsuarioOcupado(empleado.getNombreUsuario(), empleado.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            Bson cambios = Updates.combine(
                    Updates.set("nombre", empleado.getNombre()),
                    Updates.set("nombreUsuario", empleado.getNombreUsuario()),
                    Updates.set("contrasena", empleado.getContrasena()),
                    Updates.set("correo", empleado.getCorreo()),
                    Updates.set("cuentaBancaria", empleado.getCuentaBancaria()));
            long encontrados = coleccion(nombreColeccion)
                    .updateOne(Filters.eq("_id", new ObjectId(empleado.getId())), cambios)
                    .getMatchedCount();
            if (encontrados == 0) {
                throw new OurException(ErrorMessages.UPDATE_USER);
            }
            LogApp.info("Datos actualizados en " + nombreColeccion + ": " + empleado.getNombreUsuario()
                    + " (id " + empleado.getId() + ")");
        } catch (MongoException e) {
            LogApp.error("Error actualizando " + empleado.getNombreUsuario() + " en " + nombreColeccion, e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }

    @Override
    public List<Empleado> obtenerEmpleados() throws OurException {
        List<Empleado> empleados = new ArrayList<>();
        try (MongoCursor<Document> cursor = coleccion(COL_EMPLEADOS).find().iterator()) {
            while (cursor.hasNext()) {
                empleados.add(documentoAEmpleado(cursor.next()));
            }
            LogApp.info("Consultados " + empleados.size() + " empleados");
            return empleados;
        } catch (MongoException e) {
            LogApp.error("Error consultando empleados", e);
            throw new OurException(ErrorMessages.GET_EMPLEADOS);
        }
    }

    @Override
    public void insertarEmpleado(Empleado empleado) throws OurException {
        try {
            if (nombreUsuarioOcupado(empleado.getNombreUsuario(), null)) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            ObjectId nuevoId = new ObjectId();
            coleccion(COL_EMPLEADOS).insertOne(documentoEmpleado(nuevoId, empleado, "Empleado")
                    .append("idSucursal", empleado.getIdSucursal() != null ? new ObjectId(empleado.getIdSucursal()) : null));
            empleado.setId(nuevoId.toHexString());
            LogApp.info("Empleado dado de alta: " + empleado.getNombreUsuario() + " (id " + empleado.getId() + ")");
        } catch (MongoException e) {
            LogApp.error("Error dando de alta el empleado " + empleado.getNombreUsuario(), e);
            throw new OurException(ErrorMessages.INSERT_EMPLEADO);
        }
    }

    @Override
    public void ascenderEmpleado(Empleado empleado) throws OurException {
        ObjectId id = new ObjectId(empleado.getId());
        try {
            // Se copia a Administradores con el mismo _id y luego se borra de Empleados
            coleccion(COL_ADMINISTRADORES).insertOne(documentoEmpleado(id, empleado, "Admin"));
        } catch (MongoException e) {
            LogApp.error("Error ascendiendo al empleado " + empleado.getId(), e);
            throw new OurException(ErrorMessages.ASCENDER_EMPLEADO);
        }
        try {
            coleccion(COL_EMPLEADOS).deleteOne(Filters.eq("_id", id));
            coleccion(COL_SUCURSALES).updateMany(Filters.eq("empleado", id), Updates.pull("empleado", id));
            LogApp.info("Empleado ascendido a administrador: " + empleado.getNombreUsuario() + " (id " + empleado.getId() + ")");
        } catch (MongoException e) {
            // Deshacer el primer paso para que la persona no quede en las dos colecciones
            LogApp.error("Error ascendiendo al empleado " + empleado.getId() + ", se deshace el cambio", e);
            try {
                coleccion(COL_ADMINISTRADORES).deleteOne(Filters.eq("_id", id));
            } catch (MongoException e2) {
                LogApp.error("No se pudo deshacer el ascenso del empleado " + empleado.getId(), e2);
            }
            throw new OurException(ErrorMessages.ASCENDER_EMPLEADO);
        }
    }

    @Override
    public void borrarEmpleado(String idEmpleado) throws OurException {
        try {
            ObjectId id = new ObjectId(idEmpleado);
            long borrados = coleccion(COL_EMPLEADOS).deleteOne(Filters.eq("_id", id)).getDeletedCount();
            if (borrados == 0) {
                throw new OurException(ErrorMessages.DELETE_EMPLEADO);
            }
            coleccion(COL_SUCURSALES).updateMany(Filters.eq("empleado", id), Updates.pull("empleado", id));
            LogApp.info("Empleado borrado: id " + idEmpleado);
        } catch (MongoException e) {
            LogApp.error("Error borrando el empleado " + idEmpleado, e);
            throw new OurException(ErrorMessages.DELETE_EMPLEADO);
        }
    }

    private Empleado documentoAEmpleado(Document d) {
        return new Empleado(id(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"), id(d, "idSucursal"));
    }

    private Document documentoEmpleado(ObjectId id, Empleado empleado, String rol) {
        return new Document("_id", id)
                .append("nombre", empleado.getNombre())
                .append("nombreUsuario", empleado.getNombreUsuario())
                .append("rol", rol)
                .append("contrasena", empleado.getContrasena())
                .append("correo", empleado.getCorreo())
                .append("cuentaBancaria", empleado.getCuentaBancaria());
    }

    @Override
    public List<Sucursal> obtenerSucursales() throws OurException {
        List<Sucursal> sucursales = new ArrayList<>();
        try (MongoCursor<Document> cursor = coleccion(COL_SUCURSALES).find().iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                Document ubicacion = d.get("Ubicacion", Document.class);
                if (ubicacion == null) {
                    ubicacion = new Document();
                }
                Number cp = (Number) ubicacion.get("CodigoPostal");
                List<String> empleados = new ArrayList<>();
                List<ObjectId> ids = d.getList("empleado", ObjectId.class);
                if (ids != null) {
                    for (ObjectId idEmpleado : ids) {
                        empleados.add(idEmpleado.toHexString());
                    }
                }
                sucursales.add(new Sucursal(id(d, "_id"), texto(d, "nombre"), texto(d, "horario"), texto(d, "telefono"),
                        texto(ubicacion, "Direccion"), texto(ubicacion, "Localidad"),
                        cp != null ? cp.intValue() : 0, empleados));
            }
            LogApp.info("Consultadas " + sucursales.size() + " sucursales");
            return sucursales;
        } catch (MongoException e) {
            LogApp.error("Error consultando sucursales", e);
            throw new OurException(ErrorMessages.GET_SUCURSALES);
        }
    }

    @Override
    public void insertarSucursal(Sucursal sucursal) throws OurException {
        try {
            ObjectId nuevoId = new ObjectId();
            Document d = new Document("_id", nuevoId)
                    .append("nombre", sucursal.getNombre())
                    .append("horario", sucursal.getHorario())
                    .append("telefono", sucursal.getTelefono())
                    .append("empleado", new ArrayList<ObjectId>())
                    .append("Ubicacion", documentoUbicacion(sucursal));
            coleccion(COL_SUCURSALES).insertOne(d);
            sucursal.setId(nuevoId.toHexString());
            LogApp.info("Sucursal creada: " + sucursal.getNombre() + " (id " + sucursal.getId() + ")");
        } catch (MongoException e) {
            LogApp.error("Error creando la sucursal " + sucursal.getNombre(), e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    @Override
    public void actualizarSucursal(Sucursal sucursal) throws OurException {
        try {
            Bson cambios = Updates.combine(
                    Updates.set("nombre", sucursal.getNombre()),
                    Updates.set("horario", sucursal.getHorario()),
                    Updates.set("telefono", sucursal.getTelefono()),
                    Updates.set("Ubicacion", documentoUbicacion(sucursal)));
            long encontrados = coleccion(COL_SUCURSALES)
                    .updateOne(Filters.eq("_id", new ObjectId(sucursal.getId())), cambios)
                    .getMatchedCount();
            if (encontrados == 0) {
                throw new OurException(ErrorMessages.SAVE_SUCURSAL);
            }
            LogApp.info("Sucursal actualizada: " + sucursal.getNombre() + " (id " + sucursal.getId() + ")");
        } catch (MongoException e) {
            LogApp.error("Error actualizando la sucursal " + sucursal.getId(), e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    @Override
    public void borrarSucursal(String idSucursal) throws OurException {
        try {
            ObjectId id = new ObjectId(idSucursal);
            if (coleccion(COL_EMPLEADOS).countDocuments(Filters.eq("idSucursal", id)) > 0) {
                LogApp.info("Borrado rechazado, la sucursal " + idSucursal + " tiene empleados");
                throw new OurException(ErrorMessages.SUCURSAL_CON_EMPLEADOS);
            }
            long borrados = coleccion(COL_SUCURSALES).deleteOne(Filters.eq("_id", id)).getDeletedCount();
            if (borrados == 0) {
                throw new OurException(ErrorMessages.SAVE_SUCURSAL);
            }
            LogApp.info("Sucursal borrada: id " + idSucursal);
        } catch (MongoException e) {
            LogApp.error("Error borrando la sucursal " + idSucursal, e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    private Document documentoUbicacion(Sucursal sucursal) {
        return new Document("Direccion", sucursal.getDireccion())
                .append("Localidad", sucursal.getLocalidad())
                .append("CodigoPostal", sucursal.getCodigoPostal());
    }

    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        List<Valoracion> valoraciones = new ArrayList<>();
        Bson filtro = Filters.eq("idCliente", new ObjectId(idCliente));
        try (MongoCursor<Document> cursor = coleccion(COL_VALORACIONES).find(filtro).iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                Number puntuacion = (Number) d.get("puntuacion");
                valoraciones.add(new Valoracion(id(d, "_id"), puntuacion != null ? puntuacion.intValue() : 0,
                        texto(d, "texto"), id(d, "idCliente"), id(d, "idTarifa")));
            }
            LogApp.info("Consultadas " + valoraciones.size() + " valoraciones del cliente " + idCliente);
            return valoraciones;
        } catch (MongoException e) {
            LogApp.error("Error consultando valoraciones del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        try {
            ObjectId nuevoId = new ObjectId();
            Document d = new Document("_id", nuevoId)
                    .append("puntuacion", valoracion.getPuntuacion())
                    .append("texto", valoracion.getTexto())
                    .append("idCliente", new ObjectId(valoracion.getIdCliente()))
                    .append("idTarifa", valoracion.getIdTarifa() != null ? new ObjectId(valoracion.getIdTarifa()) : null);
            coleccion(COL_VALORACIONES).insertOne(d);
            valoracion.setId(nuevoId.toHexString());
            LogApp.info("Valoración publicada: id " + valoracion.getId() + ", cliente " + valoracion.getIdCliente()
                    + ", puntuación " + valoracion.getPuntuacion());
        } catch (MongoException e) {
            LogApp.error("Error publicando valoración del cliente " + valoracion.getIdCliente(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void actualizarValoracion(Valoracion valoracion) throws OurException {
        try {
            Bson cambios = Updates.combine(Updates.set("puntuacion", valoracion.getPuntuacion()),
                    Updates.set("texto", valoracion.getTexto()));
            long encontrados = coleccion(COL_VALORACIONES)
                    .updateOne(Filters.eq("_id", new ObjectId(valoracion.getId())), cambios)
                    .getMatchedCount();
            if (encontrados == 0) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LogApp.info("Valoración editada: id " + valoracion.getId() + ", puntuación " + valoracion.getPuntuacion());
        } catch (MongoException e) {
            LogApp.error("Error editando la valoración " + valoracion.getId(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void borrarValoracion(String idValoracion) throws OurException {
        try {
            long borrados = coleccion(COL_VALORACIONES)
                    .deleteOne(Filters.eq("_id", new ObjectId(idValoracion)))
                    .getDeletedCount();
            if (borrados == 0) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LogApp.info("Valoración borrada: id " + idValoracion);
        } catch (MongoException e) {
            LogApp.error("Error borrando la valoración " + idValoracion, e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    // Devuelve el campo como texto, o "" si no existe
    private String texto(Document d, String campo) {
        Object valor = d.get(campo);
        return valor != null ? String.valueOf(valor) : "";
    }

    // $lookup devuelve una lista: se coge el primer documento
    private Document primero(Document d, String campo) {
        List<Document> lista = d.getList(campo, Document.class);
        return lista != null && !lista.isEmpty() ? lista.get(0) : null;
    }

    // Devuelve el ObjectId como texto, o null si no existe
    private String id(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor instanceof ObjectId) {
            return ((ObjectId) valor).toHexString();
        }
        return valor != null ? String.valueOf(valor) : null;
    }
}
