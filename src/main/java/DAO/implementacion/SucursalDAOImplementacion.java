package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
import DAO.SucursalDAO;
import DAO.MongoDBConexion;
import modelo.Sucursal;
import modelo.Ubicacion;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la interfaz {@link SucursalDAO} encargada de gestionar las
 * operaciones CRUD (inserción, actualización, eliminación y consulta) de las
 * sucursales y sus ubicaciones embebidas en la base de datos MongoDB.
 *
 * @author Eneko.Plaza
 */
public class SucursalDAOImplementacion implements SucursalDAO {

    /**
     * Registrador de eventos (Logger) para trazar las operaciones y errores de
     * la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(SucursalDAOImplementacion.class.getName());

    /**
     * Convierte un objeto del modelo {@link Ubicacion} en un {@link Document}
     * BSON embebido listo para almacenarse en MongoDB, respetando las claves
     * definidas en el esquema de validación ({@code Direccion}, {@code Localidad}
     * y {@code CodigoPostal}).
     *
     * @param u Objeto {@link Ubicacion} que contiene dirección, localidad y
     *          código postal.
     * @return Un {@link Document} con los datos de la ubicación o valores por
     *         defecto si el parámetro es nulo.
     */
    private Document ubicacionADocumento(Ubicacion u) {
        Document doc = new Document();
        if (u == null) {
            doc.append("Direccion", "");
            doc.append("Localidad", "");
            doc.append("CodigoPostal", 0);
        } else {
            doc.append("Direccion", u.getDireccion());
            doc.append("Localidad", u.getLocalidad());
            doc.append("CodigoPostal", u.getCodigoPostal());
        }
        return doc;
    }

    /**
     * Calcula el siguiente identificador numérico disponible para una nueva
     * sucursal buscando el valor máximo del campo {@code id} en la colección
     * {@code Sucursales} e incrementándolo en una unidad.
     *
     * @return El siguiente número entero disponible para asignar como
     *         identificador.
     * @throws OurException Si ocurre un error al consultar la colección en
     *                      MongoDB.
     */
    @Override
    public int siguienteIdSucursal() throws OurException {
        try {
            LOGGER.info("Calcular el id de la siguiente sucursal");
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            List<Document> listaDocs = col.find().into(new ArrayList<>());
            int maxId = 0;
            for (int i = 0; i < listaDocs.size(); i++) {
                Document d = listaDocs.get(i);
                Integer id = d.getInteger("id");
                if (id != null) {
                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
            return maxId + 1;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en siguienteIdSucursal", e);
            throw new OurException(ErrorMessages.GET_SUCURSALES);
        }
    }

    /**
     * Registra una nueva sucursal en la colección {@code Sucursales},
     * convirtiendo el teléfono a cadena de texto e incluyendo su ubicación
     * embebida bajo el campo {@code Ubicacion}.
     *
     * @param s Objeto {@link Sucursal} con los datos de la nueva sucursal a
     *          insertar.
     * @return {@code true} si la inserción se ejecutó correctamente.
     * @throws OurException Si se produce un fallo al guardar la sucursal en
     *                      MongoDB.
     */
    @Override
    public boolean insertarSucursal(Sucursal s) throws OurException {
        try {
            LOGGER.info("Insertar sucursal " + s.getNombre() + ", ubicación: " + s.getUbicacion() + ", teléfono: " + s.getTelefono());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            Document doc = new Document();
            doc.append("nombre", s.getNombre());
            doc.append("horario", s.getHorario());
            doc.append("telefono", String.valueOf(s.getTelefono()));
            doc.append("Ubicacion", ubicacionADocumento(s.getUbicacion()));
            col.insertOne(doc);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en insertarSucursal", e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    /**
     * Actualiza el horario, el teléfono y la ubicación de una sucursal
     * existente identificándola por su campo {@code nombre}.
     *
     * @param s Objeto {@link Sucursal} con el nombre identificador y los nuevos
     *          valores a actualizar.
     * @return {@code true} si se encontró y actualizó la sucursal;
     *         {@code false} si no existe ninguna sucursal con ese nombre.
     * @throws OurException Si ocurre un error durante la actualización en
     *                      MongoDB.
     */
    @Override
    public boolean actualizarSucursal(Sucursal s) throws OurException {
        try {
            LOGGER.info("Actualizar sucursal " + s.getNombre() + ", ubicación: " + s.getUbicacion() + ", teléfono: " + s.getTelefono());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            Document cambios = new Document();
            cambios.append("horario", s.getHorario());
            cambios.append("telefono", String.valueOf(s.getTelefono()));
            cambios.append("Ubicacion", ubicacionADocumento(s.getUbicacion()));

            Document filtro = new Document("nombre", s.getNombre());
            Document actualizacion = new Document("$set", cambios);
            long modificados = col.updateOne(filtro, actualizacion).getMatchedCount();
            if (modificados > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en actualizarSucursal", e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    /**
     * Elimina una sucursal de la colección {@code Sucursales} filtrando por su
     * nombre exacto.
     *
     * @param nombre Nombre de la sucursal que se desea eliminar.
     * @return {@code true} si se eliminó al menos un documento; {@code false}
     *         si no se encontró la sucursal.
     * @throws OurException Si ocurre un error durante el borrado en la base de
     *                      datos.
     */
    @Override
    public boolean eliminarSucursal(String nombre) throws OurException {
        try {
            LOGGER.info("Eliminar sucursal " + nombre);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            Document filtro = new Document("nombre", nombre);
            long borrados = col.deleteOne(filtro).getDeletedCount();
            if (borrados > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en eliminarSucursal", e);
            throw new OurException(ErrorMessages.DELETE_SUCURSAL);
        }
    }

    /**
     * Recupera todas las sucursales registradas en la colección
     * {@code Sucursales} y las mapea a objetos del modelo {@link Sucursal},
     * soportando tanto claves en mayúscula como en minúscula en el subdocumento
     * de ubicación y normalizando el formato del número de teléfono.
     *
     * @return Una {@link List} con todas las instancias de {@link Sucursal}
     *         encontradas.
     * @throws OurException Si ocurre un error al consultar o transformar los
     *                      documentos de MongoDB.
     */
    @Override
    public List<Sucursal> listarSucursales() throws OurException {
        LOGGER.info("Listar sucursales");
        List<Sucursal> resultado = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            List<Document> listaDocs = col.find().into(new ArrayList<>());

            for (int i = 0; i < listaDocs.size(); i++) {
                Document d = listaDocs.get(i);

                String nombre = "";
                if (d.getString("nombre") != null) {
                    nombre = d.getString("nombre");
                }

                Document u = d.get("Ubicacion", Document.class);
                if (u == null) {
                    u = d.get("ubicacion", Document.class);
                }

                String dir = "";
                String loc = "";
                int cp = 0;

                if (u != null) {
                    if (u.getString("Direccion") != null) {
                        dir = u.getString("Direccion");
                    } else if (u.getString("direccion") != null) {
                        dir = u.getString("direccion");
                    }

                    if (u.getString("Localidad") != null) {
                        loc = u.getString("Localidad");
                    } else if (u.getString("localidad") != null) {
                        loc = u.getString("localidad");
                    }

                    if (u.getInteger("CodigoPostal") != null) {
                        cp = u.getInteger("CodigoPostal");
                    } else if (u.getInteger("codigoPostal") != null) {
                        cp = u.getInteger("codigoPostal");
                    }
                }

                Object telObj = d.get("telefono");
                int telefono = 0;
                if (telObj != null) {
                    if (telObj instanceof Number) {
                        telefono = ((Number) telObj).intValue();
                    } else {
                        try {
                            telefono = Integer.parseInt(telObj.toString().replaceAll("\\s+", ""));
                        } catch (NumberFormatException e) {
                        }
                    }
                }

                String horario = "";
                if (d.getString("horario") != null) {
                    horario = d.getString("horario");
                }

                Ubicacion ubicacionObj = new Ubicacion(dir, loc, cp);
                Sucursal suc = new Sucursal(nombre, horario, telefono, ubicacionObj);
                resultado.add(suc);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarSucursales", e);
            throw new OurException(ErrorMessages.GET_SUCURSALES);
        }
        return resultado;
    }
}