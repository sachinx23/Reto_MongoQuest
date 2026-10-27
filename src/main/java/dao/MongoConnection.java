package dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import exception.ErrorMessages;
import exception.OurException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import log.LogApp;
import org.bson.Document;

/**
 * Gestiona la única conexión (MongoClient) de toda la aplicación.
 * Los datos de conexión se leen de {@code src/main/resources/config.properties}.
 */
public class MongoConnection {

    private static MongoClient client;
    private static MongoDatabase database;

    /**
     * Abre la conexión leyendo la URI y el nombre de la base de datos de
     * config.properties. Se llama una sola vez al arrancar la aplicación.
     *
     * @throws OurException si no se puede leer la configuración o el servidor no responde
     */
    public static void connect() throws OurException {
        if (client != null) {
            return;
        }
        Properties config = new Properties();
        try (InputStream in = MongoConnection.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new OurException("No se encuentra el fichero config.properties");
            }
            config.load(in);
        } catch (IOException e) {
            LogApp.error("Error leyendo config.properties", e);
            throw new OurException("No se pudo leer config.properties");
        }
        connect(config.getProperty("mongo.uri"), config.getProperty("mongo.database"));
    }

    /**
     * Abre la conexión con una URI y base de datos concretas.
     *
     * @param uri cadena de conexión de MongoDB
     * @param dbName nombre de la base de datos
     * @throws OurException si el servidor no responde
     */
    public static void connect(String uri, String dbName) throws OurException {
        try {
            if (client == null) {
                client = MongoClients.create(uri);
                database = client.getDatabase(dbName);
                // ping: obliga a comprobar que el servidor responde de verdad
                database.runCommand(new Document("ping", 1));
                LogApp.info("Conectado a MongoDB, base de datos " + dbName);
            }
        } catch (MongoException | IllegalArgumentException e) {
            LogApp.error("Error conectando con MongoDB", e);
            close();
            throw new OurException(ErrorMessages.DATABASE);
        }
    }

    /**
     * Devuelve la base de datos ya conectada.
     *
     * @return base de datos
     * @throws OurException si todavía no se ha llamado a connect()
     */
    public static MongoDatabase getDatabase() throws OurException {
        if (database == null) {
            throw new OurException(ErrorMessages.DATABASE);
        }
        return database;
    }

    /**
     * Cierra la conexión. Se llama al cerrar la aplicación.
     */
    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
            LogApp.info("Conexión con MongoDB cerrada");
        }
    }
}
