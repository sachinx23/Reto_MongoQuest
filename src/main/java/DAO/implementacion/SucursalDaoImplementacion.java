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
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SucursalDaoImplementacion implements SucursalDAO {

    private static final Logger LOGGER = Logger.getLogger(SucursalDaoImplementacion.class.getName());

    private Document ubicacionADocumento(Ubicacion u) {
        Document doc = new Document();
        if (u == null) {
            doc.append("direccion", "");
            doc.append("localidad", "");
            doc.append("codigoPostal", 0);
        } else {
            doc.append("direccion", u.getDireccion());
            doc.append("localidad", u.getLocalidad());
            doc.append("codigoPostal", u.getCodigoPostal());
        }
        return doc;
    }

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

    @Override
    public boolean insertarSucursal(Sucursal s) throws OurException {
        try {
            LOGGER.info("Insertar sucursal " + s.getNombre() + ", ubicación: " + s.getUbicacion() + ", teléfono: " + s.getTelefono());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            Document doc = new Document();
            doc.append("nombre", s.getNombre());
            doc.append("horario", s.getHorario());
            doc.append("telefono", s.getTelefono());
            doc.append("ubicacion", ubicacionADocumento(s.getUbicacion()));
            col.insertOne(doc);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en insertarSucursal", e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

    @Override
    public boolean actualizarSucursal(Sucursal s) throws OurException {
        try {
            LOGGER.info("Actualizar sucursal " + s.getNombre() + ", ubicación: " + s.getUbicacion() + ", teléfono: " + s.getTelefono());
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Sucursales");
            Document cambios = new Document();
            cambios.append("horario", s.getHorario());
            cambios.append("telefono", s.getTelefono());
            cambios.append("ubicacion", ubicacionADocumento(s.getUbicacion()));
            
            Document filtro = new Document("nombre", s.getNombre());
            Document actualizacion = new Document("$set", cambios);
            col.updateOne(filtro, actualizacion);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en actualizarSucursal", e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }

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
                
                Document u = d.get("ubicacion", Document.class);
                String dir = "";
                String loc = "";
                int cp = 0;
                
                if (u != null) {
                    if (u.getString("direccion") != null) {
                        dir = u.getString("direccion");
                    }
                    if (u.getString("localidad") != null) {
                        loc = u.getString("localidad");
                    }
                    if (u.getInteger("codigoPostal") != null) {
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