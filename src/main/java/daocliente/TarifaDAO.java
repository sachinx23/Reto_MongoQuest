package daocliente;

import exception.OurException;
import java.util.List;
import modelocliente.Tarifa;

/**
 * Tarifas que puede contratar el cliente.
 */
public interface TarifaDAO {
    /**
     * Tarifas activas que se pueden contratar.
     *
     * @return tarifas activas
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    List<Tarifa> obtenerTarifas() throws OurException;
}
