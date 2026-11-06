package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.ClienteDAO;
import exception.ErrorMessages;
import exception.OurException;
import modelocliente.Cliente;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección Clientes (el login y el registro son los de Eneko).
 */
public class ClienteDAOImplementacion extends DAOBase implements ClienteDAO {
    @Override
    public Cliente obtenerCliente(String id) throws OurException {
        try {
            Document d = buscarPorId(COL_CLIENTES, new ObjectId(id));
            if (d == null) {
                return null;
            }
            return documentoACliente(d);
        } catch (MongoException | IllegalArgumentException e) {
            throw new OurException(ErrorMessages.LOGIN);
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
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }
}
