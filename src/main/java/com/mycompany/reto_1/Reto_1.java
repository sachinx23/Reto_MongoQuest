package com.mycompany.reto_1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Reto_1 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carga la vista FXML de Iniciar Sesión desde las carpetas de recursos
        Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
        
        primaryStage.setTitle("Tel-ONDA - Iniciar Sesión");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Lanza la aplicación JavaFX
        launch(args);
    }
}