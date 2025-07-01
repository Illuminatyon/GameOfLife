package illumination.jeudelavie;

/**
 * This class represents the Game of Life model.
 * It handles the grid representation and the rules for cell evolution.
 */
public class GameOfLife {
    private boolean[][] grid;
    private int rows;
    private int columns;

    /**
     * Constructor for the GameOfLife class.
     * @param rows The number of rows in the grid.
     * @param columns The number of columns in the grid.
     */
    public GameOfLife(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new boolean[rows][columns];
        // Initialize with all cells dead
        clearGrid();
    }

    /**
     * Clears the grid by setting all cells to dead.
     */
    public void clearGrid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                grid[i][j] = false;
            }
        }
    }

    /**
     * Sets the state of a cell.
     * @param row The row of the cell.
     * @param column The column of the cell.
     * @param alive Whether the cell is alive or dead.
     */
    public void setCellState(int row, int column, boolean alive) {
        if (isValidCell(row, column)) {
            grid[row][column] = alive;
        }
    }

    /**
     * Toggles the state of a cell.
     * @param row The row of the cell.
     * @param column The column of the cell.
     */
    public void toggleCellState(int row, int column) {
        if (isValidCell(row, column)) {
            grid[row][column] = !grid[row][column];
        }
    }

    /**
     * Gets the state of a cell.
     * @param row The row of the cell.
     * @param column The column of the cell.
     * @return Whether the cell is alive or dead.
     */
    public boolean getCellState(int row, int column) {
        if (isValidCell(row, column)) {
            return grid[row][column];
        }
        return false;
    }

    /**
     * Checks if a cell position is valid.
     * @param row The row of the cell.
     * @param column The column of the cell.
     * @return Whether the cell position is valid.
     */
    private boolean isValidCell(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }

    /**
     * Counts the number of live neighbors for a cell.
     * @param row The row of the cell.
     * @param column The column of the cell.
     * @return The number of live neighbors.
     */
    private int countLiveNeighbors(int row, int column) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip the cell itself
                int newRow = row + i;
                int newCol = column + j;
                if (isValidCell(newRow, newCol) && grid[newRow][newCol]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Advances the game by one generation.
     */
    public void nextGeneration() {
        boolean[][] newGrid = new boolean[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int liveNeighbors = countLiveNeighbors(i, j);
                boolean currentState = grid[i][j];

                // Apply Game of Life rules
                // 1. Any live cell with fewer than two live neighbors dies (underpopulation)
                // 2. Any live cell with two or three live neighbors lives on
                // 3. Any live cell with more than three live neighbors dies (overpopulation)
                // 4. Any dead cell with exactly three live neighbors becomes a live cell (reproduction)

                if (currentState) {
                    // Cell is alive
                    newGrid[i][j] = liveNeighbors == 2 || liveNeighbors == 3;
                } else {
                    // Cell is dead
                    newGrid[i][j] = liveNeighbors == 3;
                }
            }
        }

        // Update the grid
        grid = newGrid;
    }

    /**
     * Gets the number of rows in the grid.
     * @return The number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns in the grid.
     * @return The number of columns.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Randomly populates the grid with live cells.
     * @param density The probability (0.0 to 1.0) of a cell being alive.
     */
    public void randomizeGrid(double density) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                grid[i][j] = Math.random() < density;
            }
        }
    }
}
