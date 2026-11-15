/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import exception.OurException;
import modelo.Contrato;
import java.util.List;
import modelo.Tarifa;

/**
 *
 * @author Eneko.Plaza
 */
public interface ContratoDAO {
    List<Contrato> listarContratos() throws OurException;
    boolean aprobarContrato(Contrato c, String idGestor) throws OurException;
    boolean firmarContrato(Contrato c) throws OurException;
    boolean rechazarContratos(Contrato c) throws OurException;
    boolean rescindirContrato(Contrato c) throws OurException;
    List<Contrato> filtrarContratos(String telefono, String estado) throws OurException;

    // Parte cliente
    List<Contrato> obtenerContratosCliente(String idCliente) throws OurException;

    void insertarContrato(Contrato contrato, Tarifa tarifa) throws OurException;

    boolean borrarPeticion(String idCliente, String idTarifa) throws OurException;

    void solicitarBaja(String idContrato) throws OurException;
}
