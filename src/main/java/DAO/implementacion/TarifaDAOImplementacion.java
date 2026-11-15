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
 * Implementación de la interfaz {@link TarifaDAO} que extiende {@link DAOBase} para
 * gestionar las operaciones CRUD y la consulta de tarifas disponibles en MongoDB.
 *
 * @author Eneko.Plaza
 */
public class TarifaDAOImplementacion extends DAOBase implements TarifaDAO {

    /**
     * Registrador de eventos (Logger) para trazar las operaciones y errores de la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(TarifaDAOImplementacion.class.getName());

    /**
     * Construye un filtro BSON para buscar documentos por su campo {@code _id},
     * contemplando tanto su representación como {@link ObjectId} (si es válida)
     * como su formato en cadena de texto ({@link String}).
     *
     * @param idStr Identificador de la tarifa en formato texto.
     * @return Expresión de filtro {@link Bson} aplicable en consultas de MongoDB.
     */
    private Bson getFiltroId(String idStr) {
        if (idStr != null && ObjectId.isValid(idStr)) {
            return Filters.or(Filters.eq("_id", new ObjectId(idStr)), Filters.eq("_id", idStr));
        }
        return Filters.eq("_id", idStr);
    }

    /**
     * Recupera todas las tarifas almacenadas en la colección {@code Tarifas} (tanto activas
     * como inactivas) y las transforma en una lista de objetos del modelo {@link Tarifa}.
     *
     * @return Una {@link List} con todas las instancias de {@link Tarifa} registradas.
     * @throws OurException Si ocurre un error al consultar o mapear los documentos de MongoDB.
     */
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

    /**
     * Inserta una nueva tarifa en la colección {@code Tarifas} de MongoDB.
     *
     * @param t Objeto {@link Tarifa} con los datos (nombre, precio, detalles, tipo y estado) a registrar.
     * @return {@code true} si la inserción se realizó correctamente.
     * @throws OurException Si se produce un error al guardar la tarifa en la base de datos.
     */
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

    /**
     * Actualiza los datos y el estado de disponibilidad de una tarifa existente
     * identificándola mediante su {@code _id}.
     *
     * @param t Objeto {@link Tarifa} con el identificador y los nuevos valores a establecer.
     * @return {@code true} si se modificó el documento en MongoDB; {@code false} si no se encontró o no hubo cambios.
     * @throws OurException Si ocurre un error durante la actualización en la base de datos.
     */
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

    /**
     * Elimina una tarifa de la colección {@code Tarifas} a partir de su identificador.
     *
     * @param t Objeto {@link Tarifa} que contiene el identificador del registro a eliminar.
     * @return {@code true} si se eliminó el documento; {@code false} si no existía.
     * @throws OurException Si ocurre un error durante la operación de borrado en MongoDB.
     */
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

    /**
     * Busca y recupera una tarifa específica a partir de su identificador hexadecimal ({@link ObjectId}).
     *
     * @param idTarifa Cadena hexadecimal que representa el {@link ObjectId} de la tarifa.
     * @return El objeto {@link Tarifa} encontrado, o {@code null} si no existe ningún registro con ese ID.
     * @throws OurException Si el formato del identificador no es válido o falla la consulta en MongoDB.
     */
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

    /**
     * Obtiene únicamente las tarifas que se encuentran actualmente activas ({@code activa == true})
     * filtrando el listado completo de tarifas.
     *
     * @return Una {@link List} con las instancias de {@link Tarifa} disponibles para contratar.
     * @throws OurException Si se produce un error al recuperar el listado de tarifas.
     */
    @Override
    public List<Tarifa> tarifasActivas() throws OurException {
        LOGGER.info("Listar tarifas activas");
        List<Tarifa> activas = new ArrayList<>();
        for (Tarifa t : listarTarifas()) {
            if (t.getActiva()) {
                activas.add(t);
            }
        }
        return activas;
    }
}