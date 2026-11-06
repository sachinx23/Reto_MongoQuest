package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.TarifaDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import modelocliente.Tarifa;
import org.bson.Document;

/**
 * Operaciones de la colección Tarifas (parte de cliente).
 */
public class TarifaDAOImplementacion extends DAOBase implements TarifaDAO {
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
            return tarifas;
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.GET_TARIFAS);
        }
    }
}
