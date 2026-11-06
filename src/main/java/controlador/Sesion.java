package controlador;

import modelo.Usuario;

/**
 * Guarda el usuario que ha iniciado sesión, para usarlo desde cualquier pantalla.
 */
public class Sesion {

    private static Usuario usuario;

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
