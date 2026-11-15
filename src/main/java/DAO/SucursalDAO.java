/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import exception.OurException;
import modelo.Sucursal;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public interface SucursalDAO {
    int siguienteIdSucursal() throws OurException;
    boolean insertarSucursal(Sucursal s) throws OurException;
    boolean actualizarSucursal(Sucursal s) throws OurException;
    boolean eliminarSucursal(String nombre) throws OurException;
    List<Sucursal> listarSucursales() throws OurException;
    
}
