package controlador;

/**
 * Apartados del desplegable del panel de cliente.
 */
public enum OpcionCliente {
    PERFIL("Mi Perfil"),
    TARIFAS("Mis Tarifas"),
    CONTRATOS("Mis Contratos"),
    SOPORTE("Soporte"),
    VALORACIONES("Mis Valoraciones");

    private final String texto;

    OpcionCliente(String texto) {
        this.texto = texto;
    }

    // Lo que se ve escrito en el desplegable
    @Override
    public String toString() {
        return texto;
    }
}
