package daocliente.implementacion;

import com.mongodb.MongoException;
import daocliente.ValoracionDAO;
import exception.ErrorMessages;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import modelocliente.Valoracion;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Operaciones de la colección Valoraciones.
 */
public class ValoracionDAOImplementacion extends DAOBase implements ValoracionDAO {
    @Override
    public List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException {
        try {
            List<Valoracion> valoraciones = buscarValoraciones(new Document("idCliente", new ObjectId(idCliente)));
            return valoraciones;
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.GET_VALORACIONES);
        }
    }

    @Override
    public void insertarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document d = new Document();
            d.put("puntuacion", valoracion.getPuntuacion());
            d.put("texto", valoracion.getTexto());
            d.put("idCliente", new ObjectId(valoracion.getIdCliente()));
            d.put("idTarifa", aObjectId(valoracion.getIdTarifa()));
            coleccion(COL_VALORACIONES).insertOne(d);

            valoracion.setId(idTexto(d, "_id"));
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void actualizarValoracion(Valoracion valoracion) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("puntuacion", valoracion.getPuntuacion());
            cambios.put("texto", valoracion.getTexto());
            if (!modificar(COL_VALORACIONES, valoracion.getId(), cambios)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    @Override
    public void borrarValoracion(String idValoracion) throws OurException {
        try {
            if (!borrar(COL_VALORACIONES, idValoracion)) {
                throw new OurException(ErrorMessages.SAVE_VALORACION);
            }
        } catch (MongoException e) {
            throw new OurException(ErrorMessages.SAVE_VALORACION);
        }
    }

    /**
     * Valoraciones que cumplen el filtro, con el nombre de su tarifa.
     *
     * @param filtro filtro de la búsqueda
     * @return valoraciones encontradas
     * @throws OurException si falla la operación en la base de datos (su mensaje es el que ve el usuario)
     */
    private List<Valoracion> buscarValoraciones(Document filtro) throws OurException {
        List<Valoracion> valoraciones = new ArrayList<>();
        for (Document d : coleccion(COL_VALORACIONES).find(filtro)) {
            Valoracion v = new Valoracion(idTexto(d, "_id"), (int) numero(d, "puntuacion"), texto(d, "texto"),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"));
            Document cliente = buscarPorId(COL_CLIENTES, d.get("idCliente"));
            if (cliente != null) {
                v.setNombreCliente(texto(cliente, "nombre"));
            }
            valoraciones.add(v);
        }
        return valoraciones;
    }
}
