package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
import DAO.MongoDBConexion;
import DAO.TicketDAO;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import modelo.TicketSoporte;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la interfaz {@link TicketDAO} para la gestión de tickets de soporte en MongoDB.
 * 
 * <p>Administra las operaciones CRUD sobre la colección {@code TicketsSoporte}, manteniendo la
 * compatibilidad de mapeo entre la nomenclatura camelCase de la aplicación Java
 * (como {@code idCliente} e {@code idEmpleado}) y los nombres de claves utilizados en la base de datos
 * (como {@code cliente_id} y {@code empleado_id}).</p>
 * 
 * @author Eneko.Plaza
 * @version 1.4
 */
public class TikcetDAOImpl implements TicketDAO {

    /**
     * Registrador de eventos (Logger) para trazar las operaciones y errores de la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(TikcetDAOImpl.class.getName());

    /**
     * Construye un documento de filtro de MongoDB basado en el identificador indicado.
     * 
     * <p>Si la cadena recibida es un hash hexadecimal válido de 24 caracteres, se convierte 
     * a {@link ObjectId}. En caso contrario, se trata como una cadena de texto convencional.</p>
     * 
     * @param idStr Identificador del documento en formato texto.
     * @return {@link Document} de filtro para buscar por la clave {@code _id}.
     */
    private Document getFiltroId(String idStr) {
        if (idStr != null && ObjectId.isValid(idStr)) {
            return new Document("_id", new ObjectId(idStr));
        }
        return new Document("_id", idStr);
    }

    /**
     * Registra un nuevo ticket de soporte de tipo incidencia generado por un cliente.
     * 
     * <p>Inserta la incidencia en la colección {@code TicketsSoporte} con estado inicial {@code "pendiente"}
     * y vincula la referencia del ticket en la ficha del cliente correspondiente.</p>
     * 
     * @param idCliente   Identificador único del cliente que crea la incidencia.
     * @param asunto      Breve resumen explicativo del problema reportado.
     * @param descripcion Explicación detallada del incidente.
     * @return {@code true} si la inserción y vinculación se completaron correctamente.
     * @throws OurException Si ocurre un fallo en la comunicación o escritura con MongoDB.
     */
    @Override
    public boolean crearTickets(String idCliente, String asunto, String descripcion) throws OurException {
        try {
            LOGGER.info("Crear ticket del cliente " + idCliente + " con asunto " + asunto);
            MongoDatabase db = MongoDBConexion.getDatabase();
            
            Object idCli = idCliente;
            if (idCliente != null && ObjectId.isValid(idCliente)) {
                idCli = new ObjectId(idCliente);
            }

            Document doc = new Document();
            doc.append("tipo", "incidencia");
            doc.append("asunto", asunto);
            doc.append("descripcion", descripcion);
            doc.append("fechaCreacion", LocalDate.now().toString());
            doc.append("estado", "pendiente");
            
            doc.append("idCliente", idCli);
            doc.append("cliente_id", idCli);
            doc.append("idEmpleado", null);
            doc.append("empleado_id", null);
            doc.append("idContrato", null);

            db.getCollection("TicketsSoporte").insertOne(doc);

            ObjectId idTicket = doc.getObjectId("_id");
            Document actualizarCliente = new Document("$push", new Document("ticketSoporte", idTicket));
            db.getCollection("Clientes").updateOne(getFiltroId(idCliente), actualizarCliente);

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en crearTickets", e);
            throw new OurException(ErrorMessages.INSERT_TICKET);
        }
    }

    /**
     * Consulta la lista de tickets de soporte según el estado especificado.
     * 
     * @param estado Estado por el que filtrar ({@code "pendiente"}, {@code "en proceso"},
     *               {@code "cerrado"} o {@code "Todos"}).
     * @return Una {@link List} de objetos {@link TicketSoporte} recuperados de la base de datos.
     * @throws OurException Si surge un fallo durante la lectura en la base de datos.
     */
    @Override
    public List<TicketSoporte> listarTicket(String estado) throws OurException {
        LOGGER.info("Listar tickets con estado " + estado);
        List<TicketSoporte> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            Document filtro = new Document();
            if (estado != null && !estado.equalsIgnoreCase("Todos")) {
                filtro.append("estado", estado.toLowerCase());
            }

            List<Document> docs = col.find(filtro).into(new ArrayList<>());
            int i = 0;
            while (i < docs.size()) {
                Document doc = docs.get(i);
                lista.add(mapearTicket(doc));
                i = i + 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarTicket", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
        return lista;
    }

    /**
     * Consulta el historial de tickets asociados a un cliente específico, buscando tanto por
     * el campo {@code idCliente} como por {@code cliente_id}.
     * 
     * @param idCliente Identificador del cliente a consultar.
     * @return Una {@link List} con los objetos {@link TicketSoporte} pertenecientes a dicho cliente.
     * @throws OurException Si falla el filtrado o la conexión con la base de datos.
     */
    @Override
    public List<TicketSoporte> listarTicketsCliente(String idCliente) throws OurException {
        LOGGER.info("Listar tickets del cliente " + idCliente);
        List<TicketSoporte> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            Object filterCliId = idCliente;
            if (ObjectId.isValid(idCliente)) {
                filterCliId = new ObjectId(idCliente);
            }

            List<Document> listaFiltros = new ArrayList<>();
            listaFiltros.add(new Document("idCliente", filterCliId));
            listaFiltros.add(new Document("cliente_id", filterCliId));
            
            Document filtro = new Document("$or", listaFiltros);

            List<Document> docs = col.find(filtro).into(new ArrayList<>());
            int i = 0;
            while (i < docs.size()) {
                Document doc = docs.get(i);
                lista.add(mapearTicket(doc));
                i = i + 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarTicketsCliente", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
        return lista;
    }

    /**
     * Asigna un ticket de soporte a un empleado y modifica su estado a {@code "en proceso"}.
     * 
     * <p>Actualiza tanto la clave {@code idEmpleado} como {@code empleado_id} en el documento 
     * de MongoDB para garantizar la persistencia del identificador del técnico.</p>
     * 
     * @param t          Instancia del {@link TicketSoporte} a asignar.
     * @param idEmpleado Identificador del empleado que asume la atención del ticket.
     * @return {@code true} si el ticket fue actualizado satisfactoriamente.
     * @throws OurException Si ocurre un fallo en la operación de actualización.
     */
    @Override
    public boolean asignarTicket(TicketSoporte t, String idEmpleado) throws OurException {
        try {
            LOGGER.info("Asignar ticket " + t.getId() + " al empleado " + idEmpleado);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            Object idEmpObj = idEmpleado;
            if (idEmpleado != null && ObjectId.isValid(idEmpleado)) {
                idEmpObj = new ObjectId(idEmpleado);
            }

            Document cambios = new Document();
            cambios.append("idEmpleado", idEmpObj);
            cambios.append("empleado_id", idEmpObj);
            cambios.append("estado", "en proceso");

            Document sentenciaUpdate = new Document("$set", cambios);
            col.updateOne(getFiltroId(t.getId()), sentenciaUpdate);

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en asignarTicket", e);
            throw new OurException(ErrorMessages.CAMBIAR_TICKET);
        }
    }

    /**
     * Modifica el estado de procesamiento de un ticket de soporte específico.
     * 
     * @param t      Instancia del {@link TicketSoporte} a actualizar.
     * @param estado Nuevo estado a establecer ({@code "pendiente"}, {@code "en proceso"} o {@code "cerrado"}).
     * @return {@code true} si al menos un documento fue modificado en la base de datos;
     *         {@code false} en caso contrario.
     * @throws OurException Si la sentencia de actualización falla.
     */
    @Override
    public boolean cambiarEstadoTicket(TicketSoporte t, String estado) throws OurException {
        try {
            LOGGER.info("Cambiar estado del ticket " + t.getId() + " a " + estado);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            Document cambios = new Document("estado", estado);
            Document sentenciaUpdate = new Document("$set", cambios);

            long modificados = col.updateOne(getFiltroId(t.getId()), sentenciaUpdate).getModifiedCount();
            return modificados > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en cambiarEstadoTicket", e);
            throw new OurException(ErrorMessages.CAMBIAR_TICKET);
        }
    }

    /**
     * Mapea un {@link Document} de MongoDB a una entidad del modelo {@link TicketSoporte}.
     * 
     * <p>Inspecciona las variantes de nombres de campo (por ejemplo {@code idEmpleado} o {@code empleado_id})
     * para evitar que los atributos del modelo queden nulos al cargarse en la interfaz JavaFX.</p>
     * 
     * @param d Documento BSON recuperado de la colección de MongoDB.
     * @return Objeto {@link TicketSoporte} poblado con la información del documento.
     */
    public TicketSoporte mapearTicket(Document d) {
        TicketSoporte t = new TicketSoporte();
        
        if (d.get("_id") != null) {
            t.setId(d.get("_id").toString());
        }
        t.setTipo(d.getString("tipo"));
        t.setAsunto(d.getString("asunto"));
        t.setDescripcion(d.getString("descripcion"));
        t.setEstado(d.getString("estado"));
        
        if (d.get("idCliente") != null) {
            t.setIdCliente(d.get("idCliente").toString());
        } else if (d.get("cliente_id") != null) {
            t.setIdCliente(d.get("cliente_id").toString());
        }

        if (d.get("idEmpleado") != null) {
            t.setIdEmpleado(d.get("idEmpleado").toString());
        } else if (d.get("empleado_id") != null) {
            t.setIdEmpleado(d.get("empleado_id").toString());
        }

        if (d.get("idContrato") != null) {
            t.setIdContrato(d.get("idContrato").toString());
        }
        if (d.get("fechaCreacion") != null) {
            t.setFechaCreacion(d.get("fechaCreacion").toString());
        }
        return t;
    }
}