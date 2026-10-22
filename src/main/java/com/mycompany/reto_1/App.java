package com.mycompany.reto_1;

import java.io.InputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargas la ventana de Login
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("vista/IniciarSesion.fxml"));
        
        // Buscas la imagen en la carpeta de recursos
        InputStream imagen = getClass().getClassLoader().getResourceAsStream("img/logo.png");
        
        // Si existe la imagen, la pones como icono
        if (imagen != null) {
            primaryStage.getIcons().add(new Image(imagen));
        } else {
            System.err.println("No se ha encontrado la imagen en src/main/resources/img/icono.png");
        }

        //Configuras la ventana 
        primaryStage.setTitle("Tel-ONDA");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}