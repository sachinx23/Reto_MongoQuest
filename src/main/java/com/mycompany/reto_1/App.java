package com.mycompany.reto_1;

import java.io.InputStream;
import log.LogApp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        LogApp.configurar();

        Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
        InputStream imagen = getClass().getClassLoader().getResourceAsStream("img/logo.png");

        if (imagen != null) {
            primaryStage.getIcons().add(new Image(imagen));
        } else {
            System.err.println("No se ha encontrado la imagen en src/main/resources/img/icono.png");
        }

        primaryStage.setTitle("Tel-ONDA");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
