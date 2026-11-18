/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
import DAO.EmpleadoDAO;
import DAO.MongoDBConexion;
import modelo.Empleado;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Implementación de la interfaz {@link EmpleadoDAO} encargada de gestionar las
 * operaciones de autenticación, consulta, alta, baja y promoción de empleados
 * en MongoDB.
 *
 * @author Eneko.Plaza
 */
public class EmpleadoDAOImplementacion implements EmpleadoDAO {

    /**
     * Registrador de eventos (Logger) para trazar las operaciones y errores de
     * la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(EmpleadoDAOImplementacion.class.getName());

    /**
     * Verifica las credenciales de inicio de sesión buscando secuencialmente en
     * las colecciones {@code Administradores}, {@code Empleados} y
     * {@code Clientes}. Si encuentra coincidencia, añade el campo {@code rol}
     * correspondiente al documento devuelto.
     *
     * @param usuario Nombre de usuario utilizado para autenticarse.
     * @param contrasena Contraseña asociada al usuario.
     * @return El {@link Document} del usuario autenticado con su respectivo rol
     * asignado, o {@code null} si las credenciales no coinciden en ninguna
     * colección.
     * @throws OurException Si ocurre un error durante la consulta a la base de
     * datos.
     */
    @Override
    public Document comprobarUsuarios(String usuario, String contrasena) throws OurException {
        try {
            LOGGER.info("Buscar en Empleados al usuario: " + usuario);
            MongoDatabase db = MongoDBConexion.getDatabase();
            Document query = new Document("nombreUsuario", usuario).append("contrasena", contrasena);

            Document admin = db.getCollection("Administradores").find(query).first();
            if (admin != null) {
                admin.append("rol", "Admin");
                return admin;
            }

            Document empleado = db.getCollection("Empleados").find(query).first();
            if (empleado != null) {
                empleado.append("rol", "Empleado");
                return empleado;
            }

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
     * Recupera todos los documentos de la colección {@code Empleados} y los
     * mapea a instancias del modelo {@link Empleado}, asignando valores por
     * defecto ante campos nulos.
     *
     * @return Una {@link List} con todos los objetos {@link Empleado}
     * registrados en MongoDB.
     * @throws OurException Si ocurre un error al consultar o mapear los datos
     * de los empleados.
     */
    @Override
    public List<Empleado> listarEmpleados() throws OurException {
        LOGGER.info("Listar empleados");
        List<Empleado> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Empleados");
            MongoCollection<Document> colSucursales = db.getCollection("Sucursales");

            List<Document> docs = col.find().into(new ArrayList<>());
            for (int i = 0; i < docs.size(); i++) {
                Document doc = docs.get(i);

                String id = doc.getObjectId("_id").toString();
                String usuario = doc.getString("nombreUsuario");
                String nombre = doc.getString("nombre");
                String apellidos = doc.getString("apellidos");
                String contrasena = doc.getString("contrasena");
                String direccion = doc.getString("direccion");
                String correo = doc.getString("correo");
                String rol = doc.getString("rol");
                String iban = doc.getString("cuentaBancaria");

                String dirSucursal = "Sin sucursal";
                Object sucObj = doc.get("sucursalId");
                if (sucObj instanceof ObjectId) {
                    Document sucDoc = colSucursales.find(Filters.eq("_id", sucObj)).first();
                    if (sucDoc != null) {
                        Document ubi = sucDoc.get("Ubicacion", Document.class);
                        if (ubi != null) {
                            dirSucursal = ubi.getString("Direccion") + ", " + ubi.getString("Localidad");
                        }
                    }
                }

                lista.add(new Empleado(id, usuario, nombre, apellidos, contrasena, direccion, correo, rol, iban, dirSucursal));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarEmpleados", e);
            throw new OurException(ErrorMessages.GET_EMPLEADOS);
        }
        return lista;
    }

    /**
     * Registra un nuevo empleado en la colección {@code Empleados} con el rol
     * {@code "Empleado"} por defecto, siempre que el nombre de usuario no esté
     * ya ocupado.
     *
     * @param usuario Nombre de usuario único para el empleado.
     * @param nombre Nombre de pila del empleado.
     * @param apellidos Apellidos del empleado.
     * @param contrasena Contraseña de acceso al sistema.
     * @param correo Dirección de correo electrónico del empleado.
     * @param cuentaBancaria Número de cuenta bancaria (IBAN) del empleado.
     * @return {@code true} si el empleado se insertó correctamente;
     * {@code false} si ya existía un registro con ese nombre de usuario.
     * @throws OurException Si ocurre un error al insertar el documento en la
     * base de datos.
     */
    @Override
    public boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria) throws OurException {
        try {
            LOGGER.info("Crear empleado, usuario: " + usuario + ", nombre: " + nombre + " " + apellidos + ", correo: " + correo);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            if (colEmp.find(new Document("nombreUsuario", usuario)).first() != null) {
                return false;
            }

            Document nuevoEmp = new Document("nombreUsuario", usuario)
                    .append("nombre", nombre)
                    .append("apellidos", apellidos)
                    .append("contrasena", contrasena)
                    .append("correo", correo)
                    .append("cuentaBancaria", cuentaBancaria)
                    .append("rol", "Empleado");

            colEmp.insertOne(nuevoEmp);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en crearEmpleado", e);
            throw new OurException(ErrorMessages.INSERT_EMPLEADO);
        }
    }

    /**
     * Elimina un empleado de la colección {@code Empleados} filtrando por su
     * nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario del empleado que se desea dar de
     * baja.
     * @return {@code true} si se eliminó el empleado; {@code false} si no se
     * encontró el registro.
     * @throws OurException Si se produce un error durante la operación de
     * borrado en MongoDB.
     */
    @Override
    public boolean eliminarEmpleado(String nombreUsuario) throws OurException {
        try {
            LOGGER.info("Eliminar empleado, usuario: " + nombreUsuario);
            MongoDatabase db = MongoDBConexion.getDatabase();
            return db.getCollection("Empleados").deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en eliminarEmpleado", e);
            throw new OurException(ErrorMessages.DELETE_EMPLEADO);
        }
    }

    /**
     * Asciende a un empleado transfiriendo su documento desde la colección
     * {@code Empleados} hacia la colección {@code Administradores} y
     * actualizando su atributo {@code rol}.
     *
     * @param nombreUsuario Nombre de usuario del empleado que será ascendido.
     * @param nuevoRol Nuevo rol que se le asignará al usuario tras el ascenso.
     * @return {@code true} si el empleado existía y fue movido correctamente;
     * {@code false} si no se encontró al empleado.
     * @throws OurException Si ocurre un error durante la transferencia entre
     * colecciones.
     */
    @Override
    public boolean ascenderEmpleado(String nombreUsuario, String nuevoRol) throws OurException {
        try {
            LOGGER.info("Cambiar rol del empleado " + nombreUsuario + " a " + nuevoRol);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            Document emp = colEmp.find(Filters.eq("nombreUsuario", nombreUsuario)).first();
            if (emp != null) {
                colEmp.deleteOne(Filters.eq("nombreUsuario", nombreUsuario));
                emp.append("rol", nuevoRol);
                db.getCollection("Administradores").insertOne(emp);
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en ascenderEmpleado", e);
            throw new OurException(ErrorMessages.ASCENDER_EMPLEADO);
        }
        return false;
    }
}
