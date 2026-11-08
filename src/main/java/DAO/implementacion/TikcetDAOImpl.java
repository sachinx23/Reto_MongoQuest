/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
 /*
 * @author Eneko.Plaza
 */
public class TikcetDAOImpl implements TicketDAO {

    private static final Logger LOGGER = Logger.getLogger(TikcetDAOImpl.class.getName());
    private Document getFiltroId(String idStr) {
        if (idStr != null) {
            if (ObjectId.isValid(idStr)) {
                return new Document("_id", new ObjectId(idStr));
            }
        }
        return new Document("_id", idStr);
    }

    public boolean crearTickets(String idCliente, String asunto, String descripcion) throws OurException {
        try {
            LOGGER.info("Crear ticket del cliente " + idCliente + ", asunto: " + asunto);
            MongoDatabase db = MongoDBConexion.getDatabase();
            Object idCli = idCliente;
            if (idCliente != null) {
                if (ObjectId.isValid(idCliente)) {
                    idCli = new ObjectId(idCliente);
                }
            }

            Document doc = new Document();
            doc.append("tipo", "incidencia");
            doc.append("asunto", asunto);
            doc.append("descripcion", descripcion);
            doc.append("fechaCreacion", LocalDate.now().toString());
            doc.append("estado", "pendiente");
            doc.append("idCliente", idCli);
            doc.append("idEmpleado", null);
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

    public List<TicketSoporte> listarTicket(String estado) throws OurException {
        LOGGER.info("Listar tickets con estado: " + estado);
        List<TicketSoporte> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            Document filtro = new Document("tipo", "incidencia");
            if (estado != null) {
                if (!estado.equals("Todos")) {
                    filtro.append("estado", estado);
                }
            }

            List<Document> docs = col.find(filtro).into(new ArrayList<>());
            int i = 0;
            while (i < docs.size()) {
                Document doc = docs.get(i);
                TicketSoporte t = mapearTicket(doc);
                lista.add(t);
                i = i + 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarTicket", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
        return lista;
    }

    public List<TicketSoporte> listarTicketsCliente(String idCliente) throws OurException {
        LOGGER.info("Listar tickets del cliente " + idCliente);
        List<TicketSoporte> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            ObjectId id = new ObjectId(idCliente);
            Document filtro = new Document("idCliente", id);

            List<Document> docs = new ArrayList<>();
            col.find(filtro).into(docs);

            int i = 0;
            while (i < docs.size()) {
                Document doc = docs.get(i);
                TicketSoporte t = mapearTicket(doc);
                lista.add(t);
                i = i + 1;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarTicketsCliente", e);
            throw new OurException(ErrorMessages.GET_TICKETS);
        }
        return lista;
    }

    public boolean asignarTicket(TicketSoporte t, String idEmpleado) throws OurException {
        try {
            LOGGER.info("Asignar ticket " + t.getId() + " (" + t.getAsunto() + ") al empleado " + idEmpleado);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            ObjectId idEmp = new ObjectId(idEmpleado);

            Document cambios = new Document("idEmpleado", idEmp);
            cambios.append("estado", "en proceso");

            Document sentenciaUpdate = new Document("$set", cambios);
            col.updateOne(getFiltroId(t.getId()), sentenciaUpdate);

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en asignarTicket", e);
            throw new OurException(ErrorMessages.CAMBIAR_TICKET);
        }
    }

    public boolean cambiarEstadoTicket(TicketSoporte t, String estado) throws OurException {
        try {
            LOGGER.info("Cambiar estado del ticket " + t.getId() + " a " + estado);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            Document cambios = new Document("estado", estado);
            Document sentenciaUpdate = new Document("$set", cambios);

            long modificados = col.updateOne(getFiltroId(t.getId()), sentenciaUpdate).getModifiedCount();
            if (modificados > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en cambiarEstadoTicket", e);
            throw new OurException(ErrorMessages.CAMBIAR_TICKET);
        }
    }

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
        }
        if (d.get("idEmpleado") != null) {
            t.setIdEmpleado(d.get("idEmpleado").toString());
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

