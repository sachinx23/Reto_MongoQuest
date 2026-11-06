package modelocliente;

/**
 * Cliente de Tel-ONDA.
 */
public class Cliente extends Usuario {

    private String dni;
    private String telefono;
    private String direccion;
    private boolean dadoDeBaja;

    /**
     * Crea un cliente con todos sus datos.
     *
     * @param id id del documento
     * @param nombre nombre
     * @param nombreUsuario nombre de usuario
     * @param contrasena contraseña
     * @param correo correo electrónico
     * @param cuentaBancaria cuenta bancaria (IBAN)
     * @param dni DNI o NIE
     * @param telefono número de teléfono
     * @param direccion dirección
     */
    public Cliente(String id, String nombre, String nombreUsuario, String contrasena, String correo,
            String cuentaBancaria, String dni, String telefono, String direccion) {
        super(id, nombre, nombreUsuario, contrasena, correo, cuentaBancaria);
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.dadoDeBaja = false;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isDadoDeBaja() {
        return dadoDeBaja;
    }

    public void setDadoDeBaja(boolean dadoDeBaja) {
        this.dadoDeBaja = dadoDeBaja;
    }

    /**
     * Crea un cliente vacío (se rellena con los setters).
     */
    public Cliente() {
    }

    /**
     * Crea un cliente con los datos que enseña la parte de administrador (sin contraseña ni cuenta).
     *
     * @param nombreUsuario nombre de usuario
     * @param nombre nombre
     * @param apellidos apellidos
     * @param correo correo electrónico
     * @param dni DNI o NIE
     * @param telefono número de teléfono
     * @param direccion dirección
     */
    public Cliente(String nombreUsuario, String nombre, String apellidos, String correo, String dni, String telefono, String direccion) {
        setNombreUsuario(nombreUsuario);
        setNombre(nombre);
        setApellidos(apellidos);
        setCorreo(correo);
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
    }
}
