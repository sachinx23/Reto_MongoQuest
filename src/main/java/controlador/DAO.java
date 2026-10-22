package controlador;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.List;
import modelo.Sucursal;
import modelo.Ubicacion;
import org.bson.Document;

public class DAO {

    private static final String URI = "mongodb://localhost:27017";
    private static final String BBDD = "Tel-ONDA";

    public static Document comprobarUsuarios(String usuario, String contrasena) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);

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

    public static boolean registarCliente(String usuario, String nombre, String contrasena,
                                          String correo, String dni, String telefono, String direccion) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> coleccionCliente = db.getCollection("Clientes");

            Document existe = coleccionCliente.find(new Document("nombreUsuario", usuario)).first();
            if (existe != null) return false;

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

    private static Document ubicacionADocumento(Ubicacion u) {
        if (u == null) return new Document("direccion", "").append("localidad", "").append("codigoPostal", "");
        return new Document("direccion", u.getDireccion())
                .append("localidad", u.getLocalidad())
                .append("codigoPostal", u.getCodigoPostal());
    }

    public static int siguienteIdSucursal() {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> coleccion = client.getDatabase(BBDD).getCollection("Sucursales");
            Document ultimo = coleccion.find().sort(Sorts.descending("id")).first();
            return ultimo == null ? 1 : (ultimo.getInteger("id") != null ? ultimo.getInteger("id") + 1 : 1);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static boolean insertarSucursal(Sucursal s) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");

            if (col.find(Filters.eq("id", s.getId())).first() != null) return false;

            Document doc = new Document("id", s.getId())
                    .append("horario", s.getHorario())
                    .append("telefono", s.getTelefono())
                    .append("ubicacion", ubicacionADocumento(s.getUbicacion()));

            col.insertOne(doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean actualizarSucursal(Sucursal s) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");

            Document cambios = new Document("horario", s.getHorario())
                    .append("telefono", s.getTelefono())
                    .append("ubicacion", ubicacionADocumento(s.getUbicacion()));

            return col.updateOne(Filters.eq("id", s.getId()), new Document("$set", cambios)).getMatchedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean eliminarSucursal(int id) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");
            return col.deleteOne(Filters.eq("id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

public static List<Sucursal> listarSucursales() {
    List<Sucursal> lista = new ArrayList<>();
    try (MongoClient client = MongoClients.create(URI)) {
        MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");

        for (Document d : col.find().sort(Sorts.ascending("id"))) {
            // EVITA EL NULLPOINTEREXCEPTION: Comprueba si el id existe
            Integer idObj = d.getInteger("id");
            int id = (idObj != null) ? idObj : 0;

            Document u = d.get("ubicacion", Document.class);
            String cp = (u != null && u.get("codigoPostal") != null) ? String.valueOf(u.get("codigoPostal")) : "";

            Ubicacion ubicacion = new Ubicacion(
                    u != null && u.getString("direccion") != null ? u.getString("direccion") : "",
                    u != null && u.getString("localidad") != null ? u.getString("localidad") : "",
                    cp);

            lista.add(new Sucursal(id, d.getString("horario"), d.getString("telefono"), ubicacion));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return lista;
}
}