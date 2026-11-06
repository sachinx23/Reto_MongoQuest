package daocliente;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import exception.ErrorMessages;
import exception.OurException;

/**
 * Conexión única con MongoDB (base de datos Tel-ONDA) que comparten todos los DAO.
 */
public class MongoConnection {
    private static MongoClient client;
    private static MongoDatabase database;

    // La misma base de datos que usa MongoDBConexion de Eneko
    private static final String URI = "mongodb://localhost:27017/?serverSelectionTimeoutMS=5000";
    private static final String BASE_DATOS = "Tel-ONDA";

    /**
     * Abre la conexión con MongoDB (la primera vez que la parte de cliente la necesita).
     *
     * @param uri dirección del servidor MongoDB
     * @param nombreBaseDatos nombre de la base de datos
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    public static void connect(String uri, String nombreBaseDatos) throws OurException {
        try {
            if (client == null) {
                client = MongoClients.create(uri);
                database = client.getDatabase(nombreBaseDatos);

                // Fuerza la conexión real: si MongoDB está apagado, aquí salta el error
                database.listCollectionNames().first();
            }
        } catch (RuntimeException e) {
            close();
            throw new OurException(ErrorMessages.DATABASE);
        }
    }

    /**
     * Base de datos de la parte de cliente. La primera vez abre la conexión.
     *
     * @return la base de datos
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    public static MongoDatabase getDatabase() throws OurException {
        if (database == null) {
            connect(URI, BASE_DATOS);
        }
        return database;
    }

    /**
     * Cierra la conexión. 
     */
    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}
