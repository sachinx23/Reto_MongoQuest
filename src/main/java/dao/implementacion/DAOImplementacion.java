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
import modelo.Tarifa;
import modelo.TicketSoporte;
import modelo.Usuario;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Acceso a MongoDB común a todos: login, registro y los métodos de ayuda que usan
 * ClienteDAOImplementacion y EmpleadoDAOImplementacion. Cada operación se apunta en el log con LOGGER.
 * MongoDB instalado en local (un solo servidor) no permite transacciones. Por eso, cuando una operación
 * escribe en dos sitios y falla la segunda escritura, se deshace la primera para no dejar datos a medias.
 * Un Document es como un HashMap: se le meten campos con put() y se leen con get().
 * Se usa para guardar datos, para buscar (filtro) y para modificar (cambios).
 */
public class DAOImplementacion implements DAO {

    protected static final String COL_ADMINISTRADORES = "Administradores";
    protected static final String COL_EMPLEADOS = "Empleados";
    protected static final String COL_CLIENTES = "Clientes";
    protected static final String COL_TARIFAS = "Tarifas";
    protected static final String COL_CONTRATOS = "Contratos";
    protected static final String COL_TICKETS = "TicketsSoportes";
    protected static final String COL_VALORACIONES = "Valoraciones";

    private static final Logger LOGGER = Logger.getLogger(DAOImplementacion.class.getName());

    protected MongoCollection<Document> coleccion(String nombre) throws OurException {
        return MongoConnection.getDatabase().getCollection(nombre);
    }

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
    protected boolean nombreUsuarioOcupado(String nombreUsuario, String idPropio) throws OurException {
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

    protected List<Contrato> buscarContratos(Document filtro) throws OurException {
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

    protected void cambiarEstadoContrato(String idContrato, EstadoContrato estado, String idEmpleado, String mensajeError) throws OurException {
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

    protected List<TicketSoporte> buscarTickets(Document filtro) throws OurException {
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

    protected Cliente documentoACliente(Document d) {
        Cliente c = new Cliente(idTexto(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"), texto(d, "DNI"), texto(d, "telefono"), texto(d, "direccion"));
        c.setDadoDeBaja(booleano(d, "dadoDeBaja", false));
        return c;
    }

    protected Administrador documentoAAdministrador(Document d) {
        return new Administrador(idTexto(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"));
    }

    protected Tarifa documentoATarifa(Document d) {
        // Si no tiene el campo "activa", se considera activa
        return new Tarifa(idTexto(d, "_id"), texto(d, "nombreTarifa"), numero(d, "precio"), texto(d, "detalles"),
                texto(d, "tipotarifa"), booleano(d, "activa", true));
    }

    protected Empleado documentoAEmpleado(Document d) {
        return new Empleado(idTexto(d, "_id"), texto(d, "nombre"), texto(d, "nombreUsuario"), texto(d, "contrasena"),
                texto(d, "correo"), texto(d, "cuentaBancaria"), idTexto(d, "idSucursal"));
    }

    // Busca un documento por su _id. Devuelve null si no existe
    protected Document buscarPorId(String nombreColeccion, Object id) throws OurException {
        return coleccion(nombreColeccion).find(new Document("_id", id)).first();
    }

    // Modifica los campos de "cambios" en el documento con ese id. Devuelve false si no existe
    protected boolean modificar(String nombreColeccion, String id, Document cambios) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        // "$set" le dice a MongoDB que solo cambie esos campos y deje el resto como está
        long encontrados = coleccion(nombreColeccion).updateOne(filtro, new Document("$set", cambios)).getMatchedCount();
        return encontrados > 0;
    }

    // Borra el documento con ese id. Devuelve false si no existe
    protected boolean borrar(String nombreColeccion, String id) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        long borrados = coleccion(nombreColeccion).deleteOne(filtro).getDeletedCount();
        return borrados > 0;
    }

    // Convierte un id de texto a ObjectId (null si no hay id)
    protected ObjectId aObjectId(String id) {
        if (id == null) {
            return null;
        }
        return new ObjectId(id);
    }

    // Devuelve el campo como texto, o "" si no existe
    protected String texto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

    // Devuelve un id (ObjectId) como texto, o null si no existe
    protected String idTexto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return null;
        }
        return valor.toString();
    }

    // Devuelve un campo true/false. Si no existe o no es true/false, devuelve el valor por defecto
    protected boolean booleano(Document d, String campo, boolean porDefecto) {
        Object valor = d.get(campo);
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        return porDefecto;
    }

    // Devuelve un número (puede estar guardado como int o como double), o 0 si no existe
    protected double numero(Document d, String campo) {
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
