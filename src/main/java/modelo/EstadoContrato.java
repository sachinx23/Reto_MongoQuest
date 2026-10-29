package modelo;

/**
 * Estados por los que pasa un contrato.
 */
public enum EstadoContrato {
    PENDIENTE("pendiente"),
    ACTIVO("activo"),
    BAJA_PENDIENTE("baja pendiente"),
    CANCELADO("cancelado");

    // Texto que se guarda en MongoDB y que se ve en la tabla
    private final String texto;

    EstadoContrato(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }

    // Pasa el texto guardado en MongoDB ("baja pendiente") a su valor del enum (BAJA_PENDIENTE)
    public static EstadoContrato desdeTexto(String texto) {
        for (EstadoContrato estado : values()) {
            if (estado.texto.equals(texto)) {
                return estado;
            }
        }
        return null;
    }
}
