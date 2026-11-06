package com.mycompany.reto_1;

import controlador.Sesion;
import daocliente.MongoConnection;
import exception.OurException;
import exception.ShowAlert;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.HostServices;
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
    private static HostServices servicios;

    @Override
    public void start(Stage stage) throws IOException {
        LogApp.configurar();
        servicios = getHostServices();

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
        scene.getStylesheets().add(Reto_1.class.getResource("/css/Estilos.css").toExternalForm());
        stage.getIcons().add(new Image(Reto_1.class.getResource("/img/logo.png").toExternalForm()));
        stage.setTitle("Tel-ONDA");
        stage.setScene(scene);
        stage.show();
        ajustarVentana(stage);
    }

    // Cambia la pantalla que se ve en la ventana, por ejemplo setRoot("Cliente")
    public static void setRoot(String fxml) {
        try {
            scene.setRoot(loadFXML(fxml));
            ajustarVentana((Stage) scene.getWindow());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir la pantalla " + fxml, e);
            ShowAlert.error("No se pudo abrir la pantalla.");
        }
    }

    // La ventana toma el tamaño de la pantalla nueva y no se puede hacer más pequeña que eso
    // (sí más grande: los controles se reparten el espacio)
    private static void ajustarVentana(Stage stage) {
        stage.setMaximized(false);
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    // Cerrar sesión (lo usan los paneles de cliente y empleado): pide confirmación y vuelve al login
    public static void cerrarSesion(String panel) {
        if (ShowAlert.confirmar("Salir", "¿Deseas salir del panel de " + panel + "?")) {
            Sesion.cerrar();
            setRoot("IniciarSesion");
        }
    }

    // Menú Archivo > Salir: cierra la aplicación entera después de confirmar
    public static void salir() {
        if (ShowAlert.confirmar("Salir", "¿Deseas cerrar la aplicación?")) {
            LOGGER.info("Aplicación cerrada desde el menú");
            Platform.exit();
        }
    }

    // Menú Ayuda > Acerca de
    public static void acercaDe() {
        ShowAlert.info("Acerca de", "Tel-ONDA 1.0\nReto MongoQuest\nSachin, Iñigo y Eneko Plaza");
    }

    // Abre una página web en el navegador del ordenador (por ejemplo, el mapa de una sucursal)
    public static void abrirEnNavegador(String url) {
        servicios.showDocument(url);
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
