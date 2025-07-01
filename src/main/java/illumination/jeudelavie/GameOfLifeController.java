package illumination.jeudelavie;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * Contrôleur pour l'interface utilisateur du Jeu de la Vie.
 * Gère les interactions utilisateur et la mise à jour de l'affichage.
 */
public class GameOfLifeController {

    @FXML private BorderPane rootPane;
    @FXML private Canvas gameCanvas;
    @FXML private Button startStopButton;
    @FXML private Button stepButton;
    @FXML private Button clearButton;
    @FXML private Button randomButton;
    @FXML private MenuItem startStopMenuItem;
    @FXML private MenuItem stepMenuItem;
    @FXML private Slider speedSlider;
    @FXML private Label speedValueLabel;
    @FXML private Slider zoomSlider;
    @FXML private Label zoomValueLabel;
    @FXML private Label statusLabel;

    private GameOfLife gameOfLife;
    private AnimationTimer gameLoop;
    private boolean isRunning = false;
    private double cellSize = 8.0; // Taille initiale des cellules
    private double offsetX = 0.0;  // Décalage X pour le panoramique
    private double offsetY = 0.0;  // Décalage Y pour le panoramique
    private double lastX = 0.0;    // Dernière position X de la souris pour le panoramique
    private double lastY = 0.0;    // Dernière position Y de la souris pour le panoramique
    private boolean isPanning = false; // Indique si l'utilisateur est en train de faire un panoramique
    private long lastUpdateTime = 0; // Temps de la dernière mise à jour
    private int frameCount = 0;    // Compteur de frames pour limiter la fréquence de mise à jour
    private int generationCount = 0; // Compteur de générations
    @FXML private Label generationCountLabel; // Étiquette pour afficher le nombre de générations

    /**
     * Initialise le contrôleur après le chargement du FXML.
     */
    @FXML
    public void initialize() {
        // Initialiser le modèle avec une taille basée sur la taille du canvas
        int gridWidth = (int) (gameCanvas.getWidth() / cellSize);
        int gridHeight = (int) (gameCanvas.getHeight() / cellSize);
        gameOfLife = new GameOfLife(gridWidth, gridHeight);

        // Configurer les écouteurs d'événements pour le canvas
        setupCanvasEvents();

        // Configurer les écouteurs pour les sliders
        setupSliders();

        // Configurer la boucle de jeu
        setupGameLoop();

        // Dessiner la grille initiale
        drawGrid();

        // Mettre à jour les étiquettes
        updateLabels();
    }

    /**
     * Configure les écouteurs d'événements pour le canvas.
     */
    private void setupCanvasEvents() {
        // Clic de souris pour ajouter/supprimer des cellules
        gameCanvas.setOnMouseClicked(this::handleCanvasClick);

        // Gestion du panoramique (déplacement de la vue)
        gameCanvas.setOnMousePressed(event -> {
            // Activer le panoramique uniquement si la touche Ctrl est enfoncée
            if (event.isControlDown()) {
                isPanning = true;
                lastX = event.getX();
                lastY = event.getY();
            }
        });

        gameCanvas.setOnMouseDragged(event -> {
            if (isPanning) {
                double deltaX = event.getX() - lastX;
                double deltaY = event.getY() - lastY;
                offsetX += deltaX;
                offsetY += deltaY;
                lastX = event.getX();
                lastY = event.getY();

                // Vérifier si l'utilisateur panne près des bords et agrandir la grille si nécessaire
                checkAndExpandGrid();

                drawGrid();
            }
        });

        gameCanvas.setOnMouseReleased(event -> {
            isPanning = false;
        });

        // Ajouter des écouteurs pour les touches du clavier
        rootPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.CONTROL) {
                gameCanvas.setCursor(javafx.scene.Cursor.MOVE);
            }
        });

        rootPane.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.CONTROL) {
                gameCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
    }

    /**
     * Configure les écouteurs pour les sliders.
     */
    private void setupSliders() {
        // Slider de vitesse
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            speedValueLabel.setText(String.format("%.0f", newValue));
        });

        // Slider de zoom
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            cellSize = newValue.doubleValue();
            zoomValueLabel.setText(String.format("%.0f", cellSize));

            // Redimensionner la grille en fonction du zoom
            resizeGrid();

            // Redessiner la grille
            drawGrid();
        });
    }

    /**
     * Configure la boucle de jeu pour les mises à jour automatiques.
     */
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Limiter la fréquence de mise à jour en fonction du slider de vitesse
                if (now - lastUpdateTime > 1_000_000_000 / speedSlider.getValue()) {
                    gameOfLife.nextGeneration();
                    generationCount++;
                    updateGenerationLabel();
                    drawGrid();
                    lastUpdateTime = now;
                }
            }
        };
    }

    /**
     * Gère le clic sur le bouton ou menu Démarrer/Arrêter.
     */
    @FXML
    private void onStartStopButtonClick() {
        isRunning = !isRunning;

        if (isRunning) {
            startStopButton.setText("Arrêter");
            startStopMenuItem.setText("Arrêter");
            gameLoop.start();
            stepButton.setDisable(true);
            stepMenuItem.setDisable(true);
        } else {
            startStopButton.setText("Démarrer");
            startStopMenuItem.setText("Démarrer");
            gameLoop.stop();
            stepButton.setDisable(false);
            stepMenuItem.setDisable(false);
        }

        updateStatusLabel();
    }

    /**
     * Gère le clic sur le menu Quitter.
     */
    @FXML
    private void onExitMenuItemClick() {
        Platform.exit();
    }

    /**
     * Gère le clic sur le menu À propos.
     */
    @FXML
    private void onAboutMenuItemClick() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("À propos du Jeu de la Vie");
        alert.setHeaderText("Jeu de la Vie de Conway");
        alert.setContentText("Implémentation du célèbre automate cellulaire inventé par John Conway en 1970.");
        alert.showAndWait();
    }

    /**
     * Gère le clic sur le menu Règles du jeu.
     */
    @FXML
    private void onRulesMenuItemClick() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Règles du Jeu de la Vie");
        alert.setHeaderText("Règles du Jeu de la Vie");
        alert.setContentText(
                "Le Jeu de la Vie est un automate cellulaire imaginé par John Horton Conway en 1970.\n\n" +
                "Règles:\n" +
                "1. Une cellule morte avec exactement 3 voisines vivantes devient vivante (elle naît).\n" +
                "2. Une cellule vivante avec 2 ou 3 voisines vivantes reste vivante (elle survit).\n" +
                "3. Dans tous les autres cas, une cellule meurt ou reste morte (par solitude ou surpopulation).\n\n" +
                "Utilisation:\n" +
                "- Cliquez sur la grille pour ajouter/supprimer des cellules.\n" +
                "- Maintenez la touche Ctrl enfoncée pour déplacer la vue.\n" +
                "- Utilisez les contrôles de zoom et de vitesse dans le menu Options.\n" +
                "- Démarrez/arrêtez la simulation avec le menu Simulation.");
        alert.showAndWait();
    }

    /**
     * Gère le clic sur le bouton Avancer d'un tour.
     */
    @FXML
    private void onStepButtonClick() {
        gameOfLife.nextGeneration();
        generationCount++;
        drawGrid();
        updateGenerationLabel();
        updateStatusLabel();
    }

    /**
     * Gère le clic sur le bouton Effacer.
     */
    @FXML
    private void onClearButtonClick() {
        gameOfLife.clear();
        generationCount = 0;
        drawGrid();
        updateGenerationLabel();
        updateStatusLabel();
    }

    /**
     * Gère le clic sur le bouton Aléatoire.
     */
    @FXML
    private void onRandomButtonClick() {
        gameOfLife.randomize(0.3); // 30% de cellules vivantes
        drawGrid();
        updateStatusLabel();
    }

    /**
     * Gère le clic sur le bouton - de la vitesse.
     */
    @FXML
    private void decreaseSpeed() {
        double value = speedSlider.getValue();
        if (value > speedSlider.getMin()) {
            speedSlider.setValue(value - 1);
        }
    }

    /**
     * Gère le clic sur le bouton + de la vitesse.
     */
    @FXML
    private void increaseSpeed() {
        double value = speedSlider.getValue();
        if (value < speedSlider.getMax()) {
            speedSlider.setValue(value + 1);
        }
    }

    /**
     * Gère le clic sur le bouton - du zoom.
     */
    @FXML
    private void decreaseZoom() {
        double value = zoomSlider.getValue();
        if (value > zoomSlider.getMin()) {
            zoomSlider.setValue(value - 1);
        }
    }

    /**
     * Gère le clic sur le bouton + du zoom.
     */
    @FXML
    private void increaseZoom() {
        double value = zoomSlider.getValue();
        if (value < zoomSlider.getMax()) {
            zoomSlider.setValue(value + 1);
        }
    }

    /**
     * Gère le clic sur le bouton Reset du zoom.
     */
    @FXML
    private void resetZoom() {
        zoomSlider.setValue(8); // Valeur par défaut
        offsetX = 0;
        offsetY = 0;
        drawGrid();
    }

    /**
     * Gère le clic sur le canvas pour ajouter/supprimer des cellules.
     */
    private void handleCanvasClick(MouseEvent event) {
        if (!event.isControlDown()) { // Pas la touche Ctrl (utilisée pour le panoramique)
            int gridX = (int) ((event.getX() - offsetX) / cellSize);
            int gridY = (int) ((event.getY() - offsetY) / cellSize);

            // Vérifier si les coordonnées sont dans les limites de la grille
            if (gridX >= 0 && gridX < gameOfLife.getWidth() && 
                gridY >= 0 && gridY < gameOfLife.getHeight()) {

                gameOfLife.toggleCell(gridX, gridY);
                drawGrid();
                updateStatusLabel();
            }
        }
    }

    /**
     * Redimensionne la grille en fonction de la taille du canvas et du zoom.
     * Cette méthode est appelée lors de l'initialisation et lors du zoom.
     */
    private void resizeGrid() {
        // Calculer la nouvelle taille de la grille en fonction de la taille du canvas et du zoom
        int newWidth = Math.max(1, (int) (gameCanvas.getWidth() / cellSize));
        int newHeight = Math.max(1, (int) (gameCanvas.getHeight() / cellSize));

        // Redimensionner seulement si nécessaire
        if (newWidth != gameOfLife.getWidth() || newHeight != gameOfLife.getHeight()) {
            gameOfLife.resize(newWidth, newHeight);
        }
    }

    /**
     * Vérifie si l'utilisateur panne près des bords et agrandit la grille si nécessaire.
     * Cette méthode est appelée lors du panoramique pour s'assurer que la grille est suffisamment grande.
     */
    private void checkAndExpandGrid() {
        // Calculer les coordonnées de la grille visibles à l'écran
        int minVisibleX = (int) Math.floor(-offsetX / cellSize);
        int minVisibleY = (int) Math.floor(-offsetY / cellSize);
        int maxVisibleX = (int) Math.ceil((gameCanvas.getWidth() - offsetX) / cellSize);
        int maxVisibleY = (int) Math.ceil((gameCanvas.getHeight() - offsetY) / cellSize);

        // Marge pour déclencher l'expansion (en nombre de cellules)
        int margin = 5;

        // Limites maximales pour la taille de la grille
        int maxGridWidth = 1000;  // Limite raisonnable pour éviter les problèmes de mémoire
        int maxGridHeight = 1000; // Limite raisonnable pour éviter les problèmes de mémoire

        // Vérifier si nous sommes près des bords et agrandir si nécessaire
        int currentWidth = gameOfLife.getWidth();
        int currentHeight = gameOfLife.getHeight();
        int newWidth = currentWidth;
        int newHeight = currentHeight;

        // Vérifier le bord gauche
        if (minVisibleX < margin) {
            int expansion = Math.min(margin - minVisibleX, maxGridWidth - currentWidth);
            if (expansion > 0) {
                newWidth += expansion;
                offsetX += expansion * cellSize; // Ajuster le décalage pour maintenir la vue
            }
        }

        // Vérifier le bord supérieur
        if (minVisibleY < margin) {
            int expansion = Math.min(margin - minVisibleY, maxGridHeight - currentHeight);
            if (expansion > 0) {
                newHeight += expansion;
                offsetY += expansion * cellSize; // Ajuster le décalage pour maintenir la vue
            }
        }

        // Vérifier le bord droit
        if (maxVisibleX > currentWidth - margin) {
            newWidth = Math.min(Math.max(newWidth, maxVisibleX + margin), maxGridWidth);
        }

        // Vérifier le bord inférieur
        if (maxVisibleY > currentHeight - margin) {
            newHeight = Math.min(Math.max(newHeight, maxVisibleY + margin), maxGridHeight);
        }

        // Redimensionner la grille si nécessaire et si les nouvelles dimensions sont raisonnables
        if ((newWidth != currentWidth || newHeight != currentHeight) && 
            newWidth <= maxGridWidth && newHeight <= maxGridHeight) {
            try {
                gameOfLife.resize(newWidth, newHeight);
            } catch (Exception e) {
                // En cas d'erreur lors du redimensionnement, afficher un message dans la console
                // et continuer sans redimensionner
                System.err.println("Erreur lors du redimensionnement de la grille: " + e.getMessage());

                // Mettre à jour le statut pour informer l'utilisateur
                statusLabel.setText("Limite de taille de grille atteinte. Impossible d'agrandir davantage.");
            }
        }
    }

    /**
     * Dessine la grille sur le canvas.
     */
    private void drawGrid() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Effacer le canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Dessiner les cellules vivantes
        gc.setFill(Color.LIGHTGREEN);

        for (int x = 0; x < gameOfLife.getWidth(); x++) {
            for (int y = 0; y < gameOfLife.getHeight(); y++) {
                if (gameOfLife.isAlive(x, y)) {
                    double screenX = x * cellSize + offsetX;
                    double screenY = y * cellSize + offsetY;

                    // Ne dessiner que les cellules visibles
                    if (screenX + cellSize >= 0 && screenX < gameCanvas.getWidth() &&
                        screenY + cellSize >= 0 && screenY < gameCanvas.getHeight()) {

                        gc.fillRect(screenX, screenY, cellSize - 1, cellSize - 1);
                    }
                }
            }
        }

        // Dessiner la grille si le zoom est suffisamment grand
        if (cellSize >= 4) {
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(0.5);

            // Lignes horizontales
            for (int y = 0; y <= gameOfLife.getHeight(); y++) {
                double screenY = y * cellSize + offsetY;
                if (screenY >= 0 && screenY <= gameCanvas.getHeight()) {
                    gc.strokeLine(0, screenY, gameCanvas.getWidth(), screenY);
                }
            }

            // Lignes verticales
            for (int x = 0; x <= gameOfLife.getWidth(); x++) {
                double screenX = x * cellSize + offsetX;
                if (screenX >= 0 && screenX <= gameCanvas.getWidth()) {
                    gc.strokeLine(screenX, 0, screenX, gameCanvas.getHeight());
                }
            }
        }
    }

    /**
     * Met à jour les étiquettes d'information.
     */
    private void updateLabels() {
        speedValueLabel.setText(String.format("%.0f", speedSlider.getValue()));
        zoomValueLabel.setText(String.format("%.0f", zoomSlider.getValue()));
        updateGenerationLabel();
        updateStatusLabel();
    }

    /**
     * Met à jour l'étiquette du compteur de générations.
     */
    private void updateGenerationLabel() {
        generationCountLabel.setText(String.valueOf(generationCount));
    }

    /**
     * Met à jour l'étiquette de statut.
     */
    private void updateStatusLabel() {
        if (isRunning) {
            statusLabel.setText("Simulation en cours... Cliquez sur 'Arrêter' pour mettre en pause.");
        } else {
            statusLabel.setText("Cliquez sur la grille pour ajouter/supprimer des cellules. Maintenez la touche Ctrl pour déplacer la vue.");
        }
    }
}
