package daocliente.implementacion;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import daocliente.MongoConnection;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;
import modelo.Empleado;
import modelo.Tarifa;
import modelo.TicketSoporte;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Lo común a todos los DAO de cliente y empleado: los nombres de las colecciones y los métodos de ayuda
 * (buscar, modificar, borrar, pasar de Document a objeto...). Cada DAO hereda de esta clase para no repetirlos.
 * MongoDB instalado en local (un solo servidor) no permite transacciones. Por eso, cuando una operación
 * escribe en dos sitios y falla la segunda escritura, se deshace la primera para no dejar datos a medias.
 * Un Document es como un HashMap: se le meten campos con put() y se leen con get().
 */
public abstract class DAOBase {

    private static final Logger LOGGER = Logger.getLogger(DAOBase.class.getName());

    protected static final String COL_ADMINISTRADORES = "Administradores";
    protected static final String COL_EMPLEADOS = "Empleados";
    protected static final String COL_CLIENTES = "Clientes";
    protected static final String COL_TARIFAS = "Tarifas";
    protected static final String COL_CONTRATOS = "Contratos";
    protected static final String COL_TICKETS = "TicketsSoporte";
    protected static final String COL_VALORACIONES = "Valoraciones";

    protected MongoCollection<Document> coleccion(String nombre) throws OurException {
        return MongoConnection.getDatabase().getCollection(nombre);
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

    protected List<Contrato> buscarContratos(Document filtro) throws OurException {
        List<Contrato> contratos = new ArrayList<>();
        for (Document d : coleccion(COL_CONTRATOS).find(filtro)) {
            Contrato c = new Contrato(idTexto(d, "_id"), texto(d, "telefono"), EstadoContrato.desdeTexto(texto(d, "estado")),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"), idTexto(d, "idEmpleado"),
                    texto(d, "fechaComienzo"), texto(d, "fechaFinalizacion"));

            // La tarifa está embebida dentro del contrato (se comprueba que de verdad sea un documento)
            Object tarifa = d.get("tarifa");
            if (!(tarifa instanceof Document)) {
                // Los contratos sin la tarifa embebida (por ejemplo los de la parte de administrador) la buscan en Tarifas
                tarifa = buscarPorId(COL_TARIFAS, d.get("idTarifa"));
            }
            if (tarifa instanceof Document) {
                c.setNombreTarifa(texto((Document) tarifa, "nombreTarifa"));
                c.setPrecio(numero((Document) tarifa, "precio"));
            }
            // El nombre y el DNI del cliente se buscan en su colección
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                c.setNombreCliente(texto(cliente, "nombre"));
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
                texto(d, "correo"), texto(d, "cuentaBancaria"), texto(d, "dni"), texto(d, "telefono"), texto(d, "direccion"));
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
                texto(d, "tipoTarifa"), booleano(d, "activa", true));
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
