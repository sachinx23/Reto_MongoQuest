package com.mycompany.reto_1;

import dao.MongoConnection;
import exception.OurException;
import exception.ShowAlert;
import java.io.InputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Reto_1 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // La conexión se abre una sola vez al arrancar y se cierra en stop()
        try {
            MongoConnection.connect();
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
            Platform.exit();
            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("/vista/IniciarSesion.fxml"));
        InputStream imagen = getClass().getResourceAsStream("/img/logo.png");
        if (imagen != null) {
            primaryStage.getIcons().add(new Image(imagen));
        }
        primaryStage.setTitle("Tel-ONDA");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        MongoConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
