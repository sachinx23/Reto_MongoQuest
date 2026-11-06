package daocliente;

import exception.OurException;
import java.util.List;
import modelocliente.Contrato;

/**
 * Contratos del cliente: pedir tarifa, borrar la petición y pedir la baja.
 */
public interface ContratoDAO {
    /**
     * Contratos de un cliente.
     *
     * @param idCliente id del cliente
     * @return contratos del cliente
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    List<Contrato> obtenerContratosCliente(String idCliente) throws OurException;

    /**
     * Guarda una petición de tarifa (contrato "pendiente").
     *
     * @param contrato contrato con los datos
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void insertarContrato(Contrato contrato) throws OurException;

    /**
     * Borra la petición pendiente de una tarifa. Devuelve false si no había ninguna.
     *
     * @param idCliente id del cliente
     * @param idTarifa id de la tarifa
     * @return true si se borró, false si no había ninguna petición pendiente
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    boolean borrarPeticion(String idCliente, String idTarifa) throws OurException;

    /**
     * Pide la baja de un contrato (estado "baja pendiente").
     *
     * @param idContrato id del contrato
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void solicitarBaja(String idContrato) throws OurException;

    /**
     * El cliente firma un contrato "pendiente de firma": pasa a activo (empieza hoy y dura un año) y se le deja
     * un ticket cerrado con los detalles de la compra.
     *
     * @param contrato contrato que se firma
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    void firmarContrato(Contrato contrato) throws OurException;
}
