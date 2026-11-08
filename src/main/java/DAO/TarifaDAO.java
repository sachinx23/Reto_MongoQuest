/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import exception.OurException;
import modelo.Tarifa;
import java.util.List;

/**
 *
 * @author Eneko.Plaza
 */
public interface TarifaDAO {

    List<Tarifa> listarTarifas() throws OurException;

    boolean insertarTarifa(Tarifa t) throws OurException;

    boolean actualizarTarifa(Tarifa t) throws OurException;

    boolean eliminarTarifa(Tarifa t) throws OurException;

    // Parte cliente
    Tarifa buscarTarifa(String idTarifa) throws OurException;

    List<Tarifa> tarifasActivas() throws OurException;
}
