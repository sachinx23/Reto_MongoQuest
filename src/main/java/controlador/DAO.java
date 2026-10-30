package controlador;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import modelo.Sucursal;
import modelo.Ubicacion;
import modelo.Tarifa;
import modelo.Contrato;
import modelo.Empleado;
import modelo.Cliente;
import modelo.TicketSoporte;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class DAO {

    private static final String URI = "mongodb://localhost:27017";
    private static final String BBDD = "Tel-ONDA";

    // Método auxiliar seguro para crear filtros por ID (soporta ObjectId y String)
    private static Bson getFiltroId(String idStr) {
        if (idStr != null && ObjectId.isValid(idStr)) {
            return Filters.or(Filters.eq("_id", new ObjectId(idStr)), Filters.eq("_id", idStr));
        }
        return Filters.eq("_id", idStr);
    }

    // -------------------------------------------------------------------------
    // LOGIN Y USUARIOS
    // -------------------------------------------------------------------------
    public static Document comprobarUsuarios(String usuario, String contrasena) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);

            Document query = new Document("nombreUsuario", usuario)
                    .append("contrasena", contrasena);

            // Buscar en Administradores
            Document admin = db.getCollection("Administradores").find(query).first();
            if (admin != null) {
                admin.append("rol", "Admin");
                return admin;
            }

            // Buscar en Empleados
            Document empleado = db.getCollection("Empleados").find(query).first();
            if (empleado != null) {
                empleado.append("rol", "Empleado");
                return empleado;
            }

            // Buscar en Clientes
            Document cliente = db.getCollection("Clientes").find(query).first();
            if (cliente != null) {
                cliente.append("rol", "Cliente");
                return cliente;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean registarCliente(String usuario, String nombre, String apellidos, String contrasena,
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
                    .append("apellidos", apellidos)
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

    public static boolean eliminarDatosSensiblesCliente(String nombreUsuario) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            return db.getCollection("Clientes").deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Document> obtenerPlantilla() {
        List<Document> todos = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);

            for (Document doc : db.getCollection("Administradores").find()) {
                doc.append("rol", "Admin");
                todos.add(doc);
            }
            for (Document doc : db.getCollection("Empleados").find()) {
                doc.append("rol", "Empleado");
                todos.add(doc);
            }
            for (Document doc : db.getCollection("Clientes").find()) {
                doc.append("rol", "Cliente");
                todos.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todos;
    }

    public static List<Empleado> listarEmpleados() {
        List<Empleado> lista = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Empleados");
            for (Document doc : col.find()) {
                String id = doc.get("_id") != null ? doc.get("_id").toString() : "";
                String usuario = doc.getString("nombreUsuario") != null ? doc.getString("nombreUsuario") : "";
                String nombre = doc.getString("nombre") != null ? doc.getString("nombre") : "";
                String apellidos = doc.getString("apellidos") != null ? doc.getString("apellidos") : "";
                String contrasena = doc.getString("contrasena") != null ? doc.getString("contrasena") : "";
                String correo = doc.getString("correo") != null ? doc.getString("correo") : "";
                String rol = doc.getString("rol") != null ? doc.getString("rol") : "Empleado";
                String iban = doc.getString("cuentaBancaria") != null ? doc.getString("cuentaBancaria") : "";

                Object idSucObj = doc.get("idSucursal");
                String idSucursal = idSucObj != null ? idSucObj.toString() : "";

                // Se llaman exactamente a los 9 parámetros del ÚNICO constructor
                Empleado emp = new Empleado(id, usuario, nombre, apellidos, contrasena, correo, rol, iban, idSucursal);
                lista.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Clientes");
            for (Document doc : col.find()) {
                String usuario = doc.getString("nombreUsuario");
                String nombre = doc.getString("nombre");
                String apellidos = doc.getString("apellidos");
                String correo = doc.getString("correo");
                String dni = doc.getString("dni");
                Object telObj = doc.get("telefono");
                String telefono = telObj != null ? telObj.toString() : "";
                String direccion = doc.getString("direccion");

                Cliente cli = new Cliente(usuario, nombre, apellidos, correo, dni, telefono, direccion);
                lista.add(cli);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static boolean ascenderEmpleado(String nombreUsuario, String nuevoRol) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            Document emp = colEmp.find(Filters.eq("nombreUsuario", nombreUsuario)).first();
            if (emp != null) {
                colEmp.deleteOne(Filters.eq("nombreUsuario", nombreUsuario));
                emp.append("rol", nuevoRol);
                db.getCollection("Administradores").insertOne(emp);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean crearEmpleado(String usuario, String nombre, String apellidos, String contrasena, String correo, String cuentaBancaria) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> colEmp = db.getCollection("Empleados");

            if (colEmp.find(new Document("nombreUsuario", usuario)).first() != null) {
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

    public static boolean eliminarEmpleado(String nombreUsuario) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            return db.getCollection("Empleados").deleteOne(Filters.eq("nombreUsuario", nombreUsuario)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE SUCURSALES
    // -------------------------------------------------------------------------
    private static Document ubicacionADocumento(Ubicacion u) {
        if (u == null) {
            return new Document("direccion", "").append("localidad", "").append("codigoPostal", 0);
        }
        return new Document("direccion", u.getDireccion())
                .append("localidad", u.getLocalidad())
                .append("codigoPostal", u.getCodigoPostal());
    }

    public static int siguienteIdSucursal() {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");
            int maxId = 0;
            for (Document d : col.find()) {
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
            return client.getDatabase(BBDD).getCollection("Sucursales").deleteOne(Filters.eq("id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Sucursal> listarSucursales() {
        List<Sucursal> resultado = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Sucursales");
            for (Document d : col.find()) {
                Document u = d.get("ubicacion", Document.class);
                String dir = u != null && u.getString("direccion") != null ? u.getString("direccion") : "";
                String loc = u != null && u.getString("localidad") != null ? u.getString("localidad") : "";
                int cp = u != null && u.getInteger("codigoPostal") != null ? u.getInteger("codigoPostal") : 0;

                int id = d.getInteger("id", 0);

                // Conversión segura de teléfono (soporta String y Number)
                Object telObj = d.get("telefono");
                int telefono = 0;
                if (telObj instanceof Number) {
                    telefono = ((Number) telObj).intValue();
                } else if (telObj != null) {
                    try {
                        telefono = Integer.parseInt(telObj.toString().replaceAll("\\s+", ""));
                    } catch (NumberFormatException ignored) {
                    }
                }

                String horario = d.getString("horario") != null ? d.getString("horario") : "";

                resultado.add(new Sucursal(id, horario, telefono, new Ubicacion(dir, loc, cp)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE TARIFAS
    // -------------------------------------------------------------------------
    public static List<Tarifa> listarTarifas() {
        List<Tarifa> resultado = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
            for (Document d : col.find()) {
                String id = d.get("_id") != null ? d.get("_id").toString() : "";
                String nombre = d.getString("nombreTarifa");
                Object p = d.get("precio");
                double precio = p instanceof Number ? ((Number) p).doubleValue() : 0.0;
                String detalles = d.getString("detalles");
                String tipo = d.getString("tipoTarifa");
                Boolean activa = d.getBoolean("activa", false);

                resultado.add(new Tarifa(id, nombre, precio, detalles, tipo, activa));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public static boolean insertarTarifa(Tarifa t) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
            Document doc = new Document("nombreTarifa", t.getNombreTarifa())
                    .append("precio", t.getPrecio())
                    .append("detalles", t.getDetalles())
                    .append("tipoTarifa", t.getTipoTarifa())
                    .append("activa", t.getActiva());
            col.insertOne(doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean actualizarTarifa(Tarifa t) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
            Document actualizar = new Document("nombreTarifa", t.getNombreTarifa())
                    .append("precio", t.getPrecio())
                    .append("detalles", t.getDetalles())
                    .append("tipoTarifa", t.getTipoTarifa())
                    .append("activa", t.getActiva());

            return col.updateOne(getFiltroId(t.getId()), new Document("$set", actualizar)).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean eliminarTarifa(Tarifa t) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Tarifas");
            return col.deleteOne(getFiltroId(t.getId())).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE CONTRATOS
    // -------------------------------------------------------------------------
    public static List<Contrato> listarContratos() {
        List<Contrato> lista = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Contratos");
            for (Document doc : col.find()) {
                Contrato c = mapearContrato(doc);
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static boolean aprobarContrato(Contrato c, String idGestor) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            MongoCollection<Document> col = db.getCollection("Contratos");

            Object idEmp = ObjectId.isValid(idGestor) ? new ObjectId(idGestor) : idGestor;
            boolean ok = col.updateOne(getFiltroId(c.getId()), Updates.combine(
                    Updates.set("estado", "activo"),
                    Updates.set("idEmpleado", idEmp),
                    Updates.set("fechaComienzo", LocalDate.now().toString())
            )).getModifiedCount() > 0;

            if (ok) {
                ObjectId idCli = new ObjectId(c.getIdCliente());
                ObjectId idCon = new ObjectId(c.getId());

                Document ticket = new Document()
                        .append("tipo", "contrato")
                        .append("asunto", "Confirmación de Alta de Tarifa")
                        .append("descripcion", "Su solicitud para la línea " + c.getTelefono() + " ha sido aprobada.")
                        .append("fechaCreacion", LocalDate.now().toString())
                        .append("estado", "cerrado")
                        .append("idCliente", idCli)
                        .append("idEmpleado", idEmp)
                        .append("idContrato", idCon);
                db.getCollection("TicketsSoporte").insertOne(ticket);
            }
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean rechazarContratos(Contrato c) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Contratos");
            return col.updateOne(getFiltroId(c.getId()), Updates.set("estado", "rechazado")).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

   public static boolean rescindirContrato(Contrato c) {
    try (MongoClient client = MongoClients.create(URI)) {
        MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Contratos");

        Bson actualizacion = Updates.combine(
                Updates.set("estado", "rescindido"),
                Updates.set("fechaFinalizacion", LocalDate.now().toString())
        );

        return col.updateOne(getFiltroId(c.getId()), actualizacion).getModifiedCount() > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

    public static List<Contrato> filtrarContratos(String telefono, String estado) {
        List<Contrato> lista = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("Contratos");
            List<Bson> filtros = new ArrayList<>();

            if (telefono != null && !telefono.trim().isEmpty()) {
                filtros.add(Filters.regex("telefono", Pattern.compile(telefono.trim(), Pattern.CASE_INSENSITIVE)));
            }

            if (estado != null && !"Todos".equalsIgnoreCase(estado)) {
                filtros.add(Filters.eq("estado", estado));
            }

            Bson filtroFinal = filtros.isEmpty() ? new Document() : Filters.and(filtros);

            for (Document doc : col.find(filtroFinal)) {
                Contrato c = mapearContrato(doc);
                if (c != null) {
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static Contrato mapearContrato(Document doc) {
        if (doc == null) {
            return null;
        }

        Contrato c = new Contrato();

        // Mapeo básico de ID, teléfono y estado
        c.setId(doc.get("_id") != null ? doc.get("_id").toString() : "");
        c.setTelefono(doc.get("telefono") != null ? doc.get("telefono").toString() : "");
        c.setEstado(doc.get("estado") != null ? doc.get("estado").toString() : "");

        // Mapeo de claves foráneas convertidas a String
        c.setIdCliente(doc.get("idCliente") != null ? doc.get("idCliente").toString() : "");
        c.setIdTarifa(doc.get("idTarifa") != null ? doc.get("idTarifa").toString() : "");
        c.setIdEmpleado(doc.get("idEmpleado") != null ? doc.get("idEmpleado").toString() : "");

        // Lectura de fechas con .toString() para evitar fallos de tipo (String vs Date)
        c.setFechaComienzo(doc.get("fechaComienzo") != null ? doc.get("fechaComienzo").toString() : "");

        // Comprobamos fechaFinalizacion por si en Mongo está escrito con o sin tilde
        Object fFin = doc.get("fechaFinalizacion");
        if (fFin == null) {
            fFin = doc.get("fechaFinalización");
        }
        c.setFechaFin(fFin != null ? fFin.toString() : "");

        return c;
    }

    public static boolean crearTickets(String idCliente, String asunto, String descripcion) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase(BBDD);
            Object idCli = ObjectId.isValid(idCliente) ? new ObjectId(idCliente) : idCliente;

            Document doc = new Document()
                    .append("tipo", "incidencia")
                    .append("asunto", asunto)
                    .append("descripcion", descripcion)
                    .append("fechaCreacion", LocalDate.now().toString())
                    .append("estado", "pendiente")
                    .append("idCliente", idCli)
                    .append("idEmpleado", null)
                    .append("idContrato", null);

            db.getCollection("TicketsSoporte").insertOne(doc);

            db.getCollection("Clientes").updateOne(getFiltroId(idCliente),
                    Updates.push("ticketSoporte", doc.getObjectId("_id")));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<TicketSoporte> listarTicket(String estado) {
        List<TicketSoporte> lista = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("TicketsSoporte");

            Document filtro = new Document("tipo", "incidencia");

            if (estado != null && !estado.equals("Todos")) {
                filtro.append("estado", estado);
            }

            List<Document> docs = col.find(filtro).into(new ArrayList<>());
            for (int i = 0; i < docs.size(); i++) {
                lista.add(mapearTicket(docs.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static List<TicketSoporte> listarTicketsCliente(String idCliente) {
        List<TicketSoporte> lista = new ArrayList<>();
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("TicketsSoporte");

            ObjectId id = new ObjectId(idCliente);
            Document filtro = new Document("idCliente", id);

            // Guardar los resultados en una lista
            List<Document> docs = new ArrayList<>();
            col.find(filtro).into(docs);

            // Recorrer la lista 
            for (int i = 0; i < docs.size(); i++) {
                Document doc = docs.get(i);
                TicketSoporte t = mapearTicket(doc);
                lista.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static boolean asignarTicket(TicketSoporte t, String idEmpleado) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("TicketsSoporte");

            // Convertimos el id del empleado a ObjectId
            ObjectId idEmp = new ObjectId(idEmpleado);

            //Preparamos los cambios el empleado y el estado
            Document cambios = new Document("idEmpleado", idEmp)
                    .append("estado", "en proceso");

            //Actualizar el ticket 
            col.updateOne(getFiltroId(t.getId()), new Document("$set", cambios));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean cambiarEstadoTicket(TicketSoporte t, String estado) {
        try (MongoClient client = MongoClients.create(URI)) {
            MongoCollection<Document> col = client.getDatabase(BBDD).getCollection("TicketsSoporte");
            return col.updateOne(getFiltroId(t.getId()),
                    Updates.set("estado", estado)).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static TicketSoporte mapearTicket(Document d) {
        TicketSoporte t = new TicketSoporte();
        // El _id es un ObjectId, lo paso a texto
        if (d.get("_id") != null) {
            t.setId(d.get("_id").toString());
        }
        // Estos campos ya son texto, se leen directamente
        t.setTipo(d.getString("tipo"));
        t.setAsunto(d.getString("asunto"));
        t.setDescripcion(d.getString("descripcion"));
        t.setEstado(d.getString("estado"));
        // Los ids son ObjectId, los paso a texto solo si existen
        if (d.get("idCliente") != null) {
            t.setIdCliente(d.get("idCliente").toString());
        }
        if (d.get("idEmpleado") != null) {
            t.setIdEmpleado(d.get("idEmpleado").toString());
        }
        if (d.get("idContrato") != null) {
            t.setIdContrato(d.get("idContrato").toString());
        }
        // La fecha la paso a texto
        if (d.get("fechaCreacion") != null) {
            t.setFechaCreacion(d.get("fechaCreacion").toString());
        }
        return t;
    }
}
