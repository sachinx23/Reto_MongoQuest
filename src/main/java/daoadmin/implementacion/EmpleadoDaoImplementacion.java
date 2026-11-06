/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin.implementacion;

import daocliente.MongoConnection;
import daoadmin.EmpleadoDAO;
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
@Override
    public Document comprobarUsuarios(String usuario, String contrasena) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Empleado> listarEmpleados() {
        List<Empleado> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("Empleados");
            for (Document doc : col.find()) {
                String id = doc.get("_id") != null ? doc.get("_id").toString() : "";
                String usuario = doc.getString("nombreUsuario") != null ? doc.getString("nombreUsuario") : "";
                String nombre = doc.getString("nombre") != null ? doc.getString("nombre") : "";
                String apellidos = doc.getString("apellidos") != null ? doc.getString("apellidos") : "";
                String contrasena = doc.getString("contrasena") != null ? doc.getString("contrasena") : "";
                String correo = doc.getString("correo") != null ? doc.getString("correo") : "";
                String rol = doc.getString("rol") != null ? doc.getString("rol") : "Empleado";
                String iban = doc.getString("cuentaBancaria") != null ? doc.getString("cuentaBancaria") : "";

                Object idSucObj = doc.get("idSucursal");
                String idSucursal = idSucObj != null ? idSucObj.toString() : "";

                lista.add(new Empleado(id, usuario, nombre, apellidos, contrasena, correo, rol, iban, idSucursal));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
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
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminarEmpleado(String nombreUsuario) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            return db.getCollection("Empleados").deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean ascenderEmpleado(String nombreUsuario, String nuevoRol) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            Document emp = colEmp.find(Filters.eq("nombreUsuario", nombreUsuario)).first();
            if (emp != null) {
                colEmp.deleteOne(Filters.eq("nombreUsuario", nombreUsuario));
                emp.append("rol", nuevoRol);
                db.getCollection("Administradores").insertOne(emp);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
