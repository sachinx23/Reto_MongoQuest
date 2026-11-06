/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
import DAO.ValoracionDAO;
import DAO.MongoDBConexion;
import modelo.Valoracion;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import com.mongodb.MongoException;
/**
 *
 * @author Eneko.Plaza
 */
public class ValoracionDAOImplementacion extends DAOBase implements ValoracionDAO {

    private static final Logger LOGGER = Logger.getLogger(ValoracionDAOImplementacion.class.getName());

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
    public List<Valoracion> listarValoraciones() throws OurException {
        try {
            LOGGER.info("Listar valoraciones");
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarValoraciones", e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public List<Valoracion> listarValoracionesPorPuntuacion(int puntuacion) throws OurException {
        try {
            LOGGER.info("Listar valoraciones con puntuación " + puntuacion);
            List<Valoracion> lista = new ArrayList<Valoracion>();
            List<Document> docs = getColeccion().find(Filters.eq("puntuacion", puntuacion)).into(new ArrayList<Document>());

            for (int i = 0; i < docs.size(); i++) {
                Document d = docs.get(i);
                Valoracion v = mapearValoracion(d);
                lista.add(v);
            }
            return lista;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarValoracionesPorPuntuacion", e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public double mediaValoraciones() throws OurException {
        try {
            LOGGER.info("Calcular la media de las valoraciones");
            List<Document> docs = getColeccion().find().into(new ArrayList<Document>());
            if (docs.isEmpty()) {
                return 0;
            }

            double suma = 0;
            for (int i = 0; i < docs.size(); i++) {
                suma = suma + docs.get(i).getInteger("puntuacion");
            }
            return suma / docs.size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en mediaValoraciones", e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public boolean eliminarValoracion(Valoracion v) throws OurException {
        try {
            LOGGER.info("Eliminar valoración " + v.getId());
            long borrados = getColeccion().deleteOne(getFiltroId(v.getId())).getDeletedCount();
            if (borrados > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en eliminarValoracion", e);
            throw new OurException(ErrorMessages.DELETE_VALORACION);
        }
    }

    // Parte cliente

    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        LOGGER.info("Listar valoraciones del cliente " + idCliente);
        try {
            List<Valoracion> valoraciones = buscarValoraciones(new Document("idCliente", new ObjectId(idCliente)));
            return valoraciones;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerValoracionesCliente", e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        LOGGER.info("Insertar valoración de " + valoracion.getPuntuacion() + " estrellas del cliente " + valoracion.getIdCliente() + " para la tarifa " + valoracion.getIdTarifa());
        try {
            Document d = new Document();
            d.put("puntuacion", valoracion.getPuntuacion());
            d.put("texto", valoracion.getTexto());
            d.put("idCliente", new ObjectId(valoracion.getIdCliente()));
            d.put("idTarifa", aObjectId(valoracion.getIdTarifa()));
            coleccion(COL_VALORACIONES).insertOne(d);

            valoracion.setId(idTexto(d, "_id"));
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en insertarValoracion", e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void actualizarValoracion(Valoracion valoracion) throws OurException {
        LOGGER.info("Actualizar valoración " + valoracion.getId() + " a " + valoracion.getPuntuacion() + " estrellas");
        try {
            Document cambios = new Document();
            cambios.put("puntuacion", valoracion.getPuntuacion());
            cambios.put("texto", valoracion.getTexto());
            if (!modificar(COL_VALORACIONES, valoracion.getId(), cambios)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en actualizarValoracion", e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    private List<Valoracion> buscarValoraciones(Document filtro) throws OurException {
        List<Valoracion> valoraciones = new ArrayList<>();
        for (Document d : coleccion(COL_VALORACIONES).find(filtro)) {
            Valoracion v = new Valoracion(idTexto(d, "_id"), (int) numero(d, "puntuacion"), texto(d, "texto"),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"), "", "");
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                v.setNombreCliente(texto(cliente, "nombre"));
            }
            valoraciones.add(v);
        }
        return valoraciones;
    }
}
