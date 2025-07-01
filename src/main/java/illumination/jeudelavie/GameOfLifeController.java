package illumination.jeudelavie;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Game of Life application.
 */
public class GameOfLifeController {
    @FXML
    private Canvas gameCanvas;

    @FXML
    private Button startStopButton;

    @FXML
    private Button stepButton;

    @FXML
    private Button clearButton;

    @FXML
    private Slider speedSlider;

    @FXML
    private Label speedValueLabel;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Label zoomValueLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private BorderPane rootPane;

    private GameOfLife gameOfLife;
    private boolean isRunning = false;
    private AnimationTimer gameLoop;
    private long lastUpdate = 0;
    private int cellSize = 8; // Default cell size, will be updated by zoom
    private int generationCount = 0;
    private int lastCellRow = -1;
    private int lastCellCol = -1;

    // Selection variables
    private boolean isSelecting = false;
    private int selectionStartRow = -1;
    private int selectionStartCol = -1;
    private int selectionEndRow = -1;
    private int selectionEndCol = -1;
    private boolean[][] clipboardCells = null;

    @FXML
    public void initialize() {
        // Make the canvas resize with the window
        gameCanvas.widthProperty().bind(rootPane.widthProperty().subtract(20));
        gameCanvas.heightProperty().bind(rootPane.heightProperty().subtract(150));

        // Listen for canvas size changes and update the grid
        gameCanvas.widthProperty().addListener((obs, oldVal, newVal) -> resetGrid());
        gameCanvas.heightProperty().addListener((obs, oldVal, newVal) -> resetGrid());

        // Initialize the game model with initial grid dimensions
        resetGrid();

        // Set up mouse event handlers for the canvas
        gameCanvas.setOnMousePressed(this::handleMousePressed);
        gameCanvas.setOnMouseDragged(this::handleMouseDragged);
        gameCanvas.setOnMouseReleased(this::handleMouseReleased);

        // Set up key event handlers for the canvas
        gameCanvas.setFocusTraversable(true);
        gameCanvas.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        gameCanvas.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        // Set up the game loop with Platform.runLater to ensure UI updates
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate time since last update
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                // Get the speed from the slider (invert it so higher values mean faster)
                double speed = speedSlider.getMax() - speedSlider.getValue() + speedSlider.getMin();

                // Calculate the update interval in nanoseconds
                long updateInterval = (long) (1_000_000_000 / speed);

                if (now - lastUpdate >= updateInterval) {
                    // Use Platform.runLater to ensure UI updates properly
                    Platform.runLater(() -> {
                        gameOfLife.nextGeneration();
                        generationCount++;
                        updateStatusLabel();
                        drawGrid();
                    });
                    lastUpdate = now;
                }
            }
        };

        // Apply styling to UI elements
        applyStyles();

        // Initialize status label
        updateStatusLabel();

        // Initialize speed value label and add listener to keep it in sync with slider
        updateSpeedValueLabel();
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateSpeedValueLabel());

        // Initialize zoom value label and add listener to keep it in sync with slider
        updateZoomValueLabel();
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateZoomValueLabel();
            updateCellSize();
            resetGrid();
        });
    }

    /**
     * Updates the speed value label to match the current slider value.
     */
    private void updateSpeedValueLabel() {
        int speedValue = (int) speedSlider.getValue();
        speedValueLabel.setText(String.valueOf(speedValue));
    }

    /**
     * Decreases the simulation speed by decrementing the slider value.
     */
    @FXML
    protected void decreaseSpeed() {
        double currentValue = speedSlider.getValue();
        if (currentValue > speedSlider.getMin()) {
            speedSlider.setValue(currentValue - 1);
        }
    }

    /**
     * Increases the simulation speed by incrementing the slider value.
     */
    @FXML
    protected void increaseSpeed() {
        double currentValue = speedSlider.getValue();
        if (currentValue < speedSlider.getMax()) {
            speedSlider.setValue(currentValue + 1);
        }
    }

    /**
     * Updates the zoom value label to match the current slider value.
     */
    private void updateZoomValueLabel() {
        int zoomValue = (int) zoomSlider.getValue();
        zoomValueLabel.setText(String.valueOf(zoomValue));
    }

    /**
     * Updates the cell size based on the zoom slider value.
     */
    private void updateCellSize() {
        cellSize = (int) zoomSlider.getValue();
    }

    /**
     * Decreases the zoom level by decrementing the slider value.
     */
    @FXML
    protected void decreaseZoom() {
        double currentValue = zoomSlider.getValue();
        if (currentValue > zoomSlider.getMin()) {
            zoomSlider.setValue(currentValue - 1);
        }
    }

    /**
     * Increases the zoom level by incrementing the slider value.
     */
    @FXML
    protected void increaseZoom() {
        double currentValue = zoomSlider.getValue();
        if (currentValue < zoomSlider.getMax()) {
            zoomSlider.setValue(currentValue + 1);
        }
    }

    /**
     * Resets the zoom level to the default value (8).
     */
    @FXML
    protected void resetZoom() {
        zoomSlider.setValue(8);
    }

    /**
     * Apply styling to UI elements for better aesthetics
     */
    private void applyStyles() {
        // No need to set inline styles as they are defined in the CSS file
        // This method is kept for potential future styling needs
    }

    /**
     * Resets the grid when the canvas size changes.
     */
    private void resetGrid() {
        // Calculate grid dimensions based on canvas size
        int rows = (int) (gameCanvas.getHeight() / cellSize);
        int cols = (int) (gameCanvas.getWidth() / cellSize);

        // Ensure we have at least one row and column
        rows = Math.max(rows, 1);
        cols = Math.max(cols, 1);

        // Initialize the game model
        gameOfLife = new GameOfLife(rows, cols);
        generationCount = 0;
        updateStatusLabel();

        // Draw the initial grid
        drawGrid();
    }

    /**
     * Updates the status label with the current generation count.
     */
    private void updateStatusLabel() {
        statusLabel.setText("Génération: " + generationCount + 
                " | Grille: " + gameOfLife.getRows() + "×" + gameOfLife.getColumns() + 
                " | Ctrl+clic pour sélectionner | C pour copier | V pour coller");
    }

    @FXML
    protected void onStartStopButtonClick() {
        isRunning = !isRunning;

        if (isRunning) {
            startStopButton.setText("Arrêter");
            stepButton.setDisable(true);
            gameLoop.start();
        } else {
            startStopButton.setText("Démarrer");
            stepButton.setDisable(false);
            gameLoop.stop();
        }
    }

    @FXML
    protected void onStepButtonClick() {
        gameOfLife.nextGeneration();
        generationCount++;
        updateStatusLabel();
        drawGrid();
    }

    @FXML
    protected void onClearButtonClick() {
        gameOfLife.clearGrid();
        generationCount = 0;
        updateStatusLabel();
        drawGrid();
    }

    /**
     * Randomly populates the grid with live cells.
     */
    @FXML
    protected void onRandomButtonClick() {
        // Use a moderate density (0.3 = 30% of cells will be alive)
        gameOfLife.randomizeGrid(0.3);
        generationCount = 0;
        updateStatusLabel();
        drawGrid();
    }

    /**
     * Adds a glider pattern at the specified position.
     */
    @FXML
    protected void onAddGliderButtonClick() {
        // Add a glider pattern at the center of the grid
        int centerRow = gameOfLife.getRows() / 2;
        int centerCol = gameOfLife.getColumns() / 2;

        // Clear the area first
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                gameOfLife.setCellState(centerRow + i, centerCol + j, false);
            }
        }

        // Create a glider pattern
        gameOfLife.setCellState(centerRow - 1, centerCol, true);
        gameOfLife.setCellState(centerRow, centerCol + 1, true);
        gameOfLife.setCellState(centerRow + 1, centerCol - 1, true);
        gameOfLife.setCellState(centerRow + 1, centerCol, true);
        gameOfLife.setCellState(centerRow + 1, centerCol + 1, true);

        drawGrid();
    }

    /**
     * Adds an oscillator pattern at the specified position.
     */
    @FXML
    protected void onAddOscillatorButtonClick() {
        // Add a blinker (period 2 oscillator) at the center of the grid
        int centerRow = gameOfLife.getRows() / 2;
        int centerCol = gameOfLife.getColumns() / 2;

        // Clear the area first
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                gameOfLife.setCellState(centerRow + i, centerCol + j, false);
            }
        }

        // Create a blinker pattern (vertical line of 3 cells)
        gameOfLife.setCellState(centerRow - 1, centerCol, true);
        gameOfLife.setCellState(centerRow, centerCol, true);
        gameOfLife.setCellState(centerRow + 1, centerCol, true);

        drawGrid();
    }

    private void handleMousePressed(MouseEvent event) {
        // Convert mouse coordinates to grid coordinates
        int col = (int) (event.getX() / cellSize);
        int row = (int) (event.getY() / cellSize);

        // Check if Ctrl is pressed for selection
        if (event.isControlDown()) {
            // Start selection
            isSelecting = true;
            selectionStartRow = row;
            selectionStartCol = col;
            selectionEndRow = row;
            selectionEndCol = col;
        } else {
            // Set cell state to alive (not toggle) to prevent disappearing
            gameOfLife.setCellState(row, col, true);
            lastCellRow = row;
            lastCellCol = col;
        }

        // Redraw the grid
        drawGrid();
    }

    private void handleMouseDragged(MouseEvent event) {
        // Convert mouse coordinates to grid coordinates
        int col = (int) (event.getX() / cellSize);
        int row = (int) (event.getY() / cellSize);

        if (isSelecting) {
            // Update selection end point
            selectionEndRow = row;
            selectionEndCol = col;
        } else if (row != lastCellRow || col != lastCellCol) {
            // Set cell state to alive (not toggle) to prevent disappearing
            gameOfLife.setCellState(row, col, true);
            lastCellRow = row;
            lastCellCol = col;
        }

        // Redraw the grid
        drawGrid();
    }

    private void handleMouseReleased(MouseEvent event) {
        if (isSelecting) {
            // Finalize selection
            drawGrid();
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.isControlDown() && event.getCode() == KeyCode.C) {
            // Copy selected cells
            copySelectedCells();
        } else if (event.isControlDown() && event.getCode() == KeyCode.V) {
            // Paste copied cells
            pasteSelectedCells(lastCellRow, lastCellCol);
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        // Reset selection if Ctrl is released
        if (!event.isControlDown() && isSelecting) {
            isSelecting = false;
            drawGrid();
        }
    }

    private void copySelectedCells() {
        if (selectionStartRow < 0 || selectionStartCol < 0) return;

        // Normalize selection coordinates
        int startRow = Math.min(selectionStartRow, selectionEndRow);
        int endRow = Math.max(selectionStartRow, selectionEndRow);
        int startCol = Math.min(selectionStartCol, selectionEndCol);
        int endCol = Math.max(selectionStartCol, selectionEndCol);

        int rows = endRow - startRow + 1;
        int cols = endCol - startCol + 1;

        // Create clipboard
        clipboardCells = new boolean[rows][cols];

        // Copy cells to clipboard
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                clipboardCells[r][c] = gameOfLife.getCellState(startRow + r, startCol + c);
            }
        }

        statusLabel.setText("Cellules copiées: " + rows + "×" + cols);
    }

    private void pasteSelectedCells(int targetRow, int targetCol) {
        if (clipboardCells == null) return;

        int rows = clipboardCells.length;
        int cols = clipboardCells[0].length;

        // Paste cells from clipboard
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                gameOfLife.setCellState(targetRow + r, targetCol + c, clipboardCells[r][c]);
            }
        }

        drawGrid();
        statusLabel.setText("Cellules collées à la position (" + targetRow + "," + targetCol + ")");
    }

    private void drawGrid() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Draw background with a dark color
        gc.setFill(new Color(0.12, 0.15, 0.18, 1.0)); // Dark background color (#1e272e)
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Draw the grid lines with a more subtle color for dark theme
        gc.setStroke(new Color(0.3, 0.3, 0.3, 0.5));
        gc.setLineWidth(0.5);

        // Draw horizontal grid lines
        for (int i = 0; i <= gameOfLife.getRows(); i++) {
            double y = i * cellSize;
            gc.strokeLine(0, y, gameCanvas.getWidth(), y);
        }

        // Draw vertical grid lines
        for (int i = 0; i <= gameOfLife.getColumns(); i++) {
            double x = i * cellSize;
            gc.strokeLine(x, 0, x, gameCanvas.getHeight());
        }

        // Draw the cells with a bright color for better visibility on dark background
        gc.setFill(new Color(0.3, 0.8, 1.0, 0.9)); // Bright cyan color

        for (int row = 0; row < gameOfLife.getRows(); row++) {
            for (int col = 0; col < gameOfLife.getColumns(); col++) {
                if (gameOfLife.getCellState(row, col)) {
                    // Draw cells with slightly rounded corners and smaller size for a nicer look
                    double cellPadding = 0.5;
                    double cellX = col * cellSize + cellPadding;
                    double cellY = row * cellSize + cellPadding;
                    double cellWidth = cellSize - (2 * cellPadding);
                    double cellHeight = cellSize - (2 * cellPadding);

                    // Draw rounded rectangle for each cell
                    gc.fillRoundRect(cellX, cellY, cellWidth, cellHeight, 2, 2);
                }
            }
        }

        // Draw selection rectangle if selecting
        if (isSelecting) {
            // Normalize selection coordinates
            int startRow = Math.min(selectionStartRow, selectionEndRow);
            int endRow = Math.max(selectionStartRow, selectionEndRow);
            int startCol = Math.min(selectionStartCol, selectionEndCol);
            int endCol = Math.max(selectionStartCol, selectionEndCol);

            // Calculate pixel coordinates
            double startX = startCol * cellSize;
            double startY = startRow * cellSize;
            double width = (endCol - startCol + 1) * cellSize;
            double height = (endRow - startRow + 1) * cellSize;

            // Draw semi-transparent blue rectangle with a gradient
            gc.setFill(new Color(0.2, 0.4, 0.8, 0.2));
            gc.fillRoundRect(startX, startY, width, height, 6, 6);

            // Draw animated dashed selection border
            gc.setStroke(new Color(0.2, 0.6, 1.0, 0.8));
            gc.setLineWidth(2);
            gc.setLineDashes(5, 5);
            gc.setLineDashOffset((System.currentTimeMillis() / 100) % 10);
            gc.strokeRoundRect(startX, startY, width, height, 6, 6);

            // Reset line dashes for future drawing operations
            gc.setLineDashes(null);
        }
    }

    public Button getClearButton() {
        return clearButton;
    }

    public void setClearButton(Button clearButton) {
        this.clearButton = clearButton;
    }
}
