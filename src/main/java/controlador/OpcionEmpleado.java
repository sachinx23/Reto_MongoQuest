package controlador;

/**
 * Apartados del desplegable del panel de empleado y la pantalla (FXML) que abre cada uno.
 */
public enum OpcionEmpleado {
    PERFIL("Mi Perfil", "EmpleadoPerfil"),
    TICKETS("Tickets pendientes", "EmpleadoTickets"),
    BAJAS("Solicitudes de baja", "EmpleadoBajas"),
    VALIDAR("Validar tarifas", "EmpleadoValidar");

    private final String texto;
    private final String vista;

    OpcionEmpleado(String texto, String vista) {
        this.texto = texto;
        this.vista = vista;
    }

    // Nombre del FXML de la carpeta vista que se abre al elegir el apartado
    public String getVista() {
        return vista;
    }

    // Lo que se ve escrito en el desplegable
    @Override
    public String toString() {
        return texto;
    }
}
