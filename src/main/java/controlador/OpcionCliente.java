package controlador;

/**
 * Apartados del desplegable del panel de cliente y la pantalla (FXML) que abre cada uno.
 */
public enum OpcionCliente {
    PERFIL("Mi Perfil", "ClientePerfil"),
    TARIFAS("Mis Tarifas", "ClienteTarifas"),
    CONTRATOS("Mis Contratos", "ClienteContratos"),
    SOPORTE("Soporte", "ClienteSoporte"),
    VALORACIONES("Mis Valoraciones", "ClienteValoraciones");

    private final String texto;
    private final String vista;

    OpcionCliente(String texto, String vista) {
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
