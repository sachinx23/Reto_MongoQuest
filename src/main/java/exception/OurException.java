package exception;

public class OurException extends Exception {
    // Excepción propia con el mensaje que se enseña al usuario
    public OurException(String mensaje) {
        super(mensaje);
    }
}
