/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin.implementacion;

import daoadmin.ContratoDAO;
import daoadmin.MongoDBConexion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import modeloadmin.Contrato;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
/**
 *
 * @author Eneko.Plaza
 */
public class ContratoDaoImpl implements ContratoDAO{
   // Método sencillo para buscar por ID sin usar Bson
    private Document getFiltroId(String idStr) {
        if (idStr != null && ObjectId.isValid(idStr)) {
            return new Document("_id", new ObjectId(idStr));
        }
        return new Document("_id", idStr);
    }

    private Contrato mapearContrato(Document doc) {
        if (doc == null) {
            return null;
        }

        Contrato c = new Contrato();
        c.setId(doc.get("_id") != null ? doc.get("_id").toString() : "");
        c.setTelefono(doc.get("telefono") != null ? doc.get("telefono").toString() : "");
        c.setEstado(doc.get("estado") != null ? doc.get("estado").toString() : "");
        c.setIdCliente(doc.get("idCliente") != null ? doc.get("idCliente").toString() : "");
        c.setIdTarifa(doc.get("idTarifa") != null ? doc.get("idTarifa").toString() : "");
        c.setIdEmpleado(doc.get("idEmpleado") != null ? doc.get("idEmpleado").toString() : "");
        c.setFechaComienzo(doc.get("fechaComienzo") != null ? doc.get("fechaComienzo").toString() : "");

        Object fFin = doc.get("fechaFinalizacion");
        if (fFin == null) {
            fFin = doc.get("fechaFinalización");
        }
        c.setFechaFin(fFin != null ? fFin.toString() : "");

        return c;
    }

    @Override
    public List<Contrato> listarContratos() {
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");
            
            for (Document doc : col.find()) {
                Contrato c = mapearContrato(doc);
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean aprobarContrato(Contrato c, String idGestor) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Object idEmp = ObjectId.isValid(idGestor) ? new ObjectId(idGestor) : idGestor;

            // Actualización simple con un Document de tipo "$set"
            Document camposNuevos = new Document("estado", "activo")
                    .append("idEmpleado", idEmp)
                    .append("fechaComienzo", LocalDate.now().toString());
            
            Document sentenciaUpdate = new Document("$set", camposNuevos);

            boolean ok = col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;

            if (ok) {
                ObjectId idCli = new ObjectId(c.getIdCliente());
                ObjectId idCon = new ObjectId(c.getId());

                Document ticket = new Document()
                        .append("tipo", "contrato")
                        .append("asunto", "Confirmación de Alta de Tarifa")
                        .append("descripcion", "Su solicitud para la línea " + c.getTelefono() + " ha sido aprobada.")
                        .append("fechaCreacion", LocalDate.now().toString())
                        .append("estado", "cerrado")
                        .append("idCliente", idCli)
                        .append("idEmpleado", idEmp)
                        .append("idContrato", idCon);
                
                db.getCollection("TicketsSoporte").insertOne(ticket);
            }
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rechazarContratos(Contrato c) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document camposNuevos = new Document("estado", "rechazado");
            Document sentenciaUpdate = new Document("$set", camposNuevos);

            return col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rescindirContrato(Contrato c) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");

            Document camposNuevos = new Document("estado", "rescindido")
                    .append("fechaFinalizacion", LocalDate.now().toString());
            
            Document sentenciaUpdate = new Document("$set", camposNuevos);

            return col.updateOne(getFiltroId(c.getId()), sentenciaUpdate).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Contrato> filtrarContratos(String telefono, String estado) {
        List<Contrato> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Contratos");
            
            Document filtro = new Document();

            if (telefono != null && !telefono.trim().isEmpty()) {
                // Búsqueda de texto parcial sencilla
                filtro.append("telefono", java.util.regex.Pattern.compile(telefono.trim(), java.util.regex.Pattern.CASE_INSENSITIVE));
            }

            if (estado != null && !"Todos".equalsIgnoreCase(estado)) {
                filtro.append("estado", estado);
            }

            for (Document doc : col.find(filtro)) {
                Contrato c = mapearContrato(doc);
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}