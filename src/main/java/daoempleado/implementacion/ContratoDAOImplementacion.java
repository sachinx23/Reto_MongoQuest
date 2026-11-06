package daoempleado.implementacion;

import com.mongodb.MongoException;
import daocliente.implementacion.DAOBase;
import daoempleado.ContratoDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Contrato;
import modelo.Contrato.EstadoContrato;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección Contratos (parte de empleado).
 */
public class ContratoDAOImplementacion extends DAOBase implements ContratoDAO {

    private static final Logger LOGGER = Logger.getLogger(ContratoDAOImplementacion.class.getName());

    @Override
    public List<Contrato> obtenerContratosPorEstado(EstadoContrato estado) throws OurException {
        try {
            List<Contrato> contratos = buscarContratos(new Document("estado", estado.toString()));
            LOGGER.info("Consultados " + contratos.size() + " contratos en estado " + estado);
            return contratos;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando contratos en estado " + estado, e);
            throw new OurException(ErrorMessages.GET_CONTRATOS);
        }
    }

    @Override
    public void validarContrato(String idContrato, String idEmpleado) throws OurException {
        try {
            Document contrato = buscarPorId(COL_CONTRATOS, new ObjectId(idContrato));
            if (contrato == null) {
                throw new OurException(ErrorMessages.VALIDAR_CONTRATO);
            }
            // 1. El contrato pasa a activo
            cambiarEstadoContrato(idContrato, EstadoContrato.ACTIVO, idEmpleado, ErrorMessages.VALIDAR_CONTRATO);
            // 2. Si el cliente estaba dado de baja, al tener otra vez un contrato activo deja de estarlo
            try {
                modificar(COL_CLIENTES, idTexto(contrato, "idCliente"), new Document("dadoDeBaja", false));
            } catch (MongoException e) {
                // Si falla el paso 2, se deshace el 1: el contrato vuelve a pendiente
                cambiarEstadoContrato(idContrato, EstadoContrato.PENDIENTE, null, ErrorMessages.VALIDAR_CONTRATO);
                throw e;
            }
            LOGGER.info("Contrato " + idContrato + " validado por el empleado " + idEmpleado);
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error validando el contrato " + idContrato, e);
            throw new OurException(ErrorMessages.VALIDAR_CONTRATO);
        }
    }
}
