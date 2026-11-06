package daocliente;

import exception.OurException;
import java.util.List;
import modelo.Tarifa;

/**
 * Tarifas que puede contratar el cliente.
 */
public interface TarifaDAO {

    List<Tarifa> obtenerTarifas() throws OurException;
}
