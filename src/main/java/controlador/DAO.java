package controlador;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.List;
import modelo.Sucursal;
import modelo.Ubicacion;
import org.bson.Document;

public class DAO {

    private static final String URI = "mongodb://localhost:27017";
    private static final String BBDD = "Tel-ONDA";

    //Login
    public static Document comprobarUsuarios(String usuario, String contrasena) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);

            Document query = new Document("nombreUsuario", usuario)
                    .append("contrasena", contrasena);

            // Buscar en Administradores
            MongoCollection<Document> colAdmin = db.getCollection("Administradores");
            Document admin = colAdmin.find(query).first();
            if (admin != null) {
                admin.append("rol", "Admin");
                return admin;
            }

            // Buscar en Empleados
            MongoCollection<Document> colEmp = db.getCollection("Empleados");
            Document empleado = colEmp.find(query).first();
            if (empleado != null) {
                empleado.append("rol", "Empleado");
                return empleado;
            }

            // Buscar en Clientes
            MongoCollection<Document> colCli = db.getCollection("Clientes");
            Document cliente = colCli.find(query).first();
            if (cliente != null) {
                cliente.append("rol", "Cliente");
                return cliente;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //REGISTRAR CLIENTE
    public static boolean registarCliente(String usuario, String nombre, String contrasena,
            String correo, String dni, String telefono, String direccion) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> col = db.getCollection("Clientes");

            Document existe = col.find(new Document("nombreUsuario", usuario)).first();
            if (existe != null) {
                return false;
            }

            Document nuevo = new Document("nombreUsuario", usuario)
                    .append("nombre", nombre)
                    .append("contrasena", contrasena)
                    .append("correo", correo)
                    .append("dni", dni)
                    .append("telefono", telefono)
                    .append("direccion", direccion)
                    .append("rol", "Cliente");

            col.insertOne(nuevo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

// ELIMINAR DATOS SENSIBLES DEL CLIENTE (Administrador)
    public static boolean eliminarDatosSensiblesCliente(String nombreUsuario) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> colCli = db.getCollection("Clientes");

            // Borra por completo el documento del cliente en la BBDD
            return colCli.deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // OBTENER TODOS LOS USUARIOS (PARA LA TABLA DE GESTIONAR PLANTILLA)
    public static List<Document> obtenerPlantilla() {
        List<Document> todos = new ArrayList<>();

        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);

            // 1. Cargar Administradores
            List<Document> admins = db.getCollection("Administradores").find().into(new ArrayList<>());
            for (Document doc : admins) {
                doc.append("rol", "Admin");
                todos.add(doc);
            }

            // 2. Cargar Empleados
            List<Document> emps = db.getCollection("Empleados").find().into(new ArrayList<>());
            for (Document doc : emps) {
                doc.append("rol", "Empleado");
                todos.add(doc);
            }

            // 3. Cargar Clientes
            List<Document> clis = db.getCollection("Clientes").find().into(new ArrayList<>());
            for (Document doc : clis) {
                doc.append("rol", "Cliente");
                todos.add(doc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return todos;
    }

    //  ASCENDER EMPLEADO A ADMIN
    public static boolean ascenderEmpleado(String nombreUsuario, String nuevoRol) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            Document emp = colEmp.find(Filters.eq("nombreUsuario", nombreUsuario)).first();

            if (emp != null) {
                // Borrar de Empleados
                colEmp.deleteOne(Filters.eq("nombreUsuario", nombreUsuario));

                // Guardar en Administradores
                emp.append("rol", nuevoRol);
                db.getCollection("Administradores").insertOne(emp);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // CREAR EMPLEADO (Administrador)

    public static boolean crearEmpleado(String usuario, String nombre,String apellidos, String contrasena, String correo, String cuentaBancaria) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            // Comprobamos si ya existe el nombreUsuario
            Document existe = colEmp.find(new Document("nombreUsuario", usuario)).first();
            if (existe != null) {
                return false;
            }

            Document nuevoEmp = new Document("nombreUsuario", usuario)
                    .append("nombre", nombre)
                    .append("apellidos", apellidos)
                    .append("contrasena", contrasena)
                    .append("correo", correo)
                    .append("cuentaBancaria", cuentaBancaria)
                    .append("rol", "Empleado");

            colEmp.insertOne(nuevoEmp);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // DAR DE BAJA EMPLEADO

    public static boolean eliminarEmpleado(String nombreUsuario) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            colEmp.deleteOne(Filters.eq("nombreUsuario", nombreUsuario));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // GESTIÓN DE SUCURSALES (MÉTODOS SIMPLIFICADOS)
    private static Document ubicacionADocumento(Ubicacion u) {
        if (u == null) {
            return new Document("direccion", "").append("localidad", "").append("codigoPostal", "");
        }
        return new Document("direccion", u.getDireccion())
                .append("localidad", u.getLocalidad())
                .append("codigoPostal", u.getCodigoPostal());
    }

    public static int siguienteIdSucursal() {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");

            // Traemos las sucursales a una lista
            List<Document> lista = col.find().into(new ArrayList<>());

            int maxId = 0;
            for (Document d : lista) {
                Integer id = d.getInteger("id");
                if (id != null && id > maxId) {
                    maxId = id;
                }
            }
            return maxId + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static boolean insertarSucursal(Sucursal s) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");

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

            col.updateOne(Filters.eq("id", s.getId()), new Document("$set", cambios));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean eliminarSucursal(int id) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");
            col.deleteOne(Filters.eq("id", id));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Sucursal> listarSucursales() {
        List<Sucursal> resultado = new ArrayList<>();

        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");

            // Pasamos todos los documentos a una lista de Java normal
            List<Document> documentos = col.find().into(new ArrayList<>());

            // Recorremos la lista paso a paso
            for (Document d : documentos) {
                Integer idObj = d.getInteger("id");
                int id = (idObj != null) ? idObj : 0;

                Document u = d.get("ubicacion", Document.class);
                String dir = (u != null && u.getString("direccion") != null) ? u.getString("direccion") : "";
                String loc = (u != null && u.getString("localidad") != null) ? u.getString("localidad") : "";
                String cp = (u != null && u.get("codigoPostal") != null) ? String.valueOf(u.get("codigoPostal")) : "";

                Ubicacion ubicacion = new Ubicacion(dir, loc, cp);
                Sucursal s = new Sucursal(id, d.getString("horario"), d.getString("telefono"), ubicacion);

                resultado.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
