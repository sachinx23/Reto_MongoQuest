package controlador;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class DAO {

    private final String URI = "mongodb://localhost:27017";
    private final String BBDD = "Tel-ONDA";

    public Document autenticar(String usuario, String contrasena) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);

            // Creamos la consulta con el nombre y contrasena
            Document query = new Document("nombreUsuario", usuario).append("contrasena", contrasena);

            // Buscar en administradores
            //Uso de Document:
            //En vez de crear una clase Usuario con sus getters y setters, usas Document porque es 
            //la estructura nativa de MongoDB que te permite leer y consultar datos en formato JSON de forma directa y rápida.
            MongoCollection<Document> coleccionAdmin = db.getCollection("administradores");
            Document admin = coleccionAdmin.find(query).first();
            if (admin != null) {
                return admin;
            }

            // Buscar en empleados
            MongoCollection<Document> coleccionEmpleado = db.getCollection("empleados");
            Document empleado = coleccionEmpleado.find(query).first();
            if (empleado != null) {
                return empleado; // Devuelve el documento de la colección empleados
            }

            // Buscar en clientes (por si un cliente intenta entrar)
            MongoCollection<Document> coleccionCliente = db.getCollection("clientes");
            Document cliente = coleccionCliente.find(query).first();
            if (cliente != null) {
            // Devuelve el documento de la colección clientes
                return cliente; 
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Devuelve null si no existe en ninguna colección
        return null; 
    }
}