/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin.implementacion;

import daoadmin.MongoDBConexion;
import daoadmin.MongoDBConexion;
import daoadmin.TicketDAO;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import modeloadmin.TicketSoporte;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 /*
 * @author Eneko.Plaza
 */
public class TikcetDAOImpl implements TicketDAO {
    private Document getFiltroId(String idStr) {
        if (idStr != null) {
            if (ObjectId.isValid(idStr)) {
                return new Document("_id", new ObjectId(idStr));
            }
        }
        return new Document("_id", idStr);
    }

    public boolean crearTickets(String idCliente, String asunto, String descripcion) {
        try {
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
            e.printStackTrace();
            return false;
        }
    }

    public List<TicketSoporte> listarTicket(String estado) {
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
            e.printStackTrace();
        }
        return lista;
    }

    public List<TicketSoporte> listarTicketsCliente(String idCliente) {
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
            e.printStackTrace();
        }
        return lista;
    }

    public boolean asignarTicket(TicketSoporte t, String idEmpleado) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("TicketsSoporte");

            ObjectId idEmp = new ObjectId(idEmpleado);

            Document cambios = new Document("idEmpleado", idEmp);
            cambios.append("estado", "en proceso");

            Document sentenciaUpdate = new Document("$set", cambios);
            col.updateOne(getFiltroId(t.getId()), sentenciaUpdate);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cambiarEstadoTicket(TicketSoporte t, String estado) {
        try {
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
            e.printStackTrace();
            return false;
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

