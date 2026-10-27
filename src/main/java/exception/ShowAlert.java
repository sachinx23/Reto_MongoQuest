package exception;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Ventanas emergentes de la aplicación.
 */
public class ShowAlert
{

    public static void showAlert(String title, String message, Alert.AlertType type)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void info(String title, String message)
    {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    public static void error(String message)
    {
        showAlert("Error", message, Alert.AlertType.ERROR);
    }

    // Devuelve true si el usuario pulsa Aceptar
    public static boolean confirmar(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> respuesta = alert.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }
}
