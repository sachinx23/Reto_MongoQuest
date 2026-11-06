    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import exception.OurException;
import modelo.Valoracion;
import org.bson.Document;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public interface ValoracionDAO {
    Valoracion mapearValoracion(Document d);
    List<Valoracion> listarValoraciones() throws OurException;
    List<Valoracion> listarValoracionesPorPuntuacion(int puntuacion) throws OurException;
    double mediaValoraciones() throws OurException;
    boolean eliminarValoracion(Valoracion v) throws OurException;

    // Parte cliente
    List<Valoracion> obtenerValoracionesCliente(String idCliente) throws OurException;

    void insertarValoracion(Valoracion valoracion) throws OurException;

    void actualizarValoracion(Valoracion valoracion) throws OurException;
}
