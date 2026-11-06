/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin.implementacion;

import daocliente.MongoConnection;
import daoadmin.AdministradorDAO;
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
public class AdministradorDAOImpl implements AdministradorDAO{
@Override
    public Document comprobarUsuarios(String usuario, String contrasena) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
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
            e.printStackTrace();
        }
        return null;
    }

   @Override
    public boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria, String direccion, String idSucursal) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
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
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean eliminarEmpleado(String nombreUsuario) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> colEmp = db.getCollection("Empleados");
            long borrados = colEmp.deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount();
            if (borrados > 0) {
                return true;
            } else {
                return false;
            }
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

    @Override
    public List<Document> obtenerPlantilla() {
        List<Document> todos = new ArrayList<>();
        try {
            MongoDatabase db = MongoConnection.getDatabase();

            // Usamos .into() para volcar los documentos en una lista clásica sin bucles avanzados
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
            e.printStackTrace();
        }
        return todos;
    }
    
    @Override
public boolean agregarEmpleadoASucursal(int idSucursal, String nombreUsuarioEmpleado) {
    try {
        MongoDatabase db = MongoConnection.getDatabase();
        MongoCollection<Document> colSucursales = db.getCollection("Sucursales");
        
        colSucursales.updateOne(
            Filters.eq("id", idSucursal),
            new Document("$push", new Document("empleados", nombreUsuarioEmpleado))
        );
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}