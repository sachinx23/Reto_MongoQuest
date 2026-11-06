package daocliente;

import exception.OurException;
import modelocliente.Cliente;

/**
 * Clientes: buscar el cliente de la sesión y modificar su perfil.
 */
public interface ClienteDAO {
    /**
     * Busca el cliente que ha iniciado sesión (el login es el de Eneko, que guarda su id).
     *
     * @param id id del cliente
     * @return el cliente, o null si no existe
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    Cliente obtenerCliente(String id) throws OurException;

    /**
     * Guarda los cambios del perfil del cliente.
     *
     * @param cliente cliente con los datos
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void actualizarCliente(Cliente cliente) throws OurException;
}
