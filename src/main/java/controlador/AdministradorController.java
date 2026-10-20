package controlador;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
/**
 * FXML Controller class
 *
 * @author Eneko.Plaza
 */
public class AdministradorController {

    @FXML
    private AnchorPane panelContenido;
    
    
    private void cargarVista(String ruta) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(ruta));
            panelContenido.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void altaEmpleado(ActionEvent event){
        cargarVista("/vista/AltaEmpleado.fxml");
    }
    
    @FXML
    public void cambiarPuesto(ActionEvent event) {
        cargarVista("/vista/CambiarPuesto.fxml");
    }
  
    @FXML
    public void borrarEmpleado(ActionEvent event) {
        cargarVista("/vista/BorrarEmpleado.fxml");
    }
    @FXML
    public void gestionSucursal(ActionEvent event){
        cargarVista("/vista/GestionSucursal.fxml");
    }

    @FXML
    public void cerrarSesion(ActionEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vista/InciarSesion.fxml"));
            Stage stage = (Stage) panelContenido.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }