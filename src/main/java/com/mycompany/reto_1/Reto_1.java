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
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Reto_1 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Se abre la conexión a MongoDB una sola vez al arrancar (datos en config.properties)
        try {
            MongoConnection.connect();
        } catch (OurException e) {
            ShowAlert.showAlert("Error de conexión", e.getMessage(), Alert.AlertType.ERROR);
            Platform.exit();
            return;
        }

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

    @Override
    public void stop() {
        // Se cierra la conexión al cerrar la ventana
        MongoConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
