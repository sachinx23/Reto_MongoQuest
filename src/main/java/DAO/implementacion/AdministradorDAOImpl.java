/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO.implementacion;

import java.util.logging.Logger;
import java.util.logging.Level;
import exception.OurException;
import exception.ErrorMessages;
import DAO.AdministradorDAO;
import DAO.MongoDBConexion;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la interfaz {@link AdministradorDAO} que gestiona las operaciones
 * de acceso a datos relacionadas con administradores, empleados, clientes y sucursales
 * en una base de datos MongoDB.
 *
 * @author Eneko.Plaza
 */
public class AdministradorDAOImpl implements AdministradorDAO {

    /**
     * Registrador de eventos (Logger) para trazar la ejecución y los errores de la clase.
     */
    private static final Logger LOGGER = Logger.getLogger(AdministradorDAOImpl.class.getName());

    /**
     * Verifica las credenciales de acceso de un usuario buscando secuencialmente en las
     * colecciones de {@code Administradores}, {@code Empleados} y {@code Clientes}.
     * Si encuentra coincidencia, añade el campo {@code rol} correspondiente al documento devuelto.
     *
     * @param usuario    Nombre de usuario utilizado para iniciar sesión.
     * @param contrasena Contraseña asociada al usuario.
     * @return El {@link Document} del usuario autenticado con su respectivo rol asignado,
     *         o {@code null} si las credenciales no coinciden en ninguna colección.
     * @throws OurException Si ocurre un error durante la consulta a la base de datos.
     */
    @Override
    public Document comprobarUsuarios(String usuario, String contrasena) throws OurException {
        try {
            LOGGER.info("Buscar en Administradores al usuario: " + usuario);
            MongoDatabase db = MongoDBConexion.getDatabase();
            Document query = new Document("nombreUsuario", usuario);
            query.append("contrasena", contrasena);

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
     * Registra un nuevo empleado en la colección {@code Empleados} siempre que no exista
     * previamente otro con el mismo nombre de usuario.
     *
     * @param usuario        Nombre de usuario único para el nuevo empleado.
     * @param nombre         Nombre de pila del empleado.
     * @param apellidos      Apellidos del empleado.
     * @param contrasena     Contraseña de acceso del empleado.
     * @param correo         Dirección de correo electrónico del empleado.
     * @param cuentaBancaria Número o identificador de cuenta bancaria del empleado.
     * @param direccion      Dirección física de residencia del empleado.
     * @param idSucursal     Identificador de la sucursal a la que pertenece.
     * @return {@code true} si el empleado fue creado e insertado correctamente;
     *         {@code false} si ya existía un empleado con ese nombre de usuario.
     * @throws OurException Si ocurre un fallo al acceder o insertar en la base de datos.
     */
    @Override
    public boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria, String direccion, String idSucursal) throws OurException {
        try {
            LOGGER.info("Crear empleado, usuario: " + usuario + ", nombre: " + nombre + " " + apellidos + ", correo: " + correo + ", sucursal: " + idSucursal);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            Document filtroBusqueda = new Document("nombreUsuario", usuario);
            if (colEmp.find(filtroBusqueda).first() != null) {
                return false; // El usuario ya existe
            }

            Document nuevoEmp = new Document("nombreUsuario", usuario);
            nuevoEmp.append("nombre", nombre);
            nuevoEmp.append("apellidos", apellidos);
            nuevoEmp.append("contrasena", contrasena);
            nuevoEmp.append("correo", correo);
            nuevoEmp.append("cuentaBancaria", cuentaBancaria);
            nuevoEmp.append("direccion", direccion);       
            nuevoEmp.append("idSucursal", idSucursal);     
            nuevoEmp.append("rol", "Empleado");

            colEmp.insertOne(nuevoEmp);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en crearEmpleado", e);
            throw new OurException(ErrorMessages.INSERT_EMPLEADO);
        }
    }

    /**
     * Elimina un empleado de la colección {@code Empleados} a partir de su nombre de usuario.
     *
     * @param nombreUsuario Nombre de usuario del empleado que se desea eliminar.
     * @return {@code true} si se eliminó al menos un registro; {@code false} si no se encontró el usuario.
     * @throws OurException Si se produce un error durante la operación de borrado en MongoDB.
     */
    @Override
    public boolean eliminarEmpleado(String nombreUsuario) throws OurException {
        try {
            LOGGER.info("Eliminar empleado, usuario: " + nombreUsuario);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> colEmp = db.getCollection("Empleados");
            long borrados = colEmp.deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount();
            if (borrados > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en eliminarEmpleado", e);
            throw new OurException(ErrorMessages.DELETE_EMPLEADO);
        }
    }

    /**
     * Asciende a un empleado moviendo su documento de la colección {@code Empleados}
     * a la colección {@code Administradores} y actualizando su campo {@code rol}.
     *
     * @param nombreUsuario Nombre de usuario del empleado a ascender.
     * @param nuevoRol      Nuevo rol que se le asignará al usuario ascendido.
     * @return {@code true} si el empleado existía y fue transferido correctamente;
     *         {@code false} si no se encontró al empleado.
     * @throws OurException Si ocurre un error durante la transferencia entre colecciones.
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

    /**
     * Recupera el listado completo de todos los usuarios registrados en el sistema
     * (administradores, empleados y clientes), etiquetando cada documento con su rol.
     *
     * @return Una {@link List} de objetos {@link Document} que contiene a todos los usuarios
     *         de las tres colecciones con su respectivo campo {@code rol}.
     * @throws OurException Si ocurre un error al consultar las colecciones en la base de datos.
     */
    @Override
    public List<Document> obtenerPlantilla() throws OurException {
        LOGGER.info("Consultar la plantilla de empleados");
        List<Document> todos = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();

            List<Document> listaAdmin = db.getCollection("Administradores").find().into(new ArrayList<>());
            int i = 0;
            while (i < listaAdmin.size()) {
                Document doc = listaAdmin.get(i);
                doc.append("rol", "Admin");
                todos.add(doc);
                i = i + 1;
            }

            List<Document> listaEmpleados = db.getCollection("Empleados").find().into(new ArrayList<>());
            int j = 0;
            while (j < listaEmpleados.size()) {
                Document doc = listaEmpleados.get(j);
                doc.append("rol", "Empleado");
                todos.add(doc);
                j = j + 1;
            }

            List<Document> listaClientes = db.getCollection("Clientes").find().into(new ArrayList<>());
            int k = 0;
            while (k < listaClientes.size()) {
                Document doc = listaClientes.get(k);
                doc.append("rol", "Cliente");
                todos.add(doc);
                k = k + 1;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerPlantilla", e);
            throw new OurException(ErrorMessages.GET_EMPLEADOS);
        }
        return todos;
    }

    /**
     * Asocia un empleado a una sucursal existente añadiendo su nombre de usuario al arreglo
     * {@code empleados} del documento de la sucursal filtrada por dirección.
     *
     * @param sucursal Dirección física de la sucursal ({@code ubicacion.direccion}).
     * @param usuario  Nombre de usuario del empleado que se va a añadir.
     * @return {@code true} si la operación de actualización se ejecutó sin excepciones.
     * @throws OurException Si se produce un error al actualizar la colección {@code Sucursales}.
     */
    @Override
    public boolean agregarEmpleadoASucursal(String sucursal, String usuario) throws OurException {
        try {
            LOGGER.info("Añadir el empleado " + usuario + " a la sucursal " + sucursal);
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> colSucursales = db.getCollection("Sucursales");

            colSucursales.updateOne(
                Filters.eq("ubicacion.direccion", sucursal),
                new Document("$push", new Document("empleados", usuario))
            );
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en agregarEmpleadoASucursal", e);
            throw new OurException(ErrorMessages.SAVE_SUCURSAL);
        }
    }
}