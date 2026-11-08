/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
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
import com.mongodb.MongoException;
/**
 *
 * @author Eneko.Plaza
 */
public class TarifaDAOImplementacion extends DAOBase implements TarifaDAO {

    private static final Logger LOGGER = Logger.getLogger(TarifaDAOImplementacion.class.getName());
private Bson getFiltroId(String idStr) {
        if (idStr != null && ObjectId.isValid(idStr)) {
            return Filters.or(Filters.eq("_id", new ObjectId(idStr)), Filters.eq("_id", idStr));
        }
        return Filters.eq("_id", idStr);
    }

    @Override
    public List<Tarifa> listarTarifas() throws OurException {
        LOGGER.info("Listar tarifas");
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
            LOGGER.log(Level.SEVERE, "Error en listarTarifas", e);
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
        return resultado;
    }

    @Override
    public boolean insertarTarifa(Tarifa t) throws OurException {
        try {
            LOGGER.info("Insertar tarifa " + t.getNombreTarifa() + ", precio: " + t.getPrecio() + ", tipo: " + t.getTipoTarifa());
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
            LOGGER.log(Level.SEVERE, "Error en insertarTarifa", e);
            throw new OurException(ErrorMessages.SAVE_TARIFA);
        }
    }

    @Override
    public boolean actualizarTarifa(Tarifa t) throws OurException {
        try {
            LOGGER.info("Actualizar tarifa " + t.getId() + ", nombre: " + t.getNombreTarifa() + ", precio: " + t.getPrecio() + ", activa: " + t.getActiva());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Tarifas");
            Document actualizar = new Document("nombreTarifa", t.getNombreTarifa())
                    .append("precio", t.getPrecio())
                    .append("detalles", t.getDetalles())
                    .append("tipoTarifa", t.getTipoTarifa())
                    .append("activa", t.getActiva());

            return col.updateOne(getFiltroId(t.getId()), new Document("$set", actualizar)).getModifiedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en actualizarTarifa", e);
            throw new OurException(ErrorMessages.SAVE_TARIFA);
        }
    }

    @Override
    public boolean eliminarTarifa(Tarifa t) throws OurException {
        try {
            LOGGER.info("Eliminar tarifa " + t.getId() + ", nombre: " + t.getNombreTarifa());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Tarifas");
            return col.deleteOne(getFiltroId(t.getId())).getDeletedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en eliminarTarifa", e);
            throw new OurException(ErrorMessages.DELETE_TARIFA);
        }
    }

    // Parte cliente

    @Override
    public Tarifa buscarTarifa(String idTarifa) throws OurException {
        LOGGER.info("Buscar tarifa con id: " + idTarifa);
        try {
            Document d = buscarPorId(COL_TARIFAS, aObjectId(idTarifa));
            if (d == null) {
                return null;
            }
            return documentoATarifa(d);
        } catch (MongoException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error en buscarTarifa", e);
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
    }

    @Override
    public List<Tarifa> tarifasActivas() throws OurException {
        LOGGER.info("Listar tarifas activas");
        // Usa el DAO de Eneko y quita las tarifas desactivadas
        List<Tarifa> activas = new ArrayList<>();
        for (Tarifa t : listarTarifas()) {
            if (t.getActiva()) {
                activas.add(t);
            }
        }
        return activas;
    }
}
