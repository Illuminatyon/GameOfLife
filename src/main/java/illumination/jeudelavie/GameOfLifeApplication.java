package illumination.jeudelavie;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Application principale pour le Jeu de la Vie de Conway.
 * Cette classe est le point d'entrée de l'application.
 */
public class GameOfLifeApplication extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        // Charger le fichier FXML
        FXMLLoader fxmlLoader = new FXMLLoader(GameOfLifeApplication.class.getResource("game-of-life-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 700);
        scene.getStylesheets().add(GameOfLifeApplication.class.getResource("styles.css").toExternalForm());
        stage.setTitle("Jeu de la Vie de Conway");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    /**
     * Point d'entrée principal de l'application.
     *
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch();
    }
}