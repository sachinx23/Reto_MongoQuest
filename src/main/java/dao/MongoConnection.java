package dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import exception.OurException;

public class MongoConnection {

    private static MongoClient client;
    private static MongoDatabase database;

    public static void connect(String uri, String dbName) throws OurException {
        try {
            if (client == null) {
                client = MongoClients.create(uri);
                database = client.getDatabase(dbName);
                database.listCollectionNames().first();
            }
        } catch (RuntimeException e) {
            throw new OurException("Error initializing MongoDB connection: " + e.getMessage());
        }
    }

    public static MongoDatabase getDatabase() throws OurException {
        if (database == null) {
            throw new OurException("MongoDB database not initialized. Call connect() first.");
        }
        return database;
    }

    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}