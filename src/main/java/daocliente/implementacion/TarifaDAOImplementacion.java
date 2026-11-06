package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.TarifaDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Tarifa;
import org.bson.Document;

/**
 * Operaciones de la colección Tarifas (parte de cliente).
 */
public class TarifaDAOImplementacion extends DAOBase implements TarifaDAO {

    private static final Logger LOGGER = Logger.getLogger(TarifaDAOImplementacion.class.getName());

    @Override
    public List<Tarifa> obtenerTarifas() throws OurException {
        List<Tarifa> tarifas = new ArrayList<>();
        try {
            for (Document d : coleccion(COL_TARIFAS).find()) {
                // Solo se enseñan las tarifas activas (si no tiene el campo "activa", se considera activa)
                boolean activa = booleano(d, "activa", true);
                if (activa) {
                    tarifas.add(documentoATarifa(d));
                }
            }
            LOGGER.info("Consultadas " + tarifas.size() + " tarifas");
            return tarifas;
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error consultando tarifas", e);
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
    }
}
