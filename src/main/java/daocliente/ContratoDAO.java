package daocliente;

import exception.OurException;
import java.util.List;
import modelo.Contrato;

/**
 * Contratos del cliente: pedir tarifa, borrar la petición y pedir la baja.
 */
public interface ContratoDAO {

    List<Contrato> obtenerContratosCliente(String idCliente) throws OurException;

    void insertarContrato(Contrato contrato) throws OurException;

    // Borra la petición pendiente de una tarifa. Devuelve false si no había ninguna
    boolean borrarPeticion(String idCliente, String idTarifa) throws OurException;

    void solicitarBaja(String idContrato) throws OurException;
}
