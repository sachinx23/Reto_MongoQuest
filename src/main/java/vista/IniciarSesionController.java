package vista;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField; // Corregido
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
/**
 * FXML Controller class
 *
 * @author Eneko.Plaza
 */
public class IniciarSesionController implements Initializable {

    @FXML
    private TextField txtUsuario;
    
    @FXML
    private PasswordField txtContrasena; 
    
    @FXML
    private Label lblMensajeError;
    
    @FXML
    private Button txtIniciarSesion;
    
    private final String URI_MONGODB = "mongodb://localhost:27017";
    private final String NOMBRE_MONGODB = "Tel-ONDA";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        lblMensajeError.setText("");
    }    
    
    @FXML
    public void btnIniciarSesion(){
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        
        if(usuario.isEmpty() || contrasena.isEmpty()){
            lblMensajeError.setText("No puedes iniciar sesion con los campos vacios.");
            return;
        }
       
        System.out.println("Buscando el usuario: " + usuario);
        
       try (MongoClient mongoCliente = MongoClients.create(URI_MONGODB)) {
            MongoDatabase database = mongoCliente.getDatabase(NOMBRE_MONGODB);
            
            Document filtro = new Document("nombreUsuario", usuario)
                                    .append("contraseña", contrasena);
    }
}
}