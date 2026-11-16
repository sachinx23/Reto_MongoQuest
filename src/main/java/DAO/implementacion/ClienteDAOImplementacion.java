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
 * Implementación de la interfaz {@link ClienteDAO} que extiende {@link DAOBase} para
 * gestionar las operaciones CRUD y de autenticación de los clientes en MongoDB.
 *
 * @author Eneko.Plaza
 */
public class ClienteDAOImplementacion extends DAOBase implements ClienteDAO {

    /**
     * Registrador de eventos (Logger) para trazar las operaciones y errores de la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(ClienteDAOImplementacion.class.getName());

    /**
     * Verifica las credenciales de inicio de sesión de un cliente en la colección {@code Clientes}.
     * Si el usuario existe y la contraseña coincide, añade el atributo {@code rol} con valor
     * {@code "Cliente"} al documento devuelto.
     *
     * @param usuario    Nombre de usuario utilizado para autenticarse.
     * @param contrasena Contraseña asociada a la cuenta del cliente.
     * @return El {@link Document} del cliente autenticado con el rol asignado,
     *         o {@code null} si las credenciales son incorrectas o no existen.
     * @throws OurException Si ocurre un error durante la consulta a la base de datos.
     */
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

    /**
     * Actualiza los datos personales y bancarios de un cliente existente en MongoDB,
     * validando previamente que el nuevo nombre de usuario y el DNI no pertenezcan a otro registro.
     *
     * @param c Objeto {@link Cliente} que contiene el identificador y los nuevos datos a actualizar.
     * @return {@code true} si se modificó al menos un documento en la base de datos;
     *         {@code false} si no se realizaron cambios o no se encontró el ID.
     * @throws OurException Si el nombre de usuario o el DNI ya están en uso por otro cliente,
     *                      o si se produce un error durante la actualización.
     */
    @Override
    public boolean actualizarCliente(Cliente c) throws OurException {
        try {
            LOGGER.info("Actualizar cliente " + c.getId() + ", usuario: " + c.getNombreUsuario() + ", DNI: " + c.getDni() + ", correo: " + c.getCorreo());
            if (nombreUsuarioOcupado(c.getNombreUsuario(), c.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            Document otro = coleccion(COL_CLIENTES).find(new Document("dni", c.getDni())).first();
            if (otro != null && !idTexto(otro, "_id").equals(c.getId())) {
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Clientes");

            Object idFiltro = c.getId();
            if (ObjectId.isValid(c.getId())) {
                idFiltro = new ObjectId(c.getId());
            }

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

            long modificados = col.updateOne(new Document("_id", idFiltro), sentenciaUpdate).getModifiedCount();
            
            return modificados > 0;
        } catch (OurException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en actualizarCliente", e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }

    /**
     * Registra un nuevo cliente en la colección {@code Clientes} con el rol asignado por defecto,
     * comprobando antes que el nombre de usuario no esté registrado.
     *
     * @param usuario    Nombre de usuario único para la cuenta.
     * @param nombre     Nombre de pila del cliente.
     * @param apellidos  Apellidos del cliente.
     * @param contrasena Contraseña de acceso.
     * @param correo     Dirección de correo electrónico.
     * @param dni        Documento Nacional de Identidad del cliente.
     * @param telefono   Número de teléfono de contacto.
     * @param direccion  Dirección postal del cliente.
     * @return {@code true} si el cliente fue registrado con éxito;
     *         {@code false} si ya existía un usuario con ese nombre.
     * @throws OurException Si ocurre un fallo al insertar el documento en la base de datos.
     */
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

    /**
     * Elimina completamente el registro y los datos sensibles de un cliente en la colección
     * {@code Clientes} filtrando por su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario del cliente cuyos datos se desean eliminar.
     * @return {@code true} si se eliminó el documento del cliente; {@code false} si no se encontró.
     * @throws OurException Si se produce un error durante la operación de borrado.
     */
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

    /**
     * Obtiene todos los clientes registrados en la base de datos y los mapea a una lista
     * de objetos del modelo {@link Cliente}.
     *
     * @return Una {@link List} con todos los objetos {@link Cliente} encontrados.
     * @throws OurException Si ocurre un error al consultar o mapear los documentos de MongoDB.
     */
   @Override
    public List<Cliente> listarClientes() throws OurException {
        LOGGER.info("Listar clientes");
        List<Cliente> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Clientes");

            List<Document> documentos = col.find().into(new ArrayList<>());
            for (int i = 0; i < documentos.size(); i++) {
                Document doc = documentos.get(i);
                String usuario = doc.getString("nombreUsuario");
                String nombre = doc.getString("nombre");
                String apellidos = doc.getString("apellidos");
                String correo = doc.getString("correo");
                String dni = doc.getString("dni");

                Object telObj = doc.get("telefono");
                String telefono = "";
                if (telObj != null) {
                    telefono = telObj.toString();
                }

                Object dirObj = doc.get("direccion");
                String direccion = "";
                if (dirObj instanceof Document) {
                    Document dirDoc = (Document) dirObj;
                    String calle = dirDoc.getString("calle");
                    String localidad = dirDoc.getString("localidad");
                    Integer cp = dirDoc.getInteger("codigoPostal");

                    if (calle != null) {
                        direccion = direccion + calle;
                    }
                    if (localidad != null) {
                        if (!direccion.isEmpty()) {
                            direccion = direccion + ", ";
                        }
                        direccion = direccion + localidad;
                    }
                    if (cp != null) {
                        direccion = direccion + " (" + cp + ")";
                    }
                } else if (dirObj instanceof String) {
                    direccion = (String) dirObj;
                }

                lista.add(new Cliente(usuario, nombre, apellidos, correo, dni, telefono, direccion));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarClientes", e);
            throw new OurException(ErrorMessages.GET_CLIENTES);
        }
        return lista;
    }

    /**
     * Busca y recupera un cliente específico mediante su identificador único de MongoDB ({@link ObjectId}).
     *
     * @param id Cadena hexadecimal que representa el {@link ObjectId} del cliente.
     * @return El objeto {@link Cliente} correspondiente al identificador, o {@code null} si no existe.
     * @throws OurException Si el formato del {@code id} no es un {@link ObjectId} válido o si falla la consulta.
     */
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