package com.mycompany.reto_1;

import dao.MongoConnection;
import exception.OurException;
import exception.ShowAlert;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import log.LogApp;

public class Reto_1 extends Application {

    // Dirección de MongoDB y nombre de la base de datos
    private static final String URI = "mongodb://localhost:27017/?serverSelectionTimeoutMS=5000";
    private static final String BASE_DATOS = "Tel--ONDA";

    private static final Logger LOGGER = Logger.getLogger(Reto_1.class.getName());

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        LogApp.configurar();

        // La conexión se abre una sola vez al arrancar y se cierra en stop()
        try {
            MongoConnection.connect(URI, BASE_DATOS);
        } catch (OurException e) {
            ShowAlert.error(e.getMessage());
            Platform.exit();
            return;
        }

        scene = new Scene(loadFXML("IniciarSesion"));
        // La hoja de estilos se pone en la escena, así sirve para todas las pantallas
        scene.getStylesheets().add(Reto_1.class.getResource("/css/estilo.css").toExternalForm());
        stage.getIcons().add(new Image(Reto_1.class.getResourceAsStream("/img/logo.png")));
        stage.setTitle("Tel-ONDA");
        stage.setScene(scene);
        stage.show();
    }

    // Cambia la pantalla que se ve en la ventana, por ejemplo setRoot("Cliente")
    public static void setRoot(String fxml) {
        try {
            scene.setRoot(loadFXML(fxml));
            // La ventana se ajusta al tamaño de la pantalla nueva
            Stage stage = (Stage) scene.getWindow();
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir la pantalla " + fxml, e);
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }

    // Carga un fichero de la carpeta vista, por ejemplo loadFXML("Cliente") carga /vista/Cliente.fxml
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Reto_1.class.getResource("/vista/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() {
        MongoConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
