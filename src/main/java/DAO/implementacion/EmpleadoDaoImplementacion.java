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
/**
 *
 * @author Eneko.Plaza
 */
public class EmpleadoDaoImplementacion implements EmpleadoDAO{

    private static final Logger LOGGER = Logger.getLogger(EmpleadoDaoImplementacion.class.getName());
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

    @Override
    public List<Empleado> listarEmpleados() throws OurException {
        LOGGER.info("Listar empleados");
        List<Empleado> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConexion.getDatabase();
            MongoCollection<Document> col = db.getCollection("Empleados");
            for (Document doc : col.find()) {
                String id = doc.get("_id") != null ? doc.get("_id").toString() : "";
                String usuario = doc.getString("nombreUsuario") != null ? doc.getString("nombreUsuario") : "";
                String nombre = doc.getString("nombre") != null ? doc.getString("nombre") : "";
                String apellidos = doc.getString("apellidos") != null ? doc.getString("apellidos") : "";
                String contrasena = doc.getString("contrasena") != null ? doc.getString("contrasena") : "";
                
                // 1. Añadir la lectura de la dirección de MongoDB
                String direccion = doc.getString("direccion") != null ? doc.getString("direccion") : "";
                
                String correo = doc.getString("correo") != null ? doc.getString("correo") : "";
                String rol = doc.getString("rol") != null ? doc.getString("rol") : "Empleado";
                String iban = doc.getString("cuentaBancaria") != null ? doc.getString("cuentaBancaria") : "";

                Object idSucObj = doc.get("idSucursal");
                String idSucursal = idSucObj != null ? idSucObj.toString() : "";

                // 2. Pasar 'direccion' con su coma separadora correspondiente (los 10 parámetros)
                lista.add(new Empleado(id, usuario, nombre, apellidos, contrasena, direccion, correo, rol, iban, idSucursal));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en listarEmpleados", e);
            throw new OurException(ErrorMessages.GET_EMPLEADOS);
        }
        return lista;
    }

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
