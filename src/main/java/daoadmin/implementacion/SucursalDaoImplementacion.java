/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin.implementacion;

import daocliente.MongoConnection;
import daoadmin.SucursalDAO;
import modelo.Sucursal;
import modelo.Ubicacion;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public class SucursalDaoImplementacion implements SucursalDAO{
private Document ubicacionADocumento(Ubicacion u) {
        if (u == null) {
            return new Document("direccion", "").append("localidad", "").append("codigoPostal", 0);
        }
        return new Document("direccion", u.getDireccion())
                .append("localidad", u.getLocalidad())
                .append("codigoPostal", u.getCodigoPostal());
    }

    @Override
    public int siguienteIdSucursal() {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            int maxId = 0;
            for (Document d : col.find()) {
                Integer id = d.getInteger("id");
                if (id != null && id > maxId) {
                    maxId = id;
                }
            }
            return maxId + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    @Override
    public boolean insertarSucursal(Sucursal s) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            Document doc = new Document("id", s.getId())
                    .append("horario", s.getHorario())
                    .append("telefono", s.getTelefono())
                    .append("ubicacion", ubicacionADocumento(s.getUbicacion()));
            col.insertOne(doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizarSucursal(Sucursal s) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            Document cambios = new Document("horario", s.getHorario())
                    .append("telefono", s.getTelefono())
                    .append("ubicacion", ubicacionADocumento(s.getUbicacion()));
            col.updateOne(Filters.eq("id", s.getId()), new Document("$set", cambios));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminarSucursal(int id) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            return db.getCollection("Sucursales").deleteOne(Filters.eq("id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Sucursal> listarSucursales() {
        List<Sucursal> resultado = new ArrayList<>();
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            for (Document d : col.find()) {
                Document u = d.get("ubicacion", Document.class);
                String dir = u != null && u.getString("direccion") != null ? u.getString("direccion") : "";
                String loc = u != null && u.getString("localidad") != null ? u.getString("localidad") : "";
                int cp = u != null && u.getInteger("codigoPostal") != null ? u.getInteger("codigoPostal") : 0;
                int id = d.getInteger("id", 0);

                Object telObj = d.get("telefono");
                int telefono = 0;
                if (telObj instanceof Number) {
                    telefono = ((Number) telObj).intValue();
                } else if (telObj != null) {
                    try {
                        telefono = Integer.parseInt(telObj.toString().replaceAll("\\s+", ""));
                    } catch (NumberFormatException ignored) {}
                }

                String horario = d.getString("horario") != null ? d.getString("horario") : "";
                resultado.add(new Sucursal(id, horario, telefono, new Ubicacion(dir, loc, cp)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
