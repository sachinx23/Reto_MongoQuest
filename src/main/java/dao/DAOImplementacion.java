package dao;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

public class DAOImplementacion implements DAO {

    @Override
    public Document comprobarUsuarios(String usuario, String contrasena) {
        System.out.println("--- INICIANDO COMPROBACIÓN DE USUARIO ---");
        System.out.println("Usuario: '" + usuario + "' | Contraseña: '" + contrasena + "'");

        try {
            MongoDatabase db = MongoConnection.getDatabase();
            Document query = new Document("nombreUsuario", usuario)
                    .append("contrasena", contrasena);

            MongoCollection<Document> coleccionAdmin = db.getCollection("Administradores");
            Document admin = coleccionAdmin.find(query).first();
            if (admin != null) {
                if (!admin.containsKey("rol")) admin.append("rol", "Administradores");
                return admin;
            }

            MongoCollection<Document> coleccionEmpleado = db.getCollection("Empleados");
            Document empleado = coleccionEmpleado.find(query).first();
            if (empleado != null) {
                if (!empleado.containsKey("rol")) empleado.append("rol", "Empleados");
                return empleado;
            }

            MongoCollection<Document> coleccionCliente = db.getCollection("Clientes");
            Document cliente = coleccionCliente.find(query).first();
            if (cliente != null) {
                if (!cliente.containsKey("rol")) cliente.append("rol", "Cliente");
                return cliente;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; 
    }
    
    @Override
    public boolean actualizarCliente(Document cliente) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("Clientes");
            
            collection.updateOne(
                com.mongodb.client.model.Filters.eq("_id", cliente.getObjectId("_id")),
                new Document("$set", new Document("nombre", cliente.getString("nombre"))
                    .append("nombreUsuario", cliente.getString("nombreUsuario"))
                    .append("dni", cliente.getString("dni"))
                    .append("correo", cliente.getString("correo"))
                    .append("direccion", cliente.getString("direccion"))
                    .append("telefono", cliente.get("telefono"))
                    .append("cuentaBancaria", cliente.getString("cuentaBancaria")))
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertarCliente(String usuario, String nombre, String contrasena, 
                                 String correo, String dni, String telefono, String direccion) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> coleccionCliente = db.getCollection("Clientes");

            if (coleccionCliente.find(new Document("nombreUsuario", usuario)).first() != null) {
                return false; 
            }

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

    @Override
    public boolean insertarContrato(String telefono, ObjectId idTarifa, ObjectId idCliente) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> colContratos = db.getCollection("Contratos");

            Document nuevoContrato = new Document("telefono", telefono)
                    .append("estado", "activo")
                    .append("fechaComienzo", java.time.LocalDate.now().toString())
                    .append("fechaFinalizacion", java.time.LocalDate.now().plusYears(1).toString())
                    .append("idTarifa", idTarifa)
                    .append("idCliente", idCliente);

            colContratos.insertOne(nuevoContrato);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertarTicket(String asunto, String descripcion, ObjectId idCliente) {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> colTickets = db.getCollection("TicketsSoportes");

            Document nuevoTicket = new Document("asunto", asunto)
                    .append("descripcion", descripcion)
                    .append("fechaCreacion", java.time.LocalDate.now().toString())
                    .append("estado", "pendiente")
                    .append("idCliente", idCliente);

            colTickets.insertOne(nuevoTicket);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}