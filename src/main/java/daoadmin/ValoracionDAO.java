    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin;

import modelo.Valoracion;
import org.bson.Document;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public interface ValoracionDAO {
    Valoracion mapearValoracion(Document d);
    List<Valoracion> listarValoraciones();
    List<Valoracion> listarValoracionesPorPuntuacion(int puntuacion);
    double mediaValoraciones();
    boolean eliminarValoracion(Valoracion v);
}