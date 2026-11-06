package exception;

/**
 * Excepción propia de la aplicación. Su mensaje es el que se le enseña al usuario.
 */
public class OurException extends Exception {

    /**
     * Crea la excepción.
     *
     * @param mensaje mensaje para el usuario (de ErrorMessages)
     */
    public OurException(String mensaje) {
        super(mensaje);
    }
}
