package controlador;

import modelocliente.Usuario;

/**
 * Guarda el usuario que ha iniciado sesión, para usarlo desde cualquier pantalla.
 */
public class Sesion {

    private static Usuario usuario;

    /**
     * Usuario que ha iniciado sesión.
     *
     * @return el usuario, o null si no hay sesión
     */
    public static Usuario getUsuario() {
        return usuario;
    }

    /**
     * Guarda el usuario que ha iniciado sesión.
     *
     * @param nuevoUsuario usuario que ha iniciado sesión
     */
    public static void setUsuario(Usuario nuevoUsuario) {
        usuario = nuevoUsuario;
    }

    /**
     * Cierra la sesión (ya no hay usuario).
     */
    public static void cerrar() {
        usuario = null;
    }
}
