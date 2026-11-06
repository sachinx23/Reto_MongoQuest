/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin;

import modelo.Tarifa;
import java.util.List;

/**
 *
 * @author Eneko.Plaza
 */
public interface TarifaDAO {

    List<Tarifa> listarTarifas();

    boolean insertarTarifa(Tarifa t);

    boolean actualizarTarifa(Tarifa t);

    boolean eliminarTarifa(Tarifa t);
}
