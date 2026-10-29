package controlador;

/**
 * Apartados del desplegable del panel de empleado.
 */
public enum OpcionEmpleado {
    PERFIL("Mi Perfil"),
    TICKETS("Tickets pendientes"),
    BAJAS("Solicitudes de baja"),
    VALIDAR("Validar tarifas");

    private final String texto;

    OpcionEmpleado(String texto) {
        this.texto = texto;
    }

    // Lo que se ve escrito en el desplegable
    @Override
    public String toString() {
        return texto;
    }
}
