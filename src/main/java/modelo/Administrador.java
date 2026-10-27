package modelo;

/**
 * Representa un documento de la colección <b>Administradores</b>.
 * Tiene los mismos datos que un empleado pero no pertenece a ninguna sucursal.
 *
 * @author eneko
 */
public class Administrador extends Empleado {

    public Administrador(String id, String nombre, String nombreUsuario, String contrasena, String correo, String cuentaBancaria) {
        super(id, nombre, nombreUsuario, "Admin", contrasena, correo, cuentaBancaria, null);
    }

    @Override
    public String toString() {
        return "Administrador{" + "id=" + getId() + ", nombre=" + getNombre() + ", nombreUsuario=" + getNombreUsuario() + '}';
    }
}
