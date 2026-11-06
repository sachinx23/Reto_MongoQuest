package daoempleado;

import exception.OurException;
import java.util.List;
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;

/**
 * Peticiones de tarifa que valida el empleado.
 */
public interface ContratoDAO {

    List<Contrato> obtenerContratosPorEstado(EstadoContrato estado) throws OurException;

    void validarContrato(String idContrato, String idEmpleado) throws OurException;
}
