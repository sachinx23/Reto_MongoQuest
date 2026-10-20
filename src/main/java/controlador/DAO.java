package controlador;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class DAO {

    private static final String URI = "mongodb://localhost:27017";
    private static final String BBDD = "Tel-ONDA";

    public static Document comprobarUsuarios(String usuario, String contrasena) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);

            Document query = new Document("nombreUsuario", usuario)
                    .append("contrasena", contrasena);

            // Buscar en administradores
            MongoCollection<Document> coleccionAdmin = db.getCollection("Administradores");
            Document admin = coleccionAdmin.find(query).first();
            if (admin != null) {
                if (!admin.containsKey("rol")) {
                    admin.append("rol", "Administradores");
                }
                return admin;
            }
            

            // Buscar en empleados
            // Document: Estructura nativa en formato JSON de MongoDB para leer/modificar campos de forma directa.
            MongoCollection<Document> coleccionEmpleado = db.getCollection("Empleados");
            Document empleado = coleccionEmpleado.find(query).first();
            if (empleado != null) {
                if (!empleado.containsKey("rol")) {
                    empleado.append("rol", "Empleados");
                }
                return empleado;
            }

            //Buscar en clientes
            MongoCollection<Document> coleccionCliente = db.getCollection("Clientes");
            Document cliente = coleccionCliente.find(query).first();
            if (cliente != null) {
                if (!cliente.containsKey("rol")) {
                    cliente.append("rol", "Cliente");
                }
                return cliente;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; 
    }
    public static boolean insertarCliente(String usuario, String nombre, String contrasena, 
                                          String correo, String dni, String telefono, String direccion) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> coleccionCliente = db.getCollection("Clientes");

            // Comprobar si ya existe un usuario con el mismo 'nombreUsuario'
            Document existe = coleccionCliente.find(new Document("nombreUsuario", usuario)).first();
            if (existe != null) {
                return false; 
            }

            // Crear el documento del nuevo cliente
            Document nuevoCliente = new Document("nombreUsuario", usuario)
                    .append("nombre", nombre)
                    .append("contrasena", contrasena)
                    .append("correo", correo)
                    .append("dni", dni)
                    .append("telefono", telefono)
                    .append("direccion", direccion)
                    .append("rol", "Cliente");

            coleccionCliente.insertOne(nuevoCliente);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}