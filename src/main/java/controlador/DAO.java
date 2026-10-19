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
            MongoCollection<Document> coleccionAdmin = db.getCollection("administradores");
            Document admin = coleccionAdmin.find(query).first();
            if (admin != null) {
                if (!admin.containsKey("rol")) {
                    admin.append("rol", "Administrador");
                }
                return admin;
            }

            // Buscar en empleados
            // Document: Estructura nativa en formato JSON de MongoDB para leer/modificar campos de forma directa.
            MongoCollection<Document> coleccionEmpleado = db.getCollection("empleados");
            Document empleado = coleccionEmpleado.find(query).first();
            if (empleado != null) {
                if (!empleado.containsKey("rol")) {
                    empleado.append("rol", "Empleado");
                }
                return empleado;
            }

            //Buscar en clientes
            MongoCollection<Document> coleccionCliente = db.getCollection("clientes");
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
}