package illumination.jeudelavie;

/**
 * Modèle pour le Jeu de la Vie de Conway.
 * Cette classe gère la logique du jeu, y compris l'état de la grille et les règles d'évolution.
 */
public class GameOfLife {
    private boolean[][] grid;
    private int width;
    private int height;

    /**
     * Constructeur qui initialise une grille vide avec les dimensions spécifiées.
     *
     * @param width  Largeur de la grille
     * @param height Hauteur de la grille
     */
    public GameOfLife(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new boolean[width][height];
        clear();
    }

    /**
     * Efface la grille (toutes les cellules mortes).
     */
    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = false;
            }
        }
    }

    /**
     * Remplit la grille avec des cellules aléatoires.
     *
     * @param density Densité des cellules vivantes (0.0 à 1.0)
     */
    public void randomize(double density) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = Math.random() < density;
            }
        }
    }

    /**
     * Fait évoluer la grille d'une génération selon les règles du Jeu de la Vie.
     * Règles:
     * 1. Une cellule morte avec exactement 3 voisines vivantes devient vivante.
     * 2. Une cellule vivante avec 2 ou 3 voisines vivantes reste vivante.
     * 3. Dans tous les autres cas, une cellule meurt ou reste morte.
     */
    public void nextGeneration() {
        boolean[][] newGrid = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int neighbors = countNeighbors(x, y);

                // Appliquer les règles du Jeu de la Vie
                if (grid[x][y]) {
                    // Cellule vivante
                    newGrid[x][y] = neighbors == 2 || neighbors == 3;
                } else {
                    // Cellule morte
                    newGrid[x][y] = neighbors == 3;
                }
            }
        }

        // Mettre à jour la grille
        grid = newGrid;
    }

    /**
     * Compte le nombre de voisins vivants pour une cellule donnée.
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     * @return Nombre de voisins vivants (0-8)
     */
    private int countNeighbors(int x, int y) {
        int count = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Ignorer la cellule elle-même

                int nx = (x + i + width) % width;   // Gestion des bords (toroïdal)
                int ny = (y + j + height) % height; // Gestion des bords (toroïdal)

                if (grid[nx][ny]) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Bascule l'état d'une cellule (vivante à morte ou morte à vivante).
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     */
    public void toggleCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[x][y] = !grid[x][y];
        }
    }

    /**
     * Définit l'état d'une cellule.
     *
     * @param x     Coordonnée X de la cellule
     * @param y     Coordonnée Y de la cellule
     * @param alive true pour vivante, false pour morte
     */
    public void setCell(int x, int y, boolean alive) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[x][y] = alive;
        }
    }

    /**
     * Vérifie si une cellule est vivante.
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     * @return true si la cellule est vivante, false sinon
     */
    public boolean isAlive(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return grid[x][y];
        }
        return false;
    }

    /**
     * Obtient la largeur de la grille.
     *
     * @return Largeur de la grille
     */
    public int getWidth() {
        return width;
    }

    /**
     * Obtient la hauteur de la grille.
     *
     * @return Hauteur de la grille
     */
    public int getHeight() {
        return height;
    }

    /**
     * Redimensionne la grille tout en préservant les cellules existantes.
     *
     * @param newWidth  Nouvelle largeur
     * @param newHeight Nouvelle hauteur
     * @throws IllegalArgumentException si les dimensions sont négatives ou trop grandes
     */
    public void resize(int newWidth, int newHeight) {
        // Validation des dimensions
        if (newWidth <= 0 || newHeight <= 0) {
            throw new IllegalArgumentException("Les dimensions de la grille doivent être positives");
        }

        // Limites maximales pour éviter les problèmes de mémoire
        final int MAX_DIMENSION = 2000;
        if (newWidth > MAX_DIMENSION || newHeight > MAX_DIMENSION) {
            throw new IllegalArgumentException("Les dimensions de la grille sont trop grandes");
        }

        // Éviter le redimensionnement inutile
        if (newWidth == width && newHeight == height) {
            return;
        }

        try {
            // Créer une nouvelle grille avec les nouvelles dimensions
            boolean[][] newGrid = new boolean[newWidth][newHeight];

            // Copier les cellules existantes dans la nouvelle grille
            for (int x = 0; x < Math.min(width, newWidth); x++) {
                for (int y = 0; y < Math.min(height, newHeight); y++) {
                    newGrid[x][y] = grid[x][y];
                }
            }

            this.width = newWidth;
            this.height = newHeight;
            this.grid = newGrid;
        } catch (OutOfMemoryError e) {
            // En cas d'erreur de mémoire, conserver la grille actuelle et lancer une exception
            throw new RuntimeException("Mémoire insuffisante pour redimensionner la grille", e);
        }
    }
}
