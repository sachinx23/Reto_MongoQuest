/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.IOException;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
/**
 *
 * @author Eneko.Plaza
 */
public class EmpleadoController {
    // @FXML
    //private ComboBox<String> cbOpcionesAdmin;
     @FXML 
    private Label lblNombreEmpleado;
    
   // @Override
    //public void initialize(URL url, ResourceBundle rb) {
        // Opciones del ComboBox
      //  cbOpcionesEmpleado.setItems(FXCollections.observableArrayList(
      //          "Gestionar Plantilla",
      //          "Gestionar Sucursal",
       //         "Gestionar Tarifas",
       //         "Cerrar Sesión"
       // ));
        
    @FXML
    public void setNombreUsuario(String rol) {
        if (lblNombreEmpleado != null && rol != null) {
            lblNombreEmpleado.setText("Empleado: " + rol);
        }
    }
}

