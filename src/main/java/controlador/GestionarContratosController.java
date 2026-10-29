/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import javafx.fxml.FXML;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Contrato;
import org.bson.Document;

/**
 *
 * @author Eneko.Plaza
 */
public class GestionarContratosController {

    //@FXML
  //  private TableView<Contrato> tablaContratos;
  // @FXML
   // private TableColumn<Contrato, String> colTelefono;
   // @FXML
  //  private TableColumn<Contrato, String> colEstado;
   // @FXML
  //  private TableColumn<Contrato, String> colTarifa;
   // @FXML
  //  private TableColumn<Contrato, String> colInicio;
   // @FXML
   // private TableColumn<Contrato, String> colFin;
   // @FXML
   // private Button btnFirmar;
   // @FXML
    //private Button btnRescindir;
    //@FXML
   // private Label lblMensaje;

   // private Document usuario;
   // private String rol;

    //public void setUsuario(Document usuario) {
     //   this.usuario = usuario;
     //   this.rol = usuario.getString("rol");
    //    configurarPorROl();
  //      cargarContratos();
  //  }

   // private void cargarContratos() {
     //   List<Contrato> lista;
       // if ("Cliente".equals(rol)) {
       //     lista = DAO.listarContratosCliente(usuario.getObjectId("_id").toString());

       // } else {
      //      lista = DAO.listarContratos();
     //   }

      //  tablaCOntratos.setItems(FXCollentions.observableArrayList(lista));

   // }

  //  @FXML
 //   private void firmarContrato() {
        //Contrato c = tablaContratos.getSelectionModel().getSelectedItem();
        //if (c == null) {
          //  mostrar("selecciona un contrato.");
         //   return;
       // }
        //if (!"pendiente".equals(c.getEstado())) {
           // mostrar("Solo se pueden firmar contratos pendientes.");
          //  return;
       // } else {
         //   mostrar("No se pudo firmar el contrato");
        //}
   // }

   // @FXML
    //private void rescindirContrato() {
        //Contrato c = tablasContrato.getSelectionModel().getSelectionItem();
        //if (c == null) {
          //  mostrar("selecciona un contrato.");
          //  return;
        //}
        //if (!c.getClass().equals("activo")) {
        //    mostrar("Solo se pueden rescindir contratos activos.");
         //   return;

       // }
      //  Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Seguro que quieres rescindir el contrato del" + c.getTelefono() + "?",ButtonType.YES, ButtonType.NO);
       
        
   // }
}
