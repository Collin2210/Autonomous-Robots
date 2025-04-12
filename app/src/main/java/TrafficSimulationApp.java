import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class TrafficSimulationApp extends Application {

    private static final int CELL_SIZE = 25;
    private static final int PADDING = 2;
    private Environment environment;
    private Canvas canvas;
    private Timeline timeline;
    private boolean isSimulationRunning = false;

    @Override
    public void start(Stage primaryStage) {
        // Create the environment
        environment = new Environment(30, 20);
        environment.createSimpleRoadNetwork();
        
        // Create the canvas
        canvas = new Canvas(environment.getWidth() * CELL_SIZE, environment.getHeight() * CELL_SIZE);
        draw();
        
        // Create control buttons
        Button startStopButton = new Button("Start Simulation");
        startStopButton.setOnAction(e -> toggleSimulation(startStopButton));
        
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetSimulation());
        
        Button addTrafficLightButton = new Button("Add Traffic Light");
        addTrafficLightButton.setOnAction(e -> {
            // Wait for user to click on the canvas to place a traffic light
            canvas.setOnMouseClicked(event -> {
                int x = (int) (event.getX() / CELL_SIZE);
                int y = (int) (event.getY() / CELL_SIZE);
                if (environment.isValidPosition(x, y) && !environment.isWall(x, y)) {
                    environment.addTrafficLight(x, y, TrafficLightState.RED);
                    draw();
                    canvas.setOnMouseClicked(null); // Remove the click handler after placement
                }
            });
        });
        
        Button addTrafficSignButton = new Button("Add Stop Sign");
        addTrafficSignButton.setOnAction(e -> {
            canvas.setOnMouseClicked(event -> {
                int x = (int) (event.getX() / CELL_SIZE);
                int y = (int) (event.getY() / CELL_SIZE);
                if (environment.isValidPosition(x, y) && !environment.isWall(x, y)) {
                    environment.addTrafficSign(x, y, TrafficSignType.STOP);
                    draw();
                    canvas.setOnMouseClicked(null);
                }
            });
        });

        // Create the layout
        HBox controlsBox = new HBox(10, startStopButton, resetButton, addTrafficLightButton, addTrafficSignButton);
        controlsBox.setPadding(new javafx.geometry.Insets(10));
        
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(controlsBox);
        
        // Create the scene
        Scene scene = new Scene(root);
        primaryStage.setTitle("Traffic Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Create the timeline for animation
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateSimulation()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }
    
    private void toggleSimulation(Button startStopButton) {
        if (isSimulationRunning) {
            timeline.stop();
            startStopButton.setText("Start Simulation");
        } else {
            timeline.play();
            startStopButton.setText("Stop Simulation");
        }
        isSimulationRunning = !isSimulationRunning;
    }
    
    private void resetSimulation() {
        if (isSimulationRunning) {
            timeline.stop();
            isSimulationRunning = false;
        }
        
        environment = new Environment(30, 20);
        environment.createSimpleRoadNetwork();
        draw();
    }
    
    private void updateSimulation() {
        // Cycle all traffic lights
        for (int y = 0; y < environment.getHeight(); y++) {
            for (int x = 0; x < environment.getWidth(); x++) {
                Cell cell = environment.getCell(x, y);
                if (cell != null && cell.hasTrafficElements()) {
                    for (TrafficElement element : cell.getTrafficElements()) {
                        if (element instanceof TrafficLight trafficLight) {
                            trafficLight.cycleState();
                        }
                    }
                }
            }
        }
        
        // Redraw the canvas
        draw();
    }
    
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Draw background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw cells
        for (int y = 0; y < environment.getHeight(); y++) {
            for (int x = 0; x < environment.getWidth(); x++) {
                Cell cell = environment.getCell(x, y);
                if (cell != null) {
                    int cellX = x * CELL_SIZE;
                    int cellY = y * CELL_SIZE;
                    
                    // Draw cell background
                    if (cell.getType() == CellType.ROAD) {
                        gc.setFill(Color.DARKGRAY);
                    } else {
                        gc.setFill(Color.BROWN);
                    }
                    gc.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);
                    
                    // Draw traffic elements
                    if (cell.hasTrafficElements()) {
                        TrafficElement element = cell.getTrafficElements().get(0);
                        
                        if (element instanceof TrafficLight) {
                            drawTrafficLight(gc, cellX, cellY, (TrafficLight) element);
                        } else if (element instanceof TrafficSign) {
                            drawTrafficSign(gc, cellX, cellY, (TrafficSign) element);
                        }
                    }
                }
            }
        }
    }
    
    private void drawTrafficLight(GraphicsContext gc, int x, int y, TrafficLight light) {
        // Draw traffic light housing
        gc.setFill(Color.DARKGREY);
        gc.fillRect(x + PADDING, y + PADDING, CELL_SIZE - 2 * PADDING, CELL_SIZE - 2 * PADDING);
        
        // Draw the active light
        switch (light.getState()) {
            case RED:
                gc.setFill(Color.RED);
                break;
            case YELLOW:
                gc.setFill(Color.YELLOW);
                break;
            case GREEN:
                gc.setFill(Color.LIME);
                break;
        }
        gc.fillOval(x + CELL_SIZE/4, y + CELL_SIZE/4, CELL_SIZE/2, CELL_SIZE/2);
    }
    
    private void drawTrafficSign(GraphicsContext gc, int x, int y, TrafficSign sign) {
        switch (sign.getType()) {
            case STOP:
                // Red octagon
                gc.setFill(Color.RED);
                double[] xPoints = new double[8];
                double[] yPoints = new double[8];
                double radius = CELL_SIZE / 2 - PADDING;
                double centerX = x + CELL_SIZE / 2;
                double centerY = y + CELL_SIZE / 2;
                
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI / 8 + i * Math.PI / 4;
                    xPoints[i] = centerX + radius * Math.cos(angle);
                    yPoints[i] = centerY + radius * Math.sin(angle);
                }
                  
                gc.fillPolygon(xPoints, yPoints, 8);
                
                // White border text
                gc.setFill(Color.WHITE);
                gc.fillText("STOP", x + PADDING + 2, y + CELL_SIZE/2 + 4);
                break;
                
            case YIELD:
                // Triangle
                gc.setFill(Color.YELLOW);
                gc.fillPolygon(
                    new double[] {x + CELL_SIZE/2, x + PADDING, x + CELL_SIZE - PADDING},
                    new double[] {y + PADDING, y + CELL_SIZE - PADDING, y + CELL_SIZE - PADDING},
                    3
                );
                break;
                
            case SPEED_LIMIT:
                gc.setFill(Color.WHITE);
                gc.fillOval(x + PADDING, y + PADDING, CELL_SIZE - 2 * PADDING, CELL_SIZE - 2 * PADDING);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeOval(x + PADDING, y + PADDING, CELL_SIZE - 2 * PADDING, CELL_SIZE - 2 * PADDING);
                gc.setFill(Color.BLACK);
                gc.fillText("30", x + CELL_SIZE/3, y + CELL_SIZE/2 + 4);
                break;
                
            case ONE_WAY:
                gc.setFill(Color.BLUE);
                gc.fillRect(x + PADDING, y + PADDING, CELL_SIZE - 2 * PADDING, CELL_SIZE - 2 * PADDING);
                
                // Arrow
                gc.setFill(Color.WHITE);
                gc.fillPolygon(
                    new double[] {x + CELL_SIZE/2, x + CELL_SIZE/4, x + 3*CELL_SIZE/4},
                    new double[] {y + PADDING + 2, y + CELL_SIZE - PADDING - 2, y + CELL_SIZE - PADDING - 2},
                    3
                );
                gc.fillRect(x + CELL_SIZE/2 - 2, y + CELL_SIZE/3, 4, CELL_SIZE/3);
                break;
                
            case NO_ENTRY:
                gc.setFill(Color.WHITE);
                gc.fillOval(x + PADDING, y + PADDING, CELL_SIZE - 2 * PADDING, CELL_SIZE - 2 * PADDING);
                gc.setFill(Color.RED);
                gc.fillRect(x + PADDING + 2, y + CELL_SIZE/2 - 3, CELL_SIZE - 2 * PADDING - 4, 6);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeOval(x + PADDING, y + PADDING, CELL_SIZE - 2 * PADDING, CELL_SIZE - 2 * PADDING);
                break;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}