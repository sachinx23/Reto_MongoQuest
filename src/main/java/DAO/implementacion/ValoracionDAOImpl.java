/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO.implementacion;

import DAO.ValoracionDAO;
import DAO.MongoDBConexion;
import modelo.Valoracion;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public class ValoracionDAOImpl implements ValoracionDAO {

    private MongoCollection<Document> getColeccion() {
        return MongoDBConexion.getColeccion("Valoraciones");
    }

    private Document getFiltroId(String idStr) {
        try {
            return new Document("_id", new ObjectId(idStr));
        } catch (Exception e) {
            return new Document("_id", idStr);
        }
    }

    @Override
    public Valoracion mapearValoracion(Document d) {
        Valoracion v = new Valoracion();
        v.setId(d.get("_id").toString());
        v.setPuntuacion(d.getInteger("puntuacion"));
        v.setTexto(d.getString("texto"));
        v.setIdCliente(d.get("idCliente").toString());
        v.setIdTarifa(d.get("idTarifa").toString());
        return v;
    }

    @Override
    public List<Valoracion> listarValoraciones() {
        List<Valoracion> lista = new ArrayList<Valoracion>();
        List<Document> docs = getColeccion().find().into(new ArrayList<Document>());

        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            Valoracion v = mapearValoracion(d);

            Document cliente = MongoDBConexion.getColeccion("Clientes").find(getFiltroId(v.getIdCliente())).first();
            if (cliente != null) {
                v.setNombreCliente(cliente.getString("nombre"));
            } else {
                v.setNombreCliente("Cliente eliminado");
            }

            Document tarifa = MongoDBConexion.getColeccion("Tarifas").find(getFiltroId(v.getIdTarifa())).first();
            if (tarifa != null) {
                v.setNombreTarifa(tarifa.getString("nombreTarifa"));
            } else {
                v.setNombreTarifa("Tarifa eliminada");
            }

            lista.add(v);
        }
        return lista;
    }

    @Override
    public List<Valoracion> listarValoracionesPorPuntuacion(int puntuacion) {
        List<Valoracion> lista = new ArrayList<Valoracion>();
        List<Document> docs = getColeccion().find(Filters.eq("puntuacion", puntuacion)).into(new ArrayList<Document>());

        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            Valoracion v = mapearValoracion(d);
            lista.add(v);
        }
        return lista;
    }

    @Override
    public double mediaValoraciones() {
        List<Document> docs = getColeccion().find().into(new ArrayList<Document>());
        if (docs.isEmpty()) {
            return 0;
        }

        double suma = 0;
        for (int i = 0; i < docs.size(); i++) {
            suma = suma + docs.get(i).getInteger("puntuacion");
        }
        return suma / docs.size();
    }

    @Override
    public boolean eliminarValoracion(Valoracion v) {
        long borrados = getColeccion().deleteOne(getFiltroId(v.getId())).getDeletedCount();
        if (borrados > 0) {
            return true;
        } else {
            return false;
        }
    }
}