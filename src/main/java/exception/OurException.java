package exception;

public class OurException extends Exception
{

    private static final long serialVersionUID = 1L;

    public OurException(String mensaje)
    {
        super(mensaje);
    }
}
