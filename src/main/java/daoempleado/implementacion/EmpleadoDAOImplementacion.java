package daoempleado.implementacion;

import com.mongodb.MongoException;
import daocliente.implementacion.DAOBase;
import daoempleado.EmpleadoDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Administrador;
import modelo.Empleado;
import org.bson.Document;

/**
 * Operaciones de la colección Empleados (perfil del empleado).
 */
public class EmpleadoDAOImplementacion extends DAOBase implements EmpleadoDAO {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoDAOImplementacion.class.getName());

    @Override
    public void actualizarEmpleado(Empleado empleado) throws OurException {
        String nombreColeccion = COL_EMPLEADOS;
        if (empleado instanceof Administrador) {
            nombreColeccion = COL_ADMINISTRADORES;
        }
        try {
            if (nombreUsuarioOcupado(empleado.getNombreUsuario(), empleado.getId())) {
                throw new OurException(ErrorMessages.USER_EXISTS);
            }
            Document cambios = new Document();
            cambios.put("nombre", empleado.getNombre());
            cambios.put("nombreUsuario", empleado.getNombreUsuario());
            cambios.put("contrasena", empleado.getContrasena());
            cambios.put("correo", empleado.getCorreo());
            cambios.put("cuentaBancaria", empleado.getCuentaBancaria());
            if (!modificar(nombreColeccion, empleado.getId(), cambios)) {
                throw new OurException(ErrorMessages.UPDATE_USER);
            }
            LOGGER.info("Datos modificados en " + nombreColeccion + ": " + empleado.getNombreUsuario());
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error modificando " + empleado.getNombreUsuario() + " en " + nombreColeccion, e);
            throw new OurException(ErrorMessages.UPDATE_USER);
        }
    }
}
