/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO.implementacion;

import DAO.TarifaDAO;
import DAO.MongoDBConexion;
import modelo.Tarifa;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public class TarifaDaoImplementacion implements TarifaDAO {
private Bson getFiltroId(String idStr) {
        if (idStr != null && ObjectId.isValid(idStr)) {
            return Filters.or(Filters.eq("_id", new ObjectId(idStr)), Filters.eq("_id", idStr));
        }
        return Filters.eq("_id", idStr);
    }

    @Override
    public List<Tarifa> listarTarifas() {
        List<Tarifa> resultado = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Tarifas");
            for (Document d : col.find()) {
                String id = d.get("_id") != null ? d.get("_id").toString() : "";
                String nombre = d.getString("nombreTarifa");
                Object p = d.get("precio");
                double precio = p instanceof Number ? ((Number) p).doubleValue() : 0.0;
                String detalles = d.getString("detalles");
                String tipo = d.getString("tipoTarifa");
                Boolean activa = d.getBoolean("activa", false);

                resultado.add(new Tarifa(id, nombre, precio, detalles, tipo, activa));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    @Override
    public boolean insertarTarifa(Tarifa t) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Tarifas");
            Document doc = new Document("nombreTarifa", t.getNombreTarifa())
                    .append("precio", t.getPrecio())
                    .append("detalles", t.getDetalles())
                    .append("tipoTarifa", t.getTipoTarifa())
                    .append("activa", t.getActiva());
            col.insertOne(doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizarTarifa(Tarifa t) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Tarifas");
            Document actualizar = new Document("nombreTarifa", t.getNombreTarifa())
                    .append("precio", t.getPrecio())
                    .append("detalles", t.getDetalles())
                    .append("tipoTarifa", t.getTipoTarifa())
                    .append("activa", t.getActiva());

            return col.updateOne(getFiltroId(t.getId()), new Document("$set", actualizar)).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminarTarifa(Tarifa t) {
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Tarifas");
            return col.deleteOne(getFiltroId(t.getId())).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
