package dao;

import exception.OurException;
import modelo.Administrador;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Usuario;

/**
 * Operaciones comunes con la base de datos: iniciar sesión y registrarse.
 */
public interface DAO {

    // Devuelve un Cliente, Empleado o Administrador, o null si los datos no son correctos
    Usuario login(String nombreUsuario, String contrasena) throws OurException;

    void insertarCliente(Cliente cliente) throws OurException;
}
