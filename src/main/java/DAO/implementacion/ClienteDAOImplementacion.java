/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
import DAO.ClienteDAO;
import DAO.MongoDBConexion;
import modelo.Cliente;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import com.mongodb.MongoException;
/**
 *
 * @author Eneko.Plaza
 */
public class ClienteDAOImplementacion extends DAOBase implements ClienteDAO {

    private static final Logger LOGGER = Logger.getLogger(ClienteDAOImplementacion.class.getName());

    @Override
    public Document comprobarUsuarios(String usuario, String contrasena) throws OurException {
        try {
            LOGGER.info("Buscar en Clientes al usuario: " + usuario);
            MongoDatabase db = MongoDBConexion.getDatabase();
            Document query = new Document("nombreUsuario", usuario).append("contrasena", contrasena);
            
            Document cliente = db.getCollection("Clientes").find(query).first();
            if (cliente != null) {
                cliente.append("rol", "Cliente");
                return cliente;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en comprobarUsuarios", e);
            throw new OurException(ErrorMessages.LOGIN);
        }
        return null;
    }

    @Override
    public boolean actualizarCliente(Cliente c) throws OurException {
        try {
            LOGGER.info("Actualizar cliente " + c.getId() + ", usuario: " + c.getNombreUsuario() + ", DNI: " + c.getDni() + ", correo: " + c.getCorreo());
            // Usuario o DNI repetidos
            if (nombreUsuarioOcupado(c.getNombreUsuario(), c.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            Document otro = coleccion(COL_CLIENTES).find(new Document("dni", c.getDni())).first();
            if (otro != null && !idTexto(otro, "_id").equals(c.getId())) {
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Clientes");

            // Comprobar si el ID es un ObjectId válido de MongoDB
            Object idFiltro = c.getId();
            if (ObjectId.isValid(c.getId())) {
                idFiltro = new ObjectId(c.getId());
            }

            // Mapeamos todos los atributos de tu modelo Cliente al Documento de MongoDB
            Document camposNuevos = new Document("cuentaBancaria", c.getCuentaBancaria())
                    .append("nombreUsuario", c.getNombreUsuario())
                    .append("nombre", c.getNombre())
                    .append("apellidos", c.getApellidos())
                    .append("contrasena", c.getContrasena())
                    .append("correo", c.getCorreo())
                    .append("dni", c.getDni())
                    .append("telefono", c.getTelefono())
                    .append("direccion", c.getDireccion());

            Document sentenciaUpdate = new Document("$set", camposNuevos);

            // Ejecutar la actualización en la colección "Clientes"
            long modificados = col.updateOne(new Document("_id", idFiltro), sentenciaUpdate).getModifiedCount();
            
            return modificados > 0;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en actualizarCliente", e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }
    @Override
    public boolean registrarCliente(String usuario, String nombre, String apellidos, String contrasena, String correo, String dni, String telefono, String direccion) throws OurException {
        try {
            LOGGER.info("Registrar cliente, usuario: " + usuario + ", nombre: " + nombre + " " + apellidos + ", DNI: " + dni + ", correo: " + correo);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Clientes");

            if (col.find(new Document("nombreUsuario", usuario)).first() != null) {
                return false;
            }

            Document nuevo = new Document("nombreUsuario", usuario)
                    .append("nombre", nombre)
                    .append("apellidos", apellidos)
                    .append("contrasena", contrasena)
                    .append("correo", correo)
                    .append("dni", dni)
                    .append("telefono", telefono)
                    .append("direccion", direccion)
                    .append("rol", "Cliente");

            col.insertOne(nuevo);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en registrarCliente", e);
            throw new OurException(ErrorMessages.REGISTER_USER);
        }
    }

    @Override
    public boolean eliminarDatosSensiblesCliente(String nombreUsuario) throws OurException {
        try {
            LOGGER.info("Eliminar datos del cliente, usuario: " + nombreUsuario);
            MongoDatabase db = MongoDBConexion.getDatabase();
            return db.getCollection("Clientes").deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en eliminarDatosSensiblesCliente", e);
            throw new OurException(ErrorMessages.DATOS_SENSIBLES);
        }
    }

    @Override
    public List<Cliente> listarClientes() throws OurException {
        LOGGER.info("Listar clientes");
        List<Cliente> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Clientes");
            
            for (Document doc : col.find()) {
                String usuario = doc.getString("nombreUsuario");
                String nombre = doc.getString("nombre");
                String apellidos = doc.getString("apellidos");
                String correo = doc.getString("correo");
                String dni = doc.getString("dni");
                Object telObj = doc.get("telefono");
                String telefono = telObj != null ? telObj.toString() : "";
                String direccion = doc.getString("direccion");

                lista.add(new Cliente(usuario, nombre, apellidos, correo, dni, telefono, direccion));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarClientes", e);
            throw new OurException(ErrorMessages.GET_CLIENTES);
        }
        return lista;
    }

    // Parte cliente

    @Override
    public Cliente obtenerCliente(String id) throws OurException {
        LOGGER.info("Cargar cliente con id: " + id);
        try {
            Document d = buscarPorId(COL_CLIENTES, new ObjectId(id));
            if (d == null) {
                return null;
            }
            return documentoACliente(d);
        } catch (MongoException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerCliente", e);
            throw new OurException(ErrorMessages.LOGIN);
        }
    }
}
