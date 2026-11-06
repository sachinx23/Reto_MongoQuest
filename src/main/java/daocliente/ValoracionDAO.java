package daocliente;

import exception.OurException;
import java.util.List;
import modelocliente.Valoracion;

/**
 * Valoraciones del cliente.
 */
public interface ValoracionDAO {
    /**
     * Valoraciones de un cliente.
     *
     * @param idCliente id del cliente
     * @return valoraciones del cliente
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException;

    /**
     * Guarda una valoración nueva.
     *
     * @param valoracion valoración
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void insertarValoracion(Valoracion valoracion) throws OurException;

    /**
     * Guarda los cambios de una valoración.
     *
     * @param valoracion valoración
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void actualizarValoracion(Valoracion valoracion) throws OurException;

    /**
     * Borra una valoración.
     *
     * @param idValoracion id de la valoración
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void borrarValoracion(String idValoracion) throws OurException;
}
