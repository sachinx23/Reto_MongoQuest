package controlador;

import dao.DAO;
import dao.implementacion.DAOImplementacion;
import daocliente.ClienteDAO;
import daocliente.implementacion.ClienteDAOImplementacion;
import daoempleado.EmpleadoDAO;
import daoempleado.implementacion.EmpleadoDAOImplementacion;
import modelo.Usuario;

/**
 * Guarda los DAO (común, cliente y empleado) y el usuario que ha iniciado sesión, para usarlos desde cualquier pantalla.
 */
public class Sesion {

    private static final DAO dao = new DAOImplementacion();
    private static final ClienteDAO clienteDao = new ClienteDAOImplementacion();
    private static final EmpleadoDAO empleadoDao = new EmpleadoDAOImplementacion();
    private static Usuario usuario;

    // Login y registro
    public static DAO getDao() {
        return dao;
    }

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
