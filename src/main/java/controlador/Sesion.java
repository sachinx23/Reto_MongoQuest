package controlador;

import daocliente.ClienteDAO;
import daocliente.implementacion.ClienteDAOImplementacion;
import daoempleado.EmpleadoDAO;
import daoempleado.implementacion.EmpleadoDAOImplementacion;
import modelo.Usuario;

/**
 * Guarda los DAO (cliente y empleado) y el usuario que ha iniciado sesión, para usarlos desde cualquier pantalla.
 */
public class Sesion {

    private static final ClienteDAO clienteDao = new ClienteDAOImplementacion();
    private static final EmpleadoDAO empleadoDao = new EmpleadoDAOImplementacion();
    private static Usuario usuario;

    // Cliente, login y registro
    public static ClienteDAO getClienteDao() {
        return clienteDao;
    }

    public static EmpleadoDAO getEmpleadoDao() {
        return empleadoDao;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario nuevoUsuario) {
        usuario = nuevoUsuario;
    }

    public static void cerrar() {
        usuario = null;
    }
}
