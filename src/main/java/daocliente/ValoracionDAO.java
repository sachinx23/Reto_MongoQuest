package daocliente;

import exception.OurException;
import java.util.List;
import modelo.Valoracion;

/**
 * Valoraciones del cliente.
 */
public interface ValoracionDAO {

    List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException;

    void insertarValoracion(Valoracion valoracion) throws OurException;

    void actualizarValoracion(Valoracion valoracion) throws OurException;

    void borrarValoracion(String idValoracion) throws OurException;
}
