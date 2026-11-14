package DAO.implementacion;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import DAO.MongoDBConexion;
import exception.OurException;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Tarifa;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Clase base abstracta para las implementaciones de acceso a datos (DAO) sobre MongoDB.
 * Proporciona constantes con los nombres de las colecciones, operaciones CRUD genéricas,
 * utilidades de validación y métodos auxiliares para el mapeo seguro entre documentos BSON
 * y los objetos del modelo de dominio.
 *
 * @author Eneko.Plaza
 */
public abstract class DAOBase {

    /**
     * Registrador de eventos (Logger) para trazar fallos y operaciones internas de la clase base.
     */
    private static final Logger LOGGER = Logger.getLogger(DAOBase.class.getName());

    /** Nombre de la colección de clientes en MongoDB. */
    protected static final String COL_CLIENTES = "Clientes";

    /** Nombre de la colección de tarifas en MongoDB. */
    protected static final String COL_TARIFAS = "Tarifas";

    /** Nombre de la colección de contratos en MongoDB. */
    protected static final String COL_CONTRATOS = "Contratos";

    /** Nombre de la colección de valoraciones en MongoDB. */
    protected static final String COL_VALORACIONES = "Valoraciones";

    /**
     * Obtiene una referencia a la colección de MongoDB especificada por su nombre.
     *
     * @param nombre Nombre de la colección a recuperar.
     * @return Instancia de {@link MongoCollection} tipada con {@link Document}.
     * @throws OurException Si ocurre un error al establecer la conexión o recuperar la colección.
     */
    protected MongoCollection<Document> coleccion(String nombre) throws OurException {
        return MongoDBConexion.getColeccion(nombre);
    }

    /**
     * Comprueba si un nombre de usuario ya está registrado en alguna de las colecciones de usuarios
     * ({@code Administradores}, {@code Empleados} o {@code Clientes}), excluyendo el propio registro
     * que se está editando.
     *
     * @param nombreUsuario Nombre de usuario cuya disponibilidad se desea verificar.
     * @param idPropio      Identificador del registro actual a excluir de la comprobación.
     * @return {@code true} si existe otro usuario distinto con el mismo nombre de usuario;
     *         {@code false} si el nombre está disponible.
     * @throws OurException Si se produce un error al consultar las colecciones.
     */
    protected boolean nombreUsuarioOcupado(String nombreUsuario, String idPropio) throws OurException {
        String[] colecciones = {"Administradores", "Empleados", COL_CLIENTES};
        for (String nombreColeccion : colecciones) {
            Document d = coleccion(nombreColeccion).find(new Document("nombreUsuario", nombreUsuario)).first();
            if (d != null && !idTexto(d, "_id").equals(idPropio)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Busca todos los documentos en la colección {@code Contratos} que cumplan con el filtro
     * proporcionado y los convierte en una lista de objetos {@link Contrato}.
     *
     * @param filtro Documento BSON con los criterios de búsqueda.
     * @return Una {@link List} con las instancias de {@link Contrato} coincidentes.
     * @throws OurException Si ocurre un error al acceder a la colección en MongoDB.
     */
    protected List<Contrato> buscarContratos(Document filtro) throws OurException {
        List<Contrato> contratos = new ArrayList<>();
        for (Document d : coleccion(COL_CONTRATOS).find(filtro)) {
            Contrato c = new Contrato(idTexto(d, "_id"), texto(d, "telefono"), texto(d, "estado"),
                    idTexto(d, "idCliente"), idTexto(d, "idTarifa"), idTexto(d, "idEmpleado"),
                    texto(d, "fechaComienzo"), texto(d, "fechaFinalizacion"));
            contratos.add(c);
        }
        return contratos;
    }

    /**
     * Actualiza el estado de un contrato y, opcionalmente, asigna el identificador del empleado
     * responsable de la gestión.
     *
     * @param idContrato   Identificador hexadecimal ({@link ObjectId}) del contrato a modificar.
     * @param estado       Nuevo estado que se asignará al contrato.
     * @param idEmpleado   Identificador del empleado gestor; si es {@code null}, no se modifica este campo.
     * @param mensajeError Mensaje de error personalizado para lanzar en caso de fallo.
     * @throws OurException Si el contrato no existe o si ocurre un error de MongoDB durante la actualización.
     */
    protected void cambiarEstadoContrato(String idContrato, String estado, String idEmpleado, String mensajeError) throws OurException {
        try {
            Document cambios = new Document();
            cambios.put("estado", estado);
            if (idEmpleado != null) {
                cambios.put("idEmpleado", new ObjectId(idEmpleado));
            }
            if (!modificar(COL_CONTRATOS, idContrato, cambios)) {
                throw new OurException(mensajeError);
            }
        } catch (MongoException e) {
            LOGGER.log(Level.SEVERE, "Error en cambiarEstadoContrato", e);
            throw new OurException(mensajeError);
        }
    }

    /**
     * Convierte un {@link Document} de MongoDB en una instancia del modelo {@link Cliente}.
     *
     * @param d Documento BSON con los datos del cliente.
     * @return Objeto {@link Cliente} poblado con los valores del documento.
     */
    protected Cliente documentoACliente(Document d) {
        return new Cliente(idTexto(d, "_id"), texto(d, "cuentaBancaria"), texto(d, "nombreUsuario"), texto(d, "nombre"),
                texto(d, "apellidos"), texto(d, "contrasena"), texto(d, "correo"), texto(d, "dni"), texto(d, "telefono"),
                texto(d, "direccion"));
    }

    /**
     * Convierte un {@link Document} de MongoDB en una instancia del modelo {@link Tarifa}.
     *
     * @param d Documento BSON con los datos de la tarifa.
     * @return Objeto {@link Tarifa} poblado con los valores del documento.
     */
    protected Tarifa documentoATarifa(Document d) {
        return new Tarifa(idTexto(d, "_id"), texto(d, "nombreTarifa"), numero(d, "precio"), texto(d, "detalles"),
                texto(d, "tipoTarifa"), booleano(d, "activa", true));
    }

    /**
     * Busca un único documento dentro de una colección filtrando por su clave primaria {@code _id}.
     *
     * @param nombreColeccion Nombre de la colección donde realizar la búsqueda.
     * @param id              Valor del identificador {@code _id} (habitualmente un {@link ObjectId} o {@link String}).
     * @return El primer {@link Document} coincidente, o {@code null} si no se encuentra ninguno.
     * @throws OurException Si ocurre un error al consultar la base de datos.
     */
    protected Document buscarPorId(String nombreColeccion, Object id) throws OurException {
        return coleccion(nombreColeccion).find(new Document("_id", id)).first();
    }

    /**
     * Actualiza los campos indicados de un documento identificado por su {@link ObjectId} mediante
     * el operador {@code $set} de MongoDB.
     *
     * @param nombreColeccion Nombre de la colección donde se encuentra el documento.
     * @param id              Identificador hexadecimal del documento a actualizar.
     * @param cambios         Documento BSON con los pares clave-valor que se desean establecer.
     * @return {@code true} si se encontró algún documento coincidente con el ID; {@code false} en caso contrario.
     * @throws OurException Si ocurre un error durante la operación de actualización.
     */
    protected boolean modificar(String nombreColeccion, String id, Document cambios) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        long encontrados = coleccion(nombreColeccion).updateOne(filtro, new Document("$set", cambios)).getMatchedCount();
        return encontrados > 0;
    }

    /**
     * Elimina un documento de una colección a partir de su identificador hexadecimal ({@link ObjectId}).
     *
     * @param nombreColeccion Nombre de la colección de la cual se eliminará el registro.
     * @param id              Identificador hexadecimal del documento a borrar.
     * @return {@code true} si se eliminó el documento; {@code false} si no existía.
     * @throws OurException Si ocurre un error durante el borrado en la base de datos.
     */
    protected boolean borrar(String nombreColeccion, String id) throws OurException {
        Document filtro = new Document("_id", new ObjectId(id));
        long borrados = coleccion(nombreColeccion).deleteOne(filtro).getDeletedCount();
        return borrados > 0;
    }

    /**
     * Convierte una cadena hexadecimal en un objeto {@link ObjectId} de MongoDB de forma segura ante nulos.
     *
     * @param id Cadena con el identificador hexadecimal.
     * @return Instancia de {@link ObjectId} correspondiente, o {@code null} si {@code id} es {@code null}.
     */
    protected ObjectId aObjectId(String id) {
        if (id == null) {
            return null;
        }
        return new ObjectId(id);
    }

    /**
     * Extrae de forma segura el valor de un campo de un {@link Document} como cadena de texto,
     * devolviendo una cadena vacía si el campo no existe o es nulo.
     *
     * @param d     Documento BSON de origen.
     * @param campo Nombre de la clave a extraer.
     * @return Representación en {@link String} del valor del campo, o {@code ""} si es {@code null}.
     */
    protected String texto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

    /**
     * Extrae el valor de un campo identificador de un {@link Document} como cadena de texto,
     * preservando {@code null} si el campo no está presente o es nulo.
     *
     * @param d     Documento BSON de origen.
     * @param campo Nombre de la clave del identificador (por ejemplo, {@code "_id"}).
     * @return Representación en {@link String} del identificador, o {@code null} si no existe.
     */
    protected String idTexto(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return null;
        }
        return valor.toString();
    }

    /**
     * Extrae un valor booleano de un {@link Document}, devolviendo un valor por defecto si el campo
     * no existe o no es de tipo {@link Boolean}.
     *
     * @param d          Documento BSON de origen.
     * @param campo      Nombre de la clave booleana a leer.
     * @param porDefecto Valor de respaldo que se retornará si el campo es nulo o inválido.
     * @return El valor {@code boolean} almacenado en el documento o {@code porDefecto}.
     */
    protected boolean booleano(Document d, String campo, boolean porDefecto) {
        Object valor = d.get(campo);
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        return porDefecto;
    }

    /**
     * Extrae y convierte a {@code double} el valor numérico de un campo de un {@link Document},
     * devolviendo {@code 0} si el campo es nulo o no puede parsearse como número.
     *
     * @param d     Documento BSON de origen.
     * @param campo Nombre de la clave numérica a extraer.
     * @return El valor numérico convertido a {@code double}, o {@code 0} en caso de ausencia o formato inválido.
     */
    protected double numero(Document d, String campo) {
        Object valor = d.get(campo);
        if (valor == null) {
            return 0;
        }
        try {
            return Double.parseDouble(valor.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}