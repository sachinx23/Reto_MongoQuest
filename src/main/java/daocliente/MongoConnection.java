package daocliente;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import exception.ErrorMessages;
import exception.OurException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

public class MongoConnection {

    private static final Logger LOGGER = Logger.getLogger(MongoConnection.class.getName());

    private static MongoClient client;
    private static MongoDatabase database;

    // Abre la conexión con MongoDB. Se llama una vez al arrancar la aplicación
    public static void connect(String uri, String nombreBaseDatos) throws OurException {
        try {
            if (client == null) {
                client = MongoClients.create(uri);
                database = client.getDatabase(nombreBaseDatos);

                // Fuerza la conexión real: si MongoDB está apagado, aquí salta el error
                database.listCollectionNames().first();
                LOGGER.info("Conectado a MongoDB, base de datos " + nombreBaseDatos);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar con MongoDB", e);
            close();
            throw new OurException(ErrorMessages.DATABASE);
        }
    }

    public static MongoDatabase getDatabase() throws OurException {
        if (database == null) {
            throw new OurException(ErrorMessages.DATABASE);
        }
        return database;
    }

    // Una colección de la base de datos (la usan los DAO de administrador).
    // No lanza OurException: la aplicación no arranca si no ha podido conectar con MongoDB
    public static MongoCollection<Document> getColeccion(String nombre) {
        return database.getCollection(nombre);
    }

    // Cierra la conexión. Se llama al cerrar la aplicación
    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}
