/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package daoadmin;

import modelo.Contrato;
import java.util.List;

/**
 *
 * @author Eneko.Plaza
 */
public interface ContratoDAO {
    List<Contrato> listarContratos();
    boolean aprobarContrato(Contrato c, String idGestor);
    boolean rechazarContratos(Contrato c);
    boolean rescindirContrato(Contrato c);
    List<Contrato> filtrarContratos(String telefono, String estado);
}