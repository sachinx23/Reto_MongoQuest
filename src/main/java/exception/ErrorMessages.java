package exception;

/**
 * Mensajes de error que se muestran al usuario.
 */
public class ErrorMessages {
    public static final String DATABASE = "No se pudo conectar con la base de datos. Comprueba que MongoDB está arrancado.";
    public static final String LOGIN = "Error al iniciar sesión. Inténtalo de nuevo.";
    public static final String USER_EXISTS = "Ese nombre de usuario ya está en uso.";
    public static final String DNI_EXISTS = "El dni de usuario ya está registrado.";
    public static final String UPDATE_USER = "No se pudieron guardar los cambios del usuario.";
    public static final String GET_TARIFAS = "No se pudieron cargar las tarifas.";
    public static final String GET_CONTRATOS = "No se pudieron cargar los contratos.";
    public static final String INSERT_CONTRATO = "No se pudo crear el contrato.";
    public static final String BAJA_CONTRATO = "No se pudo tramitar la baja del contrato.";
    public static final String FIRMAR_CONTRATO = "No se pudo firmar el contrato.";
    public static final String GET_TICKETS = "No se pudieron cargar los tickets.";
    public static final String INSERT_TICKET = "No se pudo guardar el ticket.";
    public static final String GET_VALORACIONES = "No se pudieron cargar las valoraciones.";
    public static final String SAVE_VALORACION = "No se pudo guardar la valoración.";
    // Parte de administrador
}
