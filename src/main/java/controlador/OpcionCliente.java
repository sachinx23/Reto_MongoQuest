package controlador;

/**
 * Apartados del desplegable del panel de cliente y la pantalla (FXML) que abre cada uno.
 */
public enum OpcionCliente {
    PERFIL("Mi Perfil", "ClientePerfil"),
    TARIFAS("Mis Tarifas", "ClienteTarifas"),
    CONTRATOS("Mis Contratos", "ClienteContratos"),
    SOPORTE("Soporte", "ClienteSoporte"),
    VALORACIONES("Mis Valoraciones", "ClienteValoraciones"),
    // No abre ninguna pantalla: cierra la sesión (como en el desplegable del administrador)
    CERRAR_SESION("Cerrar Sesión", null);

    private final String texto;
    private final String vista;

    /**
     * Crea un apartado.
     *
     * @param texto texto del desplegable
     * @param vista nombre del FXML (sin extensión) de la carpeta vista
     */
    OpcionCliente(String texto, String vista) {
        this.texto = texto;
        this.vista = vista;
    }

    /**
     * Pantalla que se abre al elegir el apartado.
     *
     * @return nombre del FXML de la carpeta vista
     */
    public String getVista() {
        return vista;
    }

    // Lo que se ve escrito en el desplegable
    @Override
    public String toString() {
        return texto;
    }
}
