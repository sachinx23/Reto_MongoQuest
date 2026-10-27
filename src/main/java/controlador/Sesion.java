package controlador;

import dao.DAO;
import dao.DAOImplementacion;
import modelo.Usuario;

/**
 * Guarda el DAO y el usuario que ha iniciado sesión, para usarlos desde cualquier pantalla.
 * En los tests se cambia el DAO por un MockModelDao con setDao().
 */
public class Sesion {

    private static DAO dao;
    private static Usuario usuario;

    public static DAO getDao() {
        if (dao == null) {
            dao = new DAOImplementacion();
        }
        return dao;
    }

    public static void setDao(DAO nuevoDao) {
        dao = nuevoDao;
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
