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
 * Implementación de la interfaz {@link ValoracionDAO} que extiende {@link DAOBase} para
 * gestionar las operaciones CRUD, el filtrado por puntuación y el cálculo de estadísticas
 * de las valoraciones de los clientes en MongoDB.
 *
 * @author Eneko.Plaza
 */
public class ValoracionDAOImplementacion extends DAOBase implements ValoracionDAO {

    /**
     * Registrador de eventos (Logger) para trazar las operaciones y errores de la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(ValoracionDAOImplementacion.class.getName());

    /**
     * Obtiene la referencia directa a la colección {@code Valoraciones} en MongoDB.
     *
     * @return Instancia de {@link MongoCollection} tipada con {@link Document}.
     */
    private MongoCollection<Document> getColeccion() {
        return MongoDBConexion.getColeccion("Valoraciones");
    }

    /**
     * Construye un documento de filtro para buscar por el campo {@code _id},
     * intentando convertir la cadena a {@link ObjectId} o usándola como {@link String} si no es válida.
     *
     * @param idStr Identificador del documento en formato texto.
     * @return Un {@link Document} configurado con la clave {@code _id}.
     */
    private Document getFiltroId(String idStr) {
        try {
            return new Document("_id", new ObjectId(idStr));
        } catch (Exception e) {
            return new Document("_id", idStr);
        }
    }

    /**
     * Convierte un {@link Document} de la colección {@code Valoraciones} en una instancia
     * del modelo {@link Valoracion}.
     *
     * @param d Documento BSON recuperado de MongoDB.
     * @return Objeto {@link Valoracion} poblado con los datos básicos del documento.
     */
    @Override
    public Valoracion mapearValoracion(Document d) {
        Valoracion v = new Valoracion();

        if (d.get("_id") != null) {
            v.setId(d.get("_id").toString());
        } else {
            v.setId("");
        }

        if (d.getInteger("puntuacion") != null) {
            v.setPuntuacion(d.getInteger("puntuacion"));
        } else {
            v.setPuntuacion(0);
        }

        v.setTexto(d.getString("texto"));

        if (d.get("cliente_id") != null) {
            v.setIdCliente(d.get("cliente_id").toString());
        } else {
            v.setIdCliente("");
        }

        if (d.get("tarifa_id") != null) {
            v.setIdTarifa(d.get("tarifa_id").toString());
        } else {
            v.setIdTarifa("");
        }

        return v;
    }

    /**
     * Recupera todas las valoraciones registradas en MongoDB y enriquece cada objeto
     * consultando los nombres asociados del cliente y de la tarifa (asignando textos de
     * respaldo si alguno de ellos hubiera sido eliminado).
     *
     * @return Una {@link List} con todas las instancias de {@link Valoracion} completas.
     * @throws OurException Si ocurre un error durante la consulta o el cruce de colecciones.
     */
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

    /**
     * Filtra y recupera las valoraciones que coinciden exactamente con una puntuación numérica.
     *
     * @param puntuacion Valor numérico de estrellas/puntuación por el cual filtrar.
     * @return Una {@link List} de objetos {@link Valoracion} con dicha puntuación.
     * @throws OurException Si ocurre un error al consultar la base de datos.
     */
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

    /**
     * Calcula la puntuación media aritmética de todas las valoraciones almacenadas en el sistema.
     *
     * @return El promedio de puntuación como {@code double}, o {@code 0} si no existen valoraciones.
     * @throws OurException Si se produce un fallo al leer las valoraciones desde MongoDB.
     */
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

    /**
     * Elimina una valoración de la colección {@code Valoraciones} a partir de su identificador.
     *
     * @param v Objeto {@link Valoracion} que contiene el identificador del registro a borrar.
     * @return {@code true} si se eliminó el documento; {@code false} si no existía.
     * @throws OurException Si ocurre un error durante la operación de borrado en MongoDB.
     */
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

    /**
     * Recupera todas las valoraciones realizadas por un cliente específico.
     *
     * @param idCliente Identificador hexadecimal ({@link ObjectId}) del cliente.
     * @return Una {@link List} con las {@link Valoracion} publicadas por dicho cliente.
     * @throws OurException Si ocurre un error de MongoDB durante la búsqueda.
     */
    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        LOGGER.info("Listar valoraciones del cliente " + idCliente);
        try {
            List<Valoracion> valoraciones = buscarValoraciones(new Document("cliente_id", new ObjectId(idCliente)));
            return valoraciones;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerValoracionesCliente", e);
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    /**
     * Inserta una nueva valoración en la colección {@code Valoraciones} y actualiza
     * el identificador {@code _id} generado en el objeto {@link Valoracion} recibido.
     *
     * @param valoracion Objeto {@link Valoracion} con la puntuación, comentario, cliente y tarifa asociados.
     * @throws OurException Si se produce un error de MongoDB al guardar la valoración.
     */
    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        LOGGER.info("Insertar valoración de " + valoracion.getPuntuacion() + " estrellas del cliente " + valoracion.getIdCliente() + " para la tarifa " + valoracion.getIdTarifa());
        try {
            Document d = new Document();
            d.put("puntuacion", valoracion.getPuntuacion());
            d.put("texto", valoracion.getTexto());
            d.put("cliente_id", new ObjectId(valoracion.getIdCliente()));
            d.put("tarifa_id", aObjectId(valoracion.getIdTarifa()));
            coleccion(COL_VALORACIONES).insertOne(d);

            valoracion.setId(idTexto(d, "_id"));
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en insertarValoracion", e);
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    /**
     * Actualiza la puntuación y el texto de comentario de una valoración existente.
     *
     * @param valoracion Objeto {@link Valoracion} con el identificador del registro y los nuevos datos.
     * @throws OurException Si no se encuentra la valoración a modificar o falla la operación en MongoDB.
     */
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

    /**
     * Método auxiliar que busca valoraciones según un filtro BSON dado y completa
     * el nombre del autor consultando la colección {@code Clientes}.
     *
     * @param filtro Documento BSON con los criterios de búsqueda.
     * @return Una {@link List} de objetos {@link Valoracion} coincidentes.
     * @throws OurException Si ocurre un error al consultar las colecciones en MongoDB.
     */
    private List<Valoracion> buscarValoraciones(Document filtro) throws OurException {
        List<Valoracion> valoraciones = new ArrayList<Valoracion>();
        List<Document> docs = coleccion(COL_VALORACIONES).find(filtro).into(new ArrayList<Document>());
        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            Valoracion v = new Valoracion(idTexto(d, "_id"), (int) numero(d, "puntuacion"), texto(d, "texto"),
                    idTexto(d, "cliente_id"), idTexto(d, "tarifa_id"), "", "");
            Document cliente = buscarPorId(COL_CLIENTES, d.get("cliente_id"));
            if (cliente != null) {
                v.setNombreCliente(texto(cliente, "nombre"));
            }
            valoraciones.add(v);
        }
        return valoraciones;
    }
}