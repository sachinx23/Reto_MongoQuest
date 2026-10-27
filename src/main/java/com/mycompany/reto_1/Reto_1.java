package com.mycompany.reto_1;

import java.io.InputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Reto_1 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carga el FXML desde src/main/resources/vista/IniciarSesion.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
        
        // Carga la imagen desde src/main/resources/img/logo.png
        InputStream imagen = getClass().getResourceAsStream("/img/logo.png");
        if (imagen != null) {
            primaryStage.getIcons().add(new Image(imagen));
        }

        primaryStage.setTitle("Tel--ONDA - Iniciar Sesión");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}