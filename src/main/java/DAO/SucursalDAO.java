/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import modelo.Sucursal;
import java.util.List;
/**
 *
 * @author Eneko.Plaza
 */
public interface SucursalDAO {
    int siguienteIdSucursal();
    boolean insertarSucursal(Sucursal s);
    boolean actualizarSucursal(Sucursal s);
    boolean eliminarSucursal(String nombre);
    List<Sucursal> listarSucursales();
    
}
