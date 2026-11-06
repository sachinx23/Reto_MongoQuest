package dao.implementacion;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import dao.DAO;
import dao.MongoConnection;
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
import modelo.Sucursal;
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Usuario;
import modelo.Valoracion;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Acceso a MongoDB. Cada operación se apunta en el log con LOGGER.
 * MongoDB instalado en local (un solo servidor) no permite transacciones. Por eso, cuando una operación
 * escribe en dos sitios y falla la segunda escritura, se deshace la primera para no dejar datos a medias.
 * Un Document es como un HashMap: se le meten campos con put() y se leen con get().
 * Se usa para guardar datos, para buscar (filtro) y para modificar (cambios).
 */
public class DAOImplementacion implements DAO {

    private static final String COL_ADMINISTRADORES = "Administradores";
    private static final String COL_EMPLEADOS = "Empleados";
    private static final String COL_CLIENTES = "Clientes";
    private static final String COL_TARIFAS = "Tarifas";
    private static final String COL_CONTRATOS = "Contratos";
    private static final String COL_TICKETS = "TicketsSoportes";
    private static final String COL_SUCURSALES = "Sucursales";
    private static final String COL_VALORACIONES = "Valoraciones";

    private static final Logger LOGGER = Logger.getLogger(DAOImplementacion.class.getName());

    private MongoCollection<Document> coleccion(String nombre) throws OurException {
        return MongoConnection.getDatabase().getCollection(nombre);
    }

    // ---------- LOGIN Y CLIENTES ----------

    @Override
    public Usuario login(String nombreUsuario, String contrasena) throws OurException {
        try {
            Document filtro = new Document();
            filtro.put("nombreUsuario", nombreUsuario);
            filtro.put("contrasena", contrasena);

            Document d = coleccion(COL_ADMINISTRADORES).find(filtro).first();
            if (d != null) {
                LOGGER.info("Login de administrador: " + nombreUsuario);
                return documentoAAdministrador(d);
            }

            d = coleccion(COL_EMPLEADOS).find(filtro).first();
            if (d != null) {
                LOGGER.info("Login de empleado: " + nombreUsuario);
                return documentoAEmpleado(d);
            }

            d = coleccion(COL_CLIENTES).find(filtro).first();
            if (d != null) {
                LOGGER.info("Login de cliente: " + nombreUsuario);
                return documentoACliente(d);
            }

            LOGGER.info("Login fallido: " + nombreUsuario);
            return null;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en el login de " + nombreUsuario, e);
            throw new OurException(ErrorMessages.LOGIN);
        }
    }

    // Mira si otra persona (cliente, empleado o administrador) ya usa ese nombre de usuario.
    // idPropio es el id de quien modifica sus datos, para no contarse a sí mismo (null al dar de alta).
    private boolean nombreUsuarioOcupado(String nombreUsuario, String idPropio) throws OurException {
        String[] colecciones = {COL_ADMINISTRADORES, COL_EMPLEADOS, COL_CLIENTES};
        for (String nombreColeccion : colecciones) {
            Document d = coleccion(nombreColeccion).find(new Document("nombreUsuario", nombreUsuario)).first();
            if (d != null && !idTexto(d, "_id").equals(idPropio)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void insertarCliente(Cliente cliente) throws OurException {
        try {
            if (coleccion(COL_CLIENTES).find(new Document("DNI", cliente.getDni())).first() != null) {
                LOGGER.info("Registro rechazado, DNI repetido: " + cliente.getDni());
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            if (nombreUsuarioOcupado(cliente.getNombreUsuario(), null)) {
                LOGGER.info("Registro rechazado, usuario repetido: " + cliente.getNombreUsuario());
                throw new OurException(ErrorMessages.USER_EXISTS);
            }

            Document d = new Document();
            d.put("nombreUsuario", cliente.getNombreUsuario());
            d.put("nombre", cliente.getNombre());
            d.put("contrasena", cliente.getContrasena());
            d.put("DNI", cliente.getDni());
            d.put("correo", cliente.getCorreo());
            d.put("telefono", cliente.getTelefono());
            d.put("direccion", cliente.getDireccion());
            d.put("cuentaBancaria", cliente.getCuentaBancaria());
            d.put("dadoDeBaja", false);
            d.put("ticketSoporte", new ArrayList<ObjectId>());
            coleccion(COL_CLIENTES).insertOne(d);

            // Al insertar, MongoDB le pone un _id nuevo al documento
            cliente.setId(idTexto(d, "_id"));
            LOGGER.info("Cliente registrado: " + cliente.getNombreUsuario() + " (id " + cliente.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error registrando el cliente " + cliente.getNombreUsuario(), e);
            throw new OurException(ErrorMessages.REGISTER_USER);
        }
    }

    @Override
    public void actualizarCliente(Cliente cliente) throws OurException {
        try {
            if (nombreUsuarioOcupado(cliente.getNombreUsuario(), cliente.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            // El DNI tampoco puede ser el de otro cliente
            Document otro = coleccion(COL_CLIENTES).find(new Document("DNI", cliente.getDni())).first();
            if (otro != null && !idTexto(otro, "_id").equals(cliente.getId())) {
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            Document cambios = new Document();
            cambios.put("nombre", cliente.getNombre());
            cambios.put("nombreUsuario", cliente.getNombreUsuario());
            cambios.put("DNI", cliente.getDni());
            cambios.put("correo", cliente.getCorreo());
            cambios.put("direccion", cliente.getDireccion());
            cambios.put("telefono", cliente.getTelefono());
            cambios.put("cuentaBancaria", cliente.getCuentaBancaria());

            if (!modificar(COL_CLIENTES, cliente.getId(), cambios)) {
                throw new OurException(ErrorMessages.UPDATE_USER);
            }
            LOGGER.info("Cliente modificado: " + cliente.getNombreUsuario() + " (id " + cliente.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando el cliente " + cliente.getId(), e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }

    // ---------- TARIFAS ----------

    @Override
    public List<Tarifa> obtenerTarifas() throws OurException {
        List<Tarifa> tarifas = new ArrayList<>();
        try {
            for (Document d : coleccion(COL_TARIFAS).find()) {
                // Solo se enseñan las tarifas activas (si no tiene el campo "activa", se considera activa)
                boolean activa = booleano(d, "activa", true);
                if (activa) {
                    tarifas.add(documentoATarifa(d));
                }
            }
            LOGGER.info("Consultadas " + tarifas.size() + " tarifas");
            return tarifas;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando tarifas", e);
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
    }

    // ---------- CONTRATOS ----------

    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultados " + contratos.size() + " contratos del cliente " + idCliente);
            return contratos;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando contratos del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
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

    private List<Contrato> buscarContratos(Document filtro) throws OurException {
        List<Contrato> contratos = new ArrayList<>();
        for (Document d : coleccion(COL_CONTRATOS).find(filtro)) {
            Contrato c = new Contrato(idTexto(d, "_id"), texto(d, "telefono"), EstadoContrato.desdeTexto(texto(d, "estado")),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"), idTexto(d, "idEmpleado"),
                    texto(d, "fechaComienzo"), texto(d, "fechaFinalizacion"));

            // La tarifa está embebida dentro del contrato (se comprueba que de verdad sea un documento)
            Object tarifa = d.get("tarifa");
            if (tarifa instanceof Document) {
                c.setNombreTarifa(texto((Document) tarifa, "nombreTarifa"));
                c.setPrecio(numero((Document) tarifa, "precio"));
            }
            // El nombre y el DNI del cliente se buscan en su colección
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                c.setNombreCliente(texto(cliente, "nombre"));
                c.setDniCliente(texto(cliente, "DNI"));
            }
            contratos.add(c);
        }
        return contratos;
    }

    @Override
    public void insertarContrato(Contrato contrato) throws OurException {
        try {
            Document d = new Document();
            d.put("telefono", contrato.getTelefono());
            d.put("estado", contrato.getEstado().toString());
            d.put("idCliente", new ObjectId(contrato.getIdCliente()));
            d.put("idTarifa", new ObjectId(contrato.getIdTarifa()));
            d.put("idEmpleado", aObjectId(contrato.getIdEmpleado()));

            // Información embebida: los datos de la tarifa se guardan dentro del contrato
            Document tarifa = new Document();
            tarifa.put("nombreTarifa", contrato.getNombreTarifa());
            tarifa.put("precio", contrato.getPrecio());
            d.put("tarifa", tarifa);

            d.put("fechaComienzo", contrato.getFechaComienzo());
            d.put("fechaFinalizacion", contrato.getFechaFinalizacion());
            coleccion(COL_CONTRATOS).insertOne(d);

            contrato.setId(idTexto(d, "_id"));
            LOGGER.info("Contrato creado: id " + contrato.getId() + ", cliente " + contrato.getIdCliente()
                    + ", tarifa " + contrato.getIdTarifa() + ", estado " + contrato.getEstado());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error creando un contrato del cliente " + contrato.getIdCliente(), e);
            throw new OurException(ErrorMessages.INSERT_CONTRATO);
        }
    }

    @Override
    public boolean borrarPeticion(String idCliente, String idTarifa) throws OurException {
        try {
            Document filtro = new Document();
            filtro.put("idCliente", new ObjectId(idCliente));
            filtro.put("idTarifa", new ObjectId(idTarifa));
            filtro.put("estado", EstadoContrato.PENDIENTE.toString());
            long borrados = coleccion(COL_CONTRATOS).deleteOne(filtro).getDeletedCount();
            LOGGER.info("Petición de tarifa borrada: cliente " + idCliente + ", tarifa " + idTarifa + ", borrados " + borrados);
            return borrados > 0;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la petición del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        cambiarEstadoContrato(idContrato, EstadoContrato.BAJA_PENDIENTE, null, ErrorMessages.BAJA_CONTRATO);
        LOGGER.info("Baja solicitada del contrato " + idContrato);
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

    private void cambiarEstadoContrato(String idContrato, EstadoContrato estado, String idEmpleado, String mensajeError) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("estado", estado.toString());
            if (idEmpleado != null) {
                cambios.put("idEmpleado", new ObjectId(idEmpleado));
            }
            if (!modificar(COL_CONTRATOS, idContrato, cambios)) {
                throw new OurException(mensajeError);
            }
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error cambiando el contrato " + idContrato + " a " + estado, e);
            throw new OurException(mensajeError);
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

    // ---------- TICKETS ----------

    @Override
    public List<TicketSoporte> obtenerTicketsCliente(String idCliente) throws OurException {
        try {
            List<TicketSoporte> tickets = buscarTickets(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultados " + tickets.size() + " tickets del cliente " + idCliente);
            return tickets;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando tickets del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_TICKETS);
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

    private List<TicketSoporte> buscarTickets(Document filtro) throws OurException {
        List<TicketSoporte> tickets = new ArrayList<>();
        for (Document d : coleccion(COL_TICKETS).find(filtro)) {
            TicketSoporte t = new TicketSoporte(idTexto(d, "_id"), texto(d, "asunto"), texto(d, "descripcion"),
                    texto(d, "fechaCreacion"), texto(d, "estado"), idTexto(d, "idCliente"), idTexto(d, "idEmpleado"));
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                t.setNombreCliente(texto(cliente, "nombre"));
            }
            tickets.add(t);
        }
        return tickets;
    }

    @Override
    public void insertarTicket(TicketSoporte ticket) throws OurException {
        try {
            Document d = new Document();
            d.put("asunto", ticket.getAsunto());
            d.put("descripcion", ticket.getDescripcion());
            d.put("fechaCreacion", ticket.getFechaCreacion());
            d.put("estado", ticket.getEstado());
            d.put("idCliente", new ObjectId(ticket.getIdCliente()));
            d.put("idEmpleado", null);
            // 1. Guardar el ticket
            coleccion(COL_TICKETS).insertOne(d);

            // 2. Array de _id: el cliente guarda los _id de sus tickets en "ticketSoporte".
            // "$push" añade un elemento al final del array
            Document filtroCliente = new Document("_id", new ObjectId(ticket.getIdCliente()));
            Document nuevoTicket = new Document("ticketSoporte", d.get("_id"));
            try {
                coleccion(COL_CLIENTES).updateOne(filtroCliente, new Document("$push", nuevoTicket));
            } catch (MongoException e) {
                // Si falla el paso 2, se deshace el 1: se borra el ticket
                coleccion(COL_TICKETS).deleteOne(new Document("_id", d.get("_id")));
                throw e;
            }
            ticket.setId(idTexto(d, "_id"));
            LOGGER.info("Ticket creado: id " + ticket.getId() + ", cliente " + ticket.getIdCliente() + ", asunto \"" + ticket.getAsunto() + "\"");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error creando un ticket del cliente " + ticket.getIdCliente(), e);
            throw new OurException(ErrorMessages.INSERT_TICKET);
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

    // ---------- EMPLEADOS Y ADMINISTRADORES ----------

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
    public List<Empleado> obtenerEmpleados() throws OurException {
        List<Empleado> plantilla = new ArrayList<>();
        try {
            for (Document d : coleccion(COL_ADMINISTRADORES).find()) {
                plantilla.add(documentoAAdministrador(d));
            }
            for (Document d : coleccion(COL_EMPLEADOS).find()) {
                plantilla.add(documentoAEmpleado(d));
            }
            LOGGER.info("Consultada la plantilla: " + plantilla.size() + " personas");
            return plantilla;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando la plantilla", e);
            throw new OurException(ErrorMessages.GET_EMPLEADOS);
        }
    }

    @Override
    public void insertarEmpleado(Empleado empleado) throws OurException {
        try {
            if (nombreUsuarioOcupado(empleado.getNombreUsuario(), null)) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            Document d = new Document();
            d.put("nombre", empleado.getNombre());
            d.put("nombreUsuario", empleado.getNombreUsuario());
            d.put("rol", "Empleado");
            d.put("contrasena", empleado.getContrasena());
            d.put("correo", empleado.getCorreo());
            d.put("cuentaBancaria", empleado.getCuentaBancaria());
            d.put("idSucursal", aObjectId(empleado.getIdSucursal()));
            coleccion(COL_EMPLEADOS).insertOne(d);
            empleado.setId(idTexto(d, "_id"));
            LOGGER.info("Empleado dado de alta: " + empleado.getNombreUsuario() + " (id " + empleado.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error dando de alta al empleado " + empleado.getNombreUsuario(), e);
            throw new OurException(ErrorMessages.INSERT_EMPLEADO);
        }
    }

    @Override
    public void ascenderEmpleado(Empleado empleado) throws OurException {
        if (empleado instanceof Administrador) {
            throw new OurException(ErrorMessages.YA_ES_ADMIN);
        }
        try {
            Document d = buscarPorId(COL_EMPLEADOS, new ObjectId(empleado.getId()));
            if (d == null) {
                throw new OurException(ErrorMessages.ASCENDER_EMPLEADO);
            }
            // 1. Se copia a Administradores con el mismo _id (un administrador no tiene sucursal)
            d.remove("idSucursal");
            d.put("rol", "Admin");
            coleccion(COL_ADMINISTRADORES).insertOne(d);
            // 2. Se borra de Empleados. Si falla, se deshace el paso 1
            try {
                borrar(COL_EMPLEADOS, empleado.getId());
            } catch (MongoException e) {
                borrar(COL_ADMINISTRADORES, empleado.getId());
                throw e;
            }
            LOGGER.info("Empleado ascendido a administrador: " + empleado.getNombreUsuario());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error ascendiendo al empleado " + empleado.getNombreUsuario(), e);
            throw new OurException(ErrorMessages.ASCENDER_EMPLEADO);
        }
    }

    @Override
    public void borrarEmpleado(String idEmpleado) throws OurException {
        try {
            if (!borrar(COL_EMPLEADOS, idEmpleado)) {
                throw new OurException(ErrorMessages.DELETE_EMPLEADO);
            }
            LOGGER.info("Empleado borrado: id " + idEmpleado);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando al empleado " + idEmpleado, e);
            throw new OurException(ErrorMessages.DELETE_EMPLEADO);
        }
    }

    @Override
    public List<Cliente> obtenerClientes() throws OurException {
        List<Cliente> clientes = new ArrayList<>();
        try {
            for (Document d : coleccion(COL_CLIENTES).find()) {
                clientes.add(documentoACliente(d));
            }
            LOGGER.info("Consultados " + clientes.size() + " clientes");
            return clientes;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando los clientes", e);
            throw new OurException(ErrorMessages.GET_CLIENTES);
        }
    }

    @Override
    public void borrarDatosSensiblesCliente(String idCliente) throws OurException {
        try {
            Document d = buscarPorId(COL_CLIENTES, new ObjectId(idCliente));
            if (d == null) {
                throw new OurException(ErrorMessages.DATOS_SENSIBLES);
            }
            // Solo de clientes que ya no tienen contratos
            if (!booleano(d, "dadoDeBaja", false)) {
                throw new OurException(ErrorMessages.CLIENTE_ACTIVO);
            }
            Document cambios = new Document();
            cambios.put("cuentaBancaria", "");
            cambios.put("telefono", "");
            cambios.put("direccion", "");
            modificar(COL_CLIENTES, idCliente, cambios);
            LOGGER.info("Borrados los datos sensibles del cliente " + idCliente);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando los datos del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.DATOS_SENSIBLES);
        }
    }

    // ---------- SUCURSALES ----------

    @Override
    public List<Sucursal> obtenerSucursales() throws OurException {
        List<Sucursal> sucursales = new ArrayList<>();
        try {
            for (Document d : coleccion(COL_SUCURSALES).find()) {
                // La ubicación está embebida dentro de la sucursal
                Document ubicacion = new Document();
                Object u = d.get("Ubicacion");
                if (u instanceof Document) {
                    ubicacion = (Document) u;
                }
                sucursales.add(new Sucursal(idTexto(d, "_id"), texto(d, "nombre"), texto(d, "horario"), texto(d, "telefono"),
                        texto(ubicacion, "Direccion"), texto(ubicacion, "Localidad"), (int) numero(ubicacion, "CodigoPostal")));
            }
            LOGGER.info("Consultadas " + sucursales.size() + " sucursales");
            return sucursales;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando las sucursales", e);
            throw new OurException(ErrorMessages.GET_SUCURSALES);
        }
    }

    @Override
    public void insertarSucursal(Sucursal sucursal) throws OurException {
        try {
            Document d = sucursalADocumento(sucursal);
            coleccion(COL_SUCURSALES).insertOne(d);
            sucursal.setId(idTexto(d, "_id"));
            LOGGER.info("Sucursal creada: " + sucursal.getNombre() + " (id " + sucursal.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error creando la sucursal " + sucursal.getNombre(), e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    @Override
    public void actualizarSucursal(Sucursal sucursal) throws OurException {
        try {
            if (!modificar(COL_SUCURSALES, sucursal.getId(), sucursalADocumento(sucursal))) {
                throw new OurException(ErrorMessages.SAVE_SUCURSAL);
            }
            LOGGER.info("Sucursal modificada: " + sucursal.getNombre() + " (id " + sucursal.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando la sucursal " + sucursal.getId(), e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    @Override
    public void borrarSucursal(String idSucursal) throws OurException {
        try {
            // No se puede borrar una sucursal que tiene empleados
            if (coleccion(COL_EMPLEADOS).find(new Document("idSucursal", new ObjectId(idSucursal))).first() != null) {
                throw new OurException(ErrorMessages.SUCURSAL_CON_EMPLEADOS);
            }
            if (!borrar(COL_SUCURSALES, idSucursal)) {
                throw new OurException(ErrorMessages.SAVE_SUCURSAL);
            }
            LOGGER.info("Sucursal borrada: id " + idSucursal);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la sucursal " + idSucursal, e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    // ---------- GESTIÓN DE TARIFAS, CONTRATOS, TICKETS Y VALORACIONES ----------

    @Override
    public List<Tarifa> obtenerTodasLasTarifas() throws OurException {
        List<Tarifa> tarifas = new ArrayList<>();
        try {
            for (Document d : coleccion(COL_TARIFAS).find()) {
                tarifas.add(documentoATarifa(d));
            }
            LOGGER.info("Consultadas todas las tarifas: " + tarifas.size());
            return tarifas;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando todas las tarifas", e);
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
    }

    @Override
    public void insertarTarifa(Tarifa tarifa) throws OurException {
        try {
            Document d = tarifaADocumento(tarifa);
            coleccion(COL_TARIFAS).insertOne(d);
            tarifa.setId(idTexto(d, "_id"));
            LOGGER.info("Tarifa creada: " + tarifa.getNombreTarifa() + " (id " + tarifa.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error creando la tarifa " + tarifa.getNombreTarifa(), e);
            throw new OurException(ErrorMessages.SAVE_TARIFA);
        }
    }

    @Override
    public void actualizarTarifa(Tarifa tarifa) throws OurException {
        try {
            if (!modificar(COL_TARIFAS, tarifa.getId(), tarifaADocumento(tarifa))) {
                throw new OurException(ErrorMessages.SAVE_TARIFA);
            }
            LOGGER.info("Tarifa modificada: " + tarifa.getNombreTarifa() + " (activa: " + tarifa.isActiva() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando la tarifa " + tarifa.getId(), e);
            throw new OurException(ErrorMessages.SAVE_TARIFA);
        }
    }

    @Override
    public void borrarTarifa(String idTarifa) throws OurException {
        try {
            // Una tarifa con contratos no se borra (se desactiva)
            if (coleccion(COL_CONTRATOS).find(new Document("idTarifa", new ObjectId(idTarifa))).first() != null) {
                throw new OurException(ErrorMessages.TARIFA_EN_USO);
            }
            if (!borrar(COL_TARIFAS, idTarifa)) {
                throw new OurException(ErrorMessages.DELETE_TARIFA);
            }
            LOGGER.info("Tarifa borrada: id " + idTarifa);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la tarifa " + idTarifa, e);
            throw new OurException(ErrorMessages.DELETE_TARIFA);
        }
    }

    @Override
    public List<Contrato> obtenerTodosLosContratos() throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(new Document());
            LOGGER.info("Consultados todos los contratos: " + contratos.size());
            return contratos;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando todos los contratos", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void rechazarPeticion(String idContrato, String idEmpleado) throws OurException {
        try {
            cambiarEstadoSi(idContrato, EstadoContrato.PENDIENTE, EstadoContrato.CANCELADO, idEmpleado);
            LOGGER.info("Petición " + idContrato + " rechazada por " + idEmpleado);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error rechazando la petición " + idContrato, e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    @Override
    public void rescindirContrato(String idContrato, String idEmpleado) throws OurException {
        try {
            Document contrato = buscarPorId(COL_CONTRATOS, new ObjectId(idContrato));
            if (contrato == null) {
                throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
            }
            String idCliente = idTexto(contrato, "idCliente");
            // 1. El contrato activo pasa a cancelado
            cambiarEstadoSi(idContrato, EstadoContrato.ACTIVO, EstadoContrato.CANCELADO, idEmpleado);
            // 2. Si al cliente no le queda ningún contrato, queda dado de baja. Si falla, se deshace el paso 1
            try {
                boolean quedanContratos = false;
                for (Contrato c : buscarContratos(new Document("idCliente", new ObjectId(idCliente)))) {
                    if (c.getEstado() != EstadoContrato.CANCELADO) {
                        quedanContratos = true;
                    }
                }
                if (!quedanContratos) {
                    modificar(COL_CLIENTES, idCliente, new Document("dadoDeBaja", true));
                }
            } catch (MongoException e) {
                cambiarEstadoSi(idContrato, EstadoContrato.CANCELADO, EstadoContrato.ACTIVO, idEmpleado);
                throw e;
            }
            LOGGER.info("Contrato " + idContrato + " rescindido por " + idEmpleado);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error rescindiendo el contrato " + idContrato, e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    // Cambia el estado solo si el contrato está en el estado esperado (si otro empleado ya lo cambió, da error)
    private void cambiarEstadoSi(String idContrato, EstadoContrato actual, EstadoContrato nuevo, String idEmpleado) throws OurException {
        Document filtro = new Document();
        filtro.put("_id", new ObjectId(idContrato));
        filtro.put("estado", actual.toString());
        Document cambios = new Document();
        cambios.put("estado", nuevo.toString());
        cambios.put("idEmpleado", new ObjectId(idEmpleado));
        long encontrados = coleccion(COL_CONTRATOS).updateOne(filtro, new Document("$set", cambios)).getMatchedCount();
        if (encontrados == 0) {
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    @Override
    public List<TicketSoporte> obtenerTodosLosTickets() throws OurException {
        try {
            List<TicketSoporte> tickets = buscarTickets(new Document());
            LOGGER.info("Consultados todos los tickets: " + tickets.size());
            return tickets;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando todos los tickets", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
    }

    @Override
    public List<Valoracion> obtenerTodasLasValoraciones() throws OurException {
        try {
            List<Valoracion> valoraciones = buscarValoraciones(new Document());
            LOGGER.info("Consultadas todas las valoraciones: " + valoraciones.size());
            return valoraciones;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando todas las valoraciones", e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    private List<Valoracion> buscarValoraciones(Document filtro) throws OurException {
        List<Valoracion> valoraciones = new ArrayList<>();
        for (Document d : coleccion(COL_VALORACIONES).find(filtro)) {
            Valoracion v = new Valoracion(idTexto(d, "_id"), (int) numero(d, "puntuacion"), texto(d, "texto"),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"));
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                v.setNombreCliente(texto(cliente, "nombre"));
            }
            valoraciones.add(v);
        }
        return valoraciones;
    }

    // ---------- VALORACIONES ----------

    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        try {
            List<Valoracion> valoraciones = buscarValoraciones(new Document("idCliente", new ObjectId(idCliente)));
            LOGGER.info("Consultadas " + valoraciones.size() + " valoraciones del cliente " + idCliente);
            return valoraciones;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando valoraciones del cliente " + idCliente, e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document d = new Document();
            d.put("puntuacion", valoracion.getPuntuacion());
            d.put("texto", valoracion.getTexto());
            d.put("idCliente", new ObjectId(valoracion.getIdCliente()));
            d.put("idTarifa", aObjectId(valoracion.getIdTarifa()));
            coleccion(COL_VALORACIONES).insertOne(d);

            valoracion.setId(idTexto(d, "_id"));
            LOGGER.info("Valoración publicada: id " + valoracion.getId() + ", puntuación " + valoracion.getPuntuacion());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error publicando una valoración del cliente " + valoracion.getIdCliente(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void actualizarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("puntuacion", valoracion.getPuntuacion());
            cambios.put("texto", valoracion.getTexto());
            if (!modificar(COL_VALORACIONES, valoracion.getId(), cambios)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LOGGER.info("Valoración modificada: id " + valoracion.getId());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando la valoración " + valoracion.getId(), e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void borrarValoracion(String idValoracion) throws OurException {
        try {
            if (!borrar(COL_VALORACIONES, idValoracion)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
            LOGGER.info("Valoración borrada: id " + idValoracion);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error borrando la valoración " + idValoracion, e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    // ---------- DE DOCUMENT A OBJETO Y AL REVÉS ----------

    private Cliente documentoACliente(Document d) {
        Cliente c = new Cliente(idTexto(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"), texto(d, "DNI"), texto(d, "telefono"), texto(d, "direccion"));
        c.setDadoDeBaja(booleano(d, "dadoDeBaja", false));
        return c;
    }

    private Administrador documentoAAdministrador(Document d) {
        return new Administrador(idTexto(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"));
    }

    private Tarifa documentoATarifa(Document d) {
        // Si no tiene el campo "activa", se considera activa
        return new Tarifa(idTexto(d, "_id"), texto(d, "nombreTarifa"), numero(d, "precio"), texto(d, "detalles"),
                texto(d, "tipotarifa"), booleano(d, "activa", true));
    }

    private Document tarifaADocumento(Tarifa tarifa) {
        Document d = new Document();
        d.put("nombreTarifa", tarifa.getNombreTarifa());
        d.put("precio", tarifa.getPrecio());
        d.put("detalles", tarifa.getDetalles());
        d.put("tipotarifa", tarifa.getTipoTarifa());
        d.put("activa", tarifa.isActiva());
        return d;
    }

    private Document sucursalADocumento(Sucursal sucursal) {
        // Documento embebido: la ubicación va dentro de la sucursal
        Document ubicacion = new Document();
        ubicacion.put("Direccion", sucursal.getDireccion());
        ubicacion.put("Localidad", sucursal.getLocalidad());
        ubicacion.put("CodigoPostal", sucursal.getCodigoPostal());
        Document d = new Document();
        d.put("nombre", sucursal.getNombre());
        d.put("horario", sucursal.getHorario());
        d.put("telefono", sucursal.getTelefono());
        d.put("Ubicacion", ubicacion);
        return d;
    }

    private Empleado documentoAEmpleado(Document d) {
        return new Empleado(idTexto(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"), idTexto(d, "idSucursal"));
    }

    // ---------- AYUDANTES ----------

    // Busca un documento por su _id. Devuelve null si no existe
    private Document buscarPorId(String nombreColeccion, Object id) throws OurException {
        return coleccion(nombreColeccion).find(new Document("_id", id)).first();
    }

    // Modifica los campos de "cambios" en el documento con ese id. Devuelve false si no existe
    private boolean modificar(String nombreColeccion, String id, Document cambios) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        // "$set" le dice a MongoDB que solo cambie esos campos y deje el resto como está
        long encontrados = coleccion(nombreColeccion).updateOne(filtro, new Document("$set", cambios)).getMatchedCount();
        return encontrados > 0;
    }

    // Borra el documento con ese id. Devuelve false si no existe
    private boolean borrar(String nombreColeccion, String id) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        long borrados = coleccion(nombreColeccion).deleteOne(filtro).getDeletedCount();
        return borrados > 0;
    }

    // Convierte un id de texto a ObjectId (null si no hay id)
    private ObjectId aObjectId(String id) {
        if (id == null) {
            return null;
        }
        return new ObjectId(id);
    }

    // Devuelve el campo como texto, o "" si no existe
    private String texto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

    // Devuelve un id (ObjectId) como texto, o null si no existe
    private String idTexto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return null;
        }
        return valor.toString();
    }

    // Devuelve un campo true/false. Si no existe o no es true/false, devuelve el valor por defecto
    private boolean booleano(Document d, String campo, boolean porDefecto) {
        Object valor = d.get(campo);
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        return porDefecto;
    }

    // Devuelve un número (puede estar guardado como int o como double), o 0 si no existe
    private double numero(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return 0;
        }
        try {
            return Double.parseDouble(valor.toString());
        } catch (NumberFormatException e) {
            // Si en la base de datos hay algo que no es un número, se toma como 0 en vez de cerrar la aplicación
            return 0;
        }
    }
}
