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

public class ContratoDAOImplementacion extends DAOBase implements ContratoDAO {

    private static final Logger LOGGER = Logger.getLogger(ContratoDAOImplementacion.class.getName());

    private Document getFiltroId(String idStr) {
        if (idStr != null) {
            if (ObjectId.isValid(idStr)) {
                return new Document("_id", new ObjectId(idStr));
            }
        }
        return new Document("_id", idStr);
    }

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

        Object valCliente = doc.get("idCliente");
        if (valCliente != null) {
            if (valCliente instanceof List) {
                List<?> listaCli = (List<?>) valCliente;
                if (!listaCli.isEmpty() && listaCli.get(0) != null) {
                    c.setIdCliente(listaCli.get(0).toString());
                } else {
                    c.setIdCliente("");
                }
            } else {
                c.setIdCliente(valCliente.toString());
            }
        } else {
            c.setIdCliente("");
        }

        Object valTarifa = doc.get("idTarifa");
        if (valTarifa != null) {
            if (valTarifa instanceof List) {
                List<?> listaTarifa = (List<?>) valTarifa;
                if (!listaTarifa.isEmpty() && listaTarifa.get(0) != null) {
                    c.setIdTarifa(listaTarifa.get(0).toString());
                } else {
                    c.setIdTarifa("");
                }
            } else {
                c.setIdTarifa(valTarifa.toString());
            }
        } else {
            c.setIdTarifa("");
        }
        Object valEmpleado = doc.get("idEmpleado");
        if (valEmpleado != null) {
            if (valEmpleado instanceof List) {
                List<?> listaEmp = (List<?>) valEmpleado;
                if (!listaEmp.isEmpty() && listaEmp.get(0) != null) {
                    c.setIdEmpleado(listaEmp.get(0).toString());
                } else {
                    c.setIdEmpleado("");
                }
            } else {
                c.setIdEmpleado(valEmpleado.toString());
            }
        } else {
            c.setIdEmpleado("");
        }

        if (doc.get("fechaComienzo") != null) {
            c.setFechaComienzo(doc.get("fechaComienzo").toString());
        } else {
            c.setFechaComienzo("");
        }

        Object fFin = doc.get("fechaFinalizacion");
        if (fFin == null) {
            fFin = doc.get("fechaFinalización");
        }
        if (fFin != null) {
            c.setFechaFin(fFin.toString());
        } else {
            c.setFechaFin("");
        }

        return c;
    }

    @Override
    public List<Contrato> listarContratos() throws OurException {
        LOGGER.info("Listar contratos");
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");
            List<Document> documentos = col.find().into(new ArrayList<>());

            for (int i = 0; i < documentos.size(); i++) {
                Document doc = documentos.get(i);
                Contrato c = mapearContrato(doc);
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

    @Override
    public boolean aprobarContrato(Contrato c, String idGestor) throws OurException {
        try {
            LOGGER.info("Aprobar contrato " + c.getId() + " de la línea " + c.getTelefono() + ", gestor: " + idGestor);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Object idEmp = idGestor;
            if (ObjectId.isValid(idGestor)) {
                idEmp = new ObjectId(idGestor);
            }

            // Al confirmar la tarifa, se marca como pendiente de firma para enviarlo al cliente
            Document camposNuevos = new Document("estado", "pendiente de firma")
                    .append("idEmpleado", idEmp);

            Document sentenciaUpdate = new Document("$set", camposNuevos);

            boolean ok = col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
            return ok;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en aprobarContrato", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    @Override
    public boolean firmarContrato(Contrato c) throws OurException {
        try {
            LOGGER.info("Firmar contrato " + c.getId() + " de la línea " + c.getTelefono() + ", cliente: " + c.getIdCliente());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            // Al firmar el cliente, el contrato pasa a activo y se registra la fecha de comienzo
            Document camposNuevos = new Document("estado", "activo")
                    .append("fechaComienzo", LocalDate.now().toString());

            Document sentenciaUpdate = new Document("$set", camposNuevos);

            boolean ok = col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;

            if (ok) {
                ObjectId idCli = null;
                if (ObjectId.isValid(c.getIdCliente())) {
                    idCli = new ObjectId(c.getIdCliente());
                }

                Object idEmp = c.getIdEmpleado();
                if (ObjectId.isValid(c.getIdEmpleado())) {
                    idEmp = new ObjectId(c.getIdEmpleado());
                }

                ObjectId idCon = null;
                if (ObjectId.isValid(c.getId())) {
                    idCon = new ObjectId(c.getId());
                }

                // Generar el ticket de soporte con los detalles de la compra al firmar
                Document ticket = new Document()
                        .append("tipo", "contrato")
                        .append("asunto", "Detalles de Compra y Firma de Contrato")
                        .append("descripcion", "Su contrato para la línea " + c.getTelefono() + " ha sido firmado correctamente.")
                        .append("fechaCreacion", LocalDate.now().toString())
                        .append("estado", "cerrado")
                        .append("idCliente", idCli)
                        .append("idEmpleado", idEmp)
                        .append("idContrato", idCon);

                db.getCollection("TicketsSoporte").insertOne(ticket);
            }
            return ok;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en firmarContrato", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    @Override
    public boolean rechazarContratos(Contrato c) throws OurException {
        try {
            LOGGER.info("Rechazar contrato " + c.getId() + " de la línea " + c.getTelefono());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document camposNuevos = new Document("estado", "rechazado");
            Document sentenciaUpdate = new Document("$set", camposNuevos);

            return col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en rechazarContratos", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    @Override
    public boolean rescindirContrato(Contrato c) throws OurException {
        try {
            LOGGER.info("Rescindir contrato " + c.getId() + " de la línea " + c.getTelefono());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document camposNuevos = new Document("estado", "rescindido")
                    .append("fechaFinalizacion", LocalDate.now().toString());

            Document sentenciaUpdate = new Document("$set", camposNuevos);

            return col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en rescindirContrato", e);
            throw new OurException(ErrorMessages.CAMBIAR_CONTRATO);
        }
    }

    @Override
    public List<Contrato> filtrarContratos(String telefono, String estado) throws OurException {
        LOGGER.info("Filtrar contratos, teléfono: " + telefono + ", estado: " + estado);
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document filtro = new Document();

            if (telefono != null) {
                if (!telefono.trim().isEmpty()) {
                    filtro.append("telefono", java.util.regex.Pattern.compile(telefono.trim(), java.util.regex.Pattern.CASE_INSENSITIVE));
                }
            }

            if (estado != null) {
                if (!"Todos".equalsIgnoreCase(estado)) {
                    filtro.append("estado", estado);
                }
            }

            List<Document> documentos = col.find(filtro).into(new ArrayList<>());
            for (int i = 0; i < documentos.size(); i++) {
                Document doc = documentos.get(i);
                Contrato c = mapearContrato(doc);
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

    @Override
    public List<Contrato> obtenerContratosCliente(String idCliente) throws OurException {
        LOGGER.info("Listar contratos del cliente " + idCliente);
        try {
            List<Contrato> contratos = buscarContratos(new Document("idCliente", new ObjectId(idCliente)));
            return contratos;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerContratosCliente", e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void insertarContrato(Contrato contrato, Tarifa tarifa) throws OurException {
        LOGGER.info("Pedir contrato de la tarifa " + tarifa.getNombreTarifa() + " para la línea " + contrato.getTelefono() + ", cliente: " + contrato.getIdCliente());
        try {
            Document d = new Document();
            d.put("telefono", contrato.getTelefono());
            d.put("estado", contrato.getEstado());
            d.put("idCliente", new ObjectId(contrato.getIdCliente()));
            d.put("idTarifa", new ObjectId(contrato.getIdTarifa()));
            d.put("idEmpleado", aObjectId(contrato.getIdEmpleado()));

            // Tarifa embebida
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

    @Override
    public boolean borrarPeticion(String idCliente, String idTarifa) throws OurException {
        LOGGER.info("Cancelar petición del cliente " + idCliente + " para la tarifa " + idTarifa);
        try {
            Document filtro = new Document();
            filtro.put("idCliente", new ObjectId(idCliente));
            filtro.put("idTarifa", new ObjectId(idTarifa));
            filtro.put("estado", "pendiente");
            long borrados = coleccion(COL_CONTRATOS).deleteOne(filtro).getDeletedCount();
            return borrados > 0;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en borrarPeticion", e);
            throw new OurException(ErrorMessages.BAJA_CONTRATO);
        }
    }

    @Override
    public void solicitarBaja(String idContrato) throws OurException {
        LOGGER.info("Solicitar baja del contrato " + idContrato);
        cambiarEstadoContrato(idContrato, "baja pendiente", null, ErrorMessages.BAJA_CONTRATO);
    }
}
