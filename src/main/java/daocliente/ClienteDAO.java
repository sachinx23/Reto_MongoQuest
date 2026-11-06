package daocliente;

import exception.OurException;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Usuario;

/**
 * Clientes (y el login, común a todos): iniciar sesión, registrarse y modificar el perfil.
 */
public interface ClienteDAO {

    // Devuelve un Cliente, Empleado o Administrador, o null si los datos no son correctos
    Usuario login(String nombreUsuario, String contrasena) throws OurException;

    void insertarCliente(Cliente cliente) throws OurException;

    void actualizarCliente(Cliente cliente) throws OurException;
}
