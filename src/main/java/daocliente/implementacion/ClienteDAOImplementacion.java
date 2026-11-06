package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.ClienteDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Cliente;
import modelo.Usuario;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección Clientes y el login.
 */
public class ClienteDAOImplementacion extends DAOBase implements ClienteDAO {

    private static final Logger LOGGER = Logger.getLogger(ClienteDAOImplementacion.class.getName());

    @Override
    public Usuario login(String nombreUsuario, String contrasena) throws OurException {
        try {
            Document filtro = new Document();
            filtro.put("nombreUsuario", nombreUsuario);
            filtro.put("contrasena", contrasena);

            Document d = coleccion(COL_ADMINISTRADORES).find(filtro).first();
            if (d != null) {
                LOGGER.info("Login de administrador: " + nombreUsuario);
                return documentoAAdministrador(d);
            }

            d = coleccion(COL_EMPLEADOS).find(filtro).first();
            if (d != null) {
                LOGGER.info("Login de empleado: " + nombreUsuario);
                return documentoAEmpleado(d);
            }

            d = coleccion(COL_CLIENTES).find(filtro).first();
            if (d != null) {
                LOGGER.info("Login de cliente: " + nombreUsuario);
                return documentoACliente(d);
            }

            LOGGER.info("Login fallido: " + nombreUsuario);
            return null;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en el login de " + nombreUsuario, e);
            throw new OurException(ErrorMessages.LOGIN);
        }
    }

    @Override
    public void insertarCliente(Cliente cliente) throws OurException {
        try {
            if (coleccion(COL_CLIENTES).find(new Document("dni", cliente.getDni())).first() != null) {
                LOGGER.info("Registro rechazado, DNI repetido: " + cliente.getDni());
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            if (nombreUsuarioOcupado(cliente.getNombreUsuario(), null)) {
                LOGGER.info("Registro rechazado, usuario repetido: " + cliente.getNombreUsuario());
                throw new OurException(ErrorMessages.USER_EXISTS);
            }

            Document d = new Document();
            d.put("nombreUsuario", cliente.getNombreUsuario());
            d.put("nombre", cliente.getNombre());
            d.put("apellidos", "");
            d.put("rol", "Cliente");
            d.put("contrasena", cliente.getContrasena());
            d.put("dni", cliente.getDni());
            d.put("correo", cliente.getCorreo());
            d.put("telefono", cliente.getTelefono());
            d.put("direccion", cliente.getDireccion());
            d.put("cuentaBancaria", cliente.getCuentaBancaria());
            d.put("dadoDeBaja", false);
            d.put("ticketSoporte", new ArrayList<ObjectId>());
            coleccion(COL_CLIENTES).insertOne(d);

            // Al insertar, MongoDB le pone un _id nuevo al documento
            cliente.setId(idTexto(d, "_id"));
            LOGGER.info("Cliente registrado: " + cliente.getNombreUsuario() + " (id " + cliente.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error registrando el cliente " + cliente.getNombreUsuario(), e);
            throw new OurException(ErrorMessages.REGISTER_USER);
        }
    }

    @Override
    public void actualizarCliente(Cliente cliente) throws OurException {
        try {
            if (nombreUsuarioOcupado(cliente.getNombreUsuario(), cliente.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            // El DNI tampoco puede ser el de otro cliente
            Document otro = coleccion(COL_CLIENTES).find(new Document("dni", cliente.getDni())).first();
            if (otro != null && !idTexto(otro, "_id").equals(cliente.getId())) {
                throw new OurException(ErrorMessages.DNI_EXISTS);
            }
            Document cambios = new Document();
            cambios.put("nombre", cliente.getNombre());
            cambios.put("nombreUsuario", cliente.getNombreUsuario());
            cambios.put("dni", cliente.getDni());
            cambios.put("correo", cliente.getCorreo());
            cambios.put("direccion", cliente.getDireccion());
            cambios.put("telefono", cliente.getTelefono());
            cambios.put("cuentaBancaria", cliente.getCuentaBancaria());

            if (!modificar(COL_CLIENTES, cliente.getId(), cambios)) {
                throw new OurException(ErrorMessages.UPDATE_USER);
            }
            LOGGER.info("Cliente modificado: " + cliente.getNombreUsuario() + " (id " + cliente.getId() + ")");
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando el cliente " + cliente.getId(), e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }
}
