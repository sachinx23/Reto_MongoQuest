package controlador;

/**
 * Apartados del desplegable del panel de administrador y la pantalla (FXML) que abre cada uno.
 */
public enum OpcionAdmin {
    PERFIL("Mi Perfil", "PerfilAdmin"),
    PLANTILLA("Gestionar plantilla", "GestionarPlantilla"),
    SUCURSALES("Gestionar sucursales", "Sucursal"),
    TARIFAS("Gestionar tarifas", "GestionarTarifas"),
    CONTRATOS("Gestionar contratos", "GestionarContrato"),
    TICKETS("Tickets de soporte", "GestionarTickets"),
    VALORACIONES("Ver valoraciones", "GestionarValoraciones"),
    LOG("Ver log", "VerLog");

    private final String texto;
    private final String vista;

    OpcionAdmin(String texto, String vista) {
        this.texto = texto;
        this.vista = vista;
    }

    // Nombre del FXML de la carpeta vista
    public String getVista() {
        return vista;
    }

    // Lo que se ve escrito en el desplegable
    @Override
    public String toString() {
        return texto;
    }
}
