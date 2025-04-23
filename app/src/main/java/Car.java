import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

/**
 * A JavaFX Pane representing a car on a grid-based Environment.
 * The car moves in discrete grid steps (one cell per keypress),
 * cannot move outside bounds or through walls, and is rendered as a circle.
 * Use initKeyControls(scene) to bind WASD movement.
 */
public class Car extends Pane {

    private static final double RADIUS = 20;
    public static double SPEED = 1;  // cells per keypress
    public static double VISION = 10;

    private final Circle circle;
    private final Environment environment;
    private int gridX;
    private int gridY;
    private final double cellSize;

    /**
     * Constructs a car placed on the given Environment at grid coords (startX,startY).
     * @param environment the grid-based Environment instance
     * @param startX initial column (0-based)
     * @param startY initial row (0-based)
     * @param cellSize size in pixels of one grid cell
     * @throws IllegalArgumentException if start position is invalid or a wall
     */
    public Car(Environment environment, int startX, int startY, double cellSize) {
        this.environment = environment;
        if (!environment.isValidPosition(startX, startY) || environment.isWall(startX, startY)) {
            throw new IllegalArgumentException(
                "Invalid start position or on a wall: (" + startX + "," + startY + ")");
        }
        this.gridX = startX;
        this.gridY = startY;
        this.cellSize = cellSize;

        // Pane size matches environment dimensions
        setPrefSize(environment.getWidth() * cellSize, environment.getHeight() * cellSize);

        // Initialize circle
        circle = new Circle(RADIUS, Color.BLUE);
        getChildren().add(circle);
        updateUICoord();
    }

    /** Sets movement speed in cells per keypress (integer). */
    public void setSpeed(double speed) {
        if (speed >= 0) {
            SPEED = speed;
        } else {
            throw new IllegalArgumentException("Speed must be non-negative.");
        }
    }
    public double getSpeed() { return SPEED; }

    /** Sets vision radius in cells (not used for movement). */
    public void setVision(double vision) {
        if (vision >= 0) {
            VISION = vision;
        } else {
            throw new IllegalArgumentException("Vision must be non-negative.");
        }
    }
    public double getVision() { return VISION; }

    /** @return current grid X (column) */
    public int getGridX() { return gridX; }
    /** @return current grid Y (row) */
    public int getGridY() { return gridY; }
    /** @return [gridX,gridY] */
    public int[] getGridPosition() { return new int[]{gridX, gridY}; }

    /** @return [scene X in pixels, scene Y in pixels] */
    public double[] getScenePosition() {
        return new double[]{circle.getCenterX(), circle.getCenterY()};
    }

    // Update circle's visual position based on grid coords
    private void updateUICoord() {
        double px = gridX * cellSize + cellSize / 2;
        double py = gridY * cellSize + cellSize / 2;
        circle.setCenterX(px);
        circle.setCenterY(py);
    }

    // Attempt to move by dx,dy cells; validate against walls and bounds
    private void move(int dx, int dy) {
        int newX = gridX + dx;
        int newY = gridY + dy;
        if (environment.isValidPosition(newX, newY) && !environment.isWall(newX, newY)) {
            gridX = newX;
            gridY = newY;
            updateUICoord();
        }
    }

    /**
     * Binds WASD keys on the given scene to move the car within the grid.
     * @param scene JavaFX Scene to attach key handlers to.
     */
    public void initKeyControls(Scene scene) {
        scene.setOnKeyPressed((KeyEvent event) -> {
            switch (event.getCode()) {
                case W: move(0, (int)-SPEED); break;
                case A: move((int)-SPEED, 0); break;
                case S: move(0, (int)SPEED); break;
                case D: move((int)SPEED, 0); break;
                default: break;
            }
        });
    }
}
