package DAO;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDBConexion {
    private static final String URI = "mongodb://localhost:27017";
    private static final String BBDD = "Tel-ONDA";
    private static MongoClient mongoClient = null;

    // Método para obtener la base de datos de forma centralizada
    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(URI);
        }
        return mongoClient.getDatabase(BBDD);
    }
    
    public static MongoCollection<Document> getColeccion(String nombreColeccion) {
        return getDatabase().getCollection(nombreColeccion);
    }
}