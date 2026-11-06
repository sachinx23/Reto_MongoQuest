/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin.implementacion;

import daocliente.MongoConnection;
import daoadmin.ClienteDAO;
import modelo.Cliente;
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
public class ClienteDaoImplementacion implements ClienteDAO {

    @Override
    public Document comprobarUsuarios(String usuario, String contrasena) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            Document query = new Document("nombreUsuario", usuario).append("contrasena", contrasena);
            
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
    public boolean registrarCliente(String usuario, String nombre, String apellidos, String contrasena, String correo, String dni, String telefono, String direccion) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
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
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminarDatosSensiblesCliente(String nombreUsuario) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            return db.getCollection("Clientes").deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        try {
            MongoDatabase db = MongoConnection.getDatabase();
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
            e.printStackTrace();
        }
        return lista;
    }
}
