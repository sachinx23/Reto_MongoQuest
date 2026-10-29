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
import modelo.Tarifa;
import org.bson.types.ObjectId;

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

    // REGISTRAR CLIENTE
    public static boolean registarCliente(String usuario, String nombre, String apellidos, String contrasena,
            String correo, String dni, String telefono, String direccion) {

        System.out.println("DEBUG: Entrando a registarCliente con usuario = " + usuario);

        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> col = db.getCollection("Clientes");

            // Comprobar si existe
            Document existe = col.find(new Document("nombreUsuario", usuario)).first();
            if (existe != null) {
                System.out.println("¡El usuario ya existe en la base de datos!");
                return false;
            }

            // Crear documento
            Document nuevo = new Document("nombreUsuario", usuario)
                    .append("nombre", nombre)
                    .append("apellidos", apellidos)
                    .append("contrasena", contrasena)
                    .append("correo", correo)
                    .append("dni", dni)
                    .append("telefono", telefono)
                    .append("direccion", direccion)
                    .append("rol", "Cliente");

            // Insertar
            col.insertOne(nuevo);
            System.err.println("¡Cliente insertado con éxito en MongoDB!");
            return true;

        } catch (Exception e) {
            System.out.println("DEBUG: ¡Ha ocurrido un error en el catch!");
            e.printStackTrace();
            return false;
        }
    }

// Eliminar datos sensibles del cliente desde admin
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

            // Cargar Administradores
            List<Document> admins = db.getCollection("Administradores").find().into(new ArrayList<>());
            for (Document doc : admins) {
                doc.append("rol", "Admin");
                todos.add(doc);
            }

            // Cargar Empleados
            List<Document> emps = db.getCollection("Empleados").find().into(new ArrayList<>());
            for (Document doc : emps) {
                doc.append("rol", "Empleado");
                todos.add(doc);
            }

            // Cargar Clientes
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

    //  Ascender empleado a admin
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
    // crear empleado como admin

    public static boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria) {
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
    // dar de baja a un empleado

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

    // gestion de sucursales
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

    // insertar un sucursal
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

    // actualizar un sucursal
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

    // eliminar un sucursal
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

 // gestion de listar sucursal
    public static List<Sucursal> listarSucursales() {
        List<Sucursal> resultado = new ArrayList<>();

        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");
            List<Document> lista = col.find().into(new ArrayList<>());

            for (Document d : lista) {
                Document u = d.get("ubicacion", Document.class);

                String dir = "";
                String loc = "";
                int cp = 0;
                
                if (u != null) {
                    dir = u.getString("direccion") != null ? u.getString("direccion") : "";
                    loc = u.getString("localidad") != null ? u.getString("localidad") : "";
                    // getInteger(campo, valorPorDefectoSiEsNull)
                    cp = u.getInteger("codigoPostal", 0); 
                }

                // Si viene null en MongoDB, le asigna 0 por defecto y no peta
                int id = d.getInteger("id", 0);
                int telefono = d.getInteger("telefono", 0);
                String horario = d.getString("horario") != null ? d.getString("horario") : "";

                resultado.add(new Sucursal(id, horario, telefono, new Ubicacion(dir, loc, cp)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }
    
    // Gestion de tarifas desde admin
    public static List<Tarifa> listarTarifas(){
        List<Tarifa> resultado = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)){
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
           for(Document d: col.find()){
               String id = d.getObjectId("_id").toString();//Lo que hace es devolver el ObjectId(24 carecteres)
               String nombre = d.getString("nombreTarifa");
               Object p = d.get("precio");
               // para que si es cualquier tipo de numero lo convierta a decimal
               double precio = 0.0;
               if(p instanceof Number){
                   precio = ((Number)p).doubleValue();
           }
              String detalles = d.getString("detalles");
              String tipo = d.getString("tipoTarifa");
              
              Boolean activa = d.getBoolean("activa");
               if (activa == null) {
                   activa = false;
               }
               
               resultado.add(new Tarifa(id,nombre,precio,detalles,tipo,activa ));
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }
    
    
    // insertar una tarifa
    public static boolean insertarTarifa(Tarifa t){
        try(MongoClient client = MongoClients.create(URI)){
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
            
            Document doc = new Document("nombreTarifa", t.getNombreTarifa())
                    .append("precio", t.getPrecio())
                    .append("detalles", t.getDetalles())
                    .append("tipoTarifa", t.getTipoTarifa())
                    .append("activa", t.getActiva());
            
            col.insertOne(doc);     
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    
    // actualizar una tarifa
    public static boolean actualizarTarifa(Tarifa t){
        try (MongoClient client = MongoClients.create(URI)){
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
            
            ObjectId objectId = new ObjectId(t.getId());
            
            Document actualizar = new Document("nomberTarifa", t.getNombreTarifa())
                    .append("precio", t.getPrecio())
                    .append("detalles", t.getDetalles())
                    .append("tipoTarifa", t.getTipoTarifa())
                    .append("activa", t.getActiva());
            
            col.updateOne(Filters.eq("_id",objectId), new Document("$set", actualizar));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // eliminar una tarifa
    public static boolean eliminarTarifa(Tarifa t){
        try (MongoClient client = MongoClients.create(URI)){
             MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
             ObjectId objectId = new ObjectId(t.getId());
             return col.deleteOne(Filters.eq("_id", objectId)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //listar los contratos
   // public static boolean List<Contrato>listarContratos(){
    //    List<Contrato> resultado = new ArrayList<>();
    //}
}
