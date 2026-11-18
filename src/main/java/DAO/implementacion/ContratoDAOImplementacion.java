package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
import DAO.ContratoDAO;
import DAO.MongoDBConexion;
import modelo.Contrato;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.MongoException;
import modelo.Tarifa;

/**
 * Implementación de la interfaz {@link ContratoDAO} que extiende
 * {@link DAOBase} para gestionar el ciclo de vida completo de los contratos
 * (creación, aprobación, firma, rechazo, rescisión, consulta y filtrado) en la
 * base de datos MongoDB.
 *
 * @author Eneko.Plaza
 */
public class ContratoDAOImplementacion extends DAOBase implements ContratoDAO {

    /**
     * Registrador de eventos (Logger) para trazar las operaciones y errores de
     * la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(ContratoDAOImplementacion.class.getName());

    /**
     * Construye un documento de filtro para buscar por el campo {@code _id},
     * convirtiendo la cadena a {@link ObjectId} si tiene un formato hexadecimal
     * válido.
     *
     * @param idStr Identificador en formato {@link String}.
     * @return Un {@link Document} configurado con la clave {@code _id} lista
     * para consultar en MongoDB.
     */
    private Document getFiltroId(String idStr) {
        if (idStr != null && ObjectId.isValid(idStr)) {
            return new Document("_id", new ObjectId(idStr));
        }
        return new Document("_id", idStr);
    }

    /**
     * Transforma un {@link Document} de MongoDB perteneciente a la colección
     * {@code Contratos} en una instancia del modelo {@link Contrato}.
     *
     * @param doc Documento BSON recuperado de la base de datos.
     * @return El objeto {@link Contrato} mapeado, o {@code null} si el
     * documento de entrada es nulo.
     */
    private Contrato mapearContrato(Document doc) {
        if (doc == null) {
            return null;
        }

        Contrato c = new Contrato();

        if (doc.get("_id") != null) {
            c.setId(doc.get("_id").toString());
        } else {
            c.setId("");
        }

        if (doc.get("telefono") != null) {
            c.setTelefono(doc.get("telefono").toString());
        } else {
            c.setTelefono("");
        }

        if (doc.get("estado") != null) {
            c.setEstado(doc.get("estado").toString());
        } else {
            c.setEstado("");
        }

        if (doc.get("cliente_id") != null) {
            c.setIdCliente(doc.get("cliente_id").toString());
        } else if (doc.get("idCliente") != null) {
            c.setIdCliente(doc.get("idCliente").toString());
        } else {
            c.setIdCliente("");
        }

        if (doc.get("tarifa_id") != null) {
            c.setIdTarifa(doc.get("tarifa_id").toString());
        } else if (doc.get("idTarifa") != null) {
            c.setIdTarifa(doc.get("idTarifa").toString());
        } else {
            c.setIdTarifa("");
        }

        if (doc.get("empleado_id") != null) {
            c.setIdEmpleado(doc.get("empleado_id").toString());
        } else if (doc.get("idEmpleado") != null) {
            c.setIdEmpleado(doc.get("idEmpleado").toString());
        } else {
            c.setIdEmpleado("");
        }

        if (doc.get("fechaComienzo") != null) {
            c.setFechaComienzo(doc.get("fechaComienzo").toString());
        } else {
            c.setFechaComienzo("");
        }

        if (doc.get("fechaFinalizacion") != null) {
            c.setFechaFin(doc.get("fechaFinalizacion").toString());
        } else {
            c.setFechaFin("");
        }

        return c;
    }

    /**
     * Recupera todos los contratos existentes en la colección
     * {@code Contratos}.
     *
     * @return Una {@link List} con todos los objetos {@link Contrato}
     * registrados en el sistema.
     * @throws OurException Si ocurre un error durante la lectura de la
     * colección en MongoDB.
     */
    @Override
    public List<Contrato> listarContratos() throws OurException {
        LOGGER.info("Listar contratos");
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");
            List<Document> documentos = col.find().into(new ArrayList<>());

            for (int i = 0; i < documentos.size(); i++) {
                Contrato c = mapearContrato(documentos.get(i));
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarContratos", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
        return lista;
    }

    /**
     * Aprueba una solicitud de contrato asignándole el empleado gestor
     * correspondiente y cambiando su estado a {@code "pendiente de firma"} para
     * que pueda ser validado por el cliente.
     *
     * @param c Objeto {@link Contrato} que se va a aprobar.
     * @param idGestor Identificador del empleado que gestiona y aprueba el
     * contrato.
     * @return {@code true} si el estado del contrato fue actualizado
     * correctamente; {@code false} si no se encontró o no se modificó el
     * documento.
     * @throws OurException Si se produce un fallo al actualizar el contrato en
     * la base de datos.
     */
    @Override
    public boolean aprobarContrato(Contrato c, String idGestor) throws OurException {
        try {
            LOGGER.info("Aprobar contrato " + c.getId());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document cambios = new Document();
            cambios.append("estado", "activo");

            if (idGestor != null && ObjectId.isValid(idGestor)) {
                cambios.append("empleado_id", new ObjectId(idGestor));
            }

            Document update = new Document("$set", cambios);
            long encontrados = col.updateOne(getFiltroId(c.getId()), update).getMatchedCount();
            return encontrados > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en aprobarContrato", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    /**
     * Formaliza la firma del contrato por parte del cliente, actualizando su
     * estado a {@code "activo"}, estableciendo la fecha actual como fecha de
     * comienzo y generando automáticamente un ticket cerrado en la colección
     * {@code TicketSoportes} como comprobante de la operación.
     *
     * @param c Objeto {@link Contrato} que el cliente procede a firmar.
     * @return {@code true} si el contrato pasó a estado activo; {@code false}
     * en caso contrario.
     * @throws OurException Si ocurre un error al actualizar el contrato o al
     * registrar el ticket de soporte.
     */
    @Override
    public boolean firmarContrato(Contrato c) throws OurException {
        try {
            LOGGER.info("Firmar contrato " + c.getId() + " de la línea " + c.getTelefono() + ", cliente: " + c.getIdCliente());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document cambios = new Document();
            cambios.append("estado", "activo");
            cambios.append("fechaComienzo", LocalDate.now().toString());

            Document update = new Document("$set", cambios);
            boolean ok = col.updateOne(getFiltroId(c.getId()), update).getMatchedCount() > 0;

            if (ok) {
                ObjectId idCli = null;
                if (c.getIdCliente() != null && ObjectId.isValid(c.getIdCliente())) {
                    idCli = new ObjectId(c.getIdCliente());
                }

                Object idEmp = c.getIdEmpleado();
                if (c.getIdEmpleado() != null && ObjectId.isValid(c.getIdEmpleado())) {
                    idEmp = new ObjectId(c.getIdEmpleado());
                }

                Document ticket = new Document();
                ticket.append("tipo", "contrato");
                ticket.append("asunto", "Detalles de Compra y Firma de Contrato");
                ticket.append("descripcion", "Su contrato para la línea " + c.getTelefono() + " ha sido firmado correctamente.");
                ticket.append("fechaCreacion", LocalDate.now().toString());
                ticket.append("fechaFin", null);
                ticket.append("estado", "cerrado");
                ticket.append("cliente_id", idCli);
                ticket.append("empleado_id", idEmp);

                db.getCollection("TicketSoportes").insertOne(ticket);
            }
            return ok;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en firmarContrato", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    /**
     * Marca un contrato como {@code "rechazado"} en la base de datos.
     *
     * @param c Objeto {@link Contrato} que será rechazado.
     * @return {@code true} si el contrato fue actualizado correctamente;
     * {@code false} si no se modificó.
     * @throws OurException Si ocurre un error al actualizar el estado del
     * contrato.
     */
    @Override
    public boolean rechazarContratos(Contrato c) throws OurException {
        try {
            LOGGER.info("Rechazar contrato " + c.getId());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document cambios = new Document("estado", "rechazado");
            Document update = new Document("$set", cambios);
            long encontrados = col.updateOne(getFiltroId(c.getId()), update).getMatchedCount();
            return encontrados > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en rechazarContratos", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    /**
     * Finaliza anticipadamente o da de baja un contrato, cambiando su estado a
     * {@code "rescindido"} y registrando la fecha actual como
     * {@code fechaFinalizacion}.
     *
     * @param c Objeto {@link Contrato} que se desea rescindir.
     * @return {@code true} si la rescisión se aplicó correctamente;
     * {@code false} si no se encontró el contrato.
     * @throws OurException Si se produce un fallo durante la actualización en
     * MongoDB.
     */
    @Override
    public boolean rescindirContrato(Contrato c) throws OurException {
        try {
            LOGGER.info("Rescindir contrato " + c.getId());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document cambios = new Document();
            cambios.append("estado", "rescindido");
            cambios.append("fechaFinalizacion", LocalDate.now().toString());

            Document update = new Document("$set", cambios);
            long encontrados = col.updateOne(getFiltroId(c.getId()), update).getMatchedCount();
            return encontrados > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en rescindirContrato", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    /**
     * Busca contratos aplicando filtros opcionales por número de teléfono
     * (búsqueda parcial insensible a mayúsculas) y por estado (ignorando el
     * filtro si el valor es {@code "Todos"}).
     *
     * @param telefono Número o fragmento de teléfono a buscar; puede ser
     * {@code null} o vacío.
     * @param estado Estado exacto del contrato por el cual filtrar, o
     * {@code "Todos"} para no filtrar por estado.
     * @return Una {@link List} de objetos {@link Contrato} que cumplen con los
     * criterios especificados.
     * @throws OurException Si ocurre un error al ejecutar la consulta filtrada
     * en MongoDB.
     */
    @Override
    public List<Contrato> filtrarContratos(String telefono, String estado) throws OurException {
        LOGGER.info("Filtrar contratos, teléfono: " + telefono + ", estado: " + estado);
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document filtro = new Document();

            if (telefono != null && !telefono.trim().isEmpty()) {
                filtro.append("telefono", java.util.regex.Pattern.compile(telefono.trim(), java.util.regex.Pattern.CASE_INSENSITIVE));
            }

            if (estado != null && !"Todos".equalsIgnoreCase(estado)) {
                filtro.append("estado", estado);
            }

            List<Document> documentos = col.find(filtro).into(new ArrayList<>());
            for (int i = 0; i < documentos.size(); i++) {
                Contrato c = mapearContrato(documentos.get(i));
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en filtrarContratos", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
        return lista;
    }

    // Parte cliente
    /**
     * Obtiene todos los contratos asociados a un cliente específico a partir de
     * su identificador.
     *
     * @param idCliente Identificador hexadecimal ({@link ObjectId}) del
     * cliente.
     * @return Una {@link List} con los {@link Contrato} pertenecientes al
     * cliente indicado.
     * @throws OurException Si ocurre un error de MongoDB al consultar los
     * contratos del cliente.
     */
    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        LOGGER.info("Listar contratos del cliente " + idCliente);
        List<Contrato> lista = new ArrayList<>();
        try {
            Object objId = idCliente;
            if (idCliente != null && ObjectId.isValid(idCliente)) {
                objId = new ObjectId(idCliente);
            }

            Document filtro = new Document("cliente_id", objId);
            List<Document> documentos = coleccion(COL_CONTRATOS).find(filtro).into(new ArrayList<>());

            if (documentos.isEmpty()) {
                Document filtroAlt = new Document("idCliente", objId);
                documentos = coleccion(COL_CONTRATOS).find(filtroAlt).into(new ArrayList<>());
            }

            for (int i = 0; i < documentos.size(); i++) {
                Contrato c = mapearContrato(documentos.get(i));
                if (c != null) {
                    lista.add(c);
                }
            }
            return lista;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerContratosCliente", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    /**
     * Inserta un nuevo contrato en la colección {@code Contratos} incluyendo
     * los datos embebidos de la {@link Tarifa} contratada, y asigna al objeto
     * {@link Contrato} el {@code _id} generado.
     *
     * @param contrato Objeto {@link Contrato} con los datos de la nueva
     * contratación.
     * @param tarifa Objeto {@link Tarifa} cuyos datos básicos (nombre y precio)
     * se embeberán en el documento.
     * @throws OurException Si se produce un error de MongoDB durante la
     * inserción.
     */
    @Override
    public void insertarContrato(Contrato contrato, Tarifa tarifa) throws OurException {
        LOGGER.info("Pedir contrato de la tarifa " + tarifa.getNombreTarifa() + " para la línea " + contrato.getTelefono() + ", cliente: " + contrato.getIdCliente());
        try {
            Document d = new Document();
            d.put("telefono", contrato.getTelefono());
            d.put("estado", contrato.getEstado());
            d.put("cliente_id", new ObjectId(contrato.getIdCliente()));
            d.put("tarifa_id", new ObjectId(contrato.getIdTarifa()));
            d.put("empleado_id", aObjectId(contrato.getIdEmpleado()));

            Document datosTarifa = new Document();
            datosTarifa.put("nombreTarifa", tarifa.getNombreTarifa());
            datosTarifa.put("precio", tarifa.getPrecio());
            d.put("tarifa", datosTarifa);

            d.put("fechaComienzo", contrato.getFechaComienzo());
            d.put("fechaFinalizacion", contrato.getFechaFin());
            coleccion(COL_CONTRATOS).insertOne(d);

            contrato.setId(idTexto(d, "_id"));
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en insertarContrato", e);
            throw new OurException(ErrorMessages.INSERT_CONTRATO);
        }
    }

    /**
     * Elimina una solicitud de contratación realizada por un cliente siempre
     * que aún se encuentre en estado {@code "pendiente"}.
     *
     * @param idCliente Identificador del cliente que realizó la petición.
     * @param idTarifa Identificador de la tarifa solicitada.
     * @return {@code true} si la petición pendiente fue eliminada;
     * {@code false} si no existía o ya no estaba pendiente.
     * @throws OurException Si ocurre un error en MongoDB al intentar borrar el
     * documento.
     */
    @Override
    public boolean borrarPeticion(String idCliente, String idTarifa) throws OurException {
        LOGGER.info("Cancelar petición del cliente " + idCliente + " para la tarifa " + idTarifa);
        try {
            Document filtro = new Document();
            filtro.put("cliente_id", new ObjectId(idCliente));
            filtro.put("tarifa_id", new ObjectId(idTarifa));
            filtro.put("estado", "pendiente");

            long borrados = coleccion(COL_CONTRATOS).deleteOne(filtro).getDeletedCount();

            if (borrados == 0) {
                Document filtroAlt = new Document();
                filtroAlt.put("idCliente", new ObjectId(idCliente));
                filtroAlt.put("idTarifa", new ObjectId(idTarifa));
                filtroAlt.put("estado", "pendiente");
                borrados = coleccion(COL_CONTRATOS).deleteOne(filtroAlt).getDeletedCount();
            }

            return borrados > 0;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en borrarPeticion", e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    /**
     * Registra la solicitud de baja de un contrato activo cambiando su estado a
     * {@code "baja pendiente"}.
     *
     * @param idContrato Identificador del contrato sobre el que se solicita la
     * baja.
     * @throws OurException Si se produce un error al actualizar el estado del
     * contrato.
     */
    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        LOGGER.info("Solicitar baja del contrato " + idContrato);
        cambiarEstadoContrato(idContrato, "baja pendiente", null, ErrorMessages.BAJA_CONTRATO);
    }
}
