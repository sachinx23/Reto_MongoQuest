package exception;

/**
 * Mensajes de error que se muestran al usuario. Están todos juntos para que
 * los textos sean iguales en toda la aplicación.
 */
public class ErrorMessages
{

    /** No se ha podido conectar con la base de datos. */
    public static final String DATABASE = "No se pudo conectar con la base de datos. Comprueba que MongoDB está arrancado.";

    /** Fallo al iniciar sesión. */
    public static final String LOGIN = "Error al iniciar sesión. Inténtalo de nuevo.";

    /** Fallo al registrar un cliente. */
    public static final String REGISTER_USER = "No se pudo registrar el usuario. Comprueba los datos.";

    /** El nombre de usuario ya existe. */
    public static final String USER_EXISTS = "Ese nombre de usuario ya está en uso.";

    /** Fallo al modificar los datos del cliente. */
    public static final String UPDATE_USER = "No se pudieron guardar los cambios del usuario.";

    /** Fallo al leer las tarifas. */
    public static final String GET_TARIFAS = "No se pudieron cargar las tarifas.";

    /** Fallo al leer los contratos. */
    public static final String GET_CONTRATOS = "No se pudieron cargar los contratos.";

    /** Fallo al crear un contrato. */
    public static final String INSERT_CONTRATO = "No se pudo crear el contrato.";

    /** Fallo al rescindir un contrato. */
    public static final String RESCINDIR_CONTRATO = "No se pudo rescindir el contrato.";

    /** Fallo al leer los tickets. */
    public static final String GET_TICKETS = "No se pudieron cargar los tickets.";

    /** Fallo al crear un ticket. */
    public static final String INSERT_TICKET = "No se pudo guardar el ticket.";

    /** Fallo al leer el fichero de log. */
    public static final String READ_LOG = "No se pudo leer el fichero de log.";
}
