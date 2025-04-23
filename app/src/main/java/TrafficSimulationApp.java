import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private Car car;

    @Override
    public void start(Stage primaryStage) {
        // Create the environment
        environment = new Environment(30, 20);
        environment.createSimpleRoadNetwork();
        
        // Create the canvas
        canvas = new Canvas(environment.getWidth() * CELL_SIZE, environment.getHeight() * CELL_SIZE);
        draw();
        
        // Create the car, placed at cell (1,1)
        car = new Car(environment, 1, 1, CELL_SIZE);
        
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
                    canvas.setOnMouseClicked(null);
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

        // Layout: stack canvas and car pane
        StackPane centerPane = new StackPane(canvas, car);
        
        HBox controlsBox = new HBox(10, startStopButton, resetButton, addTrafficLightButton, addTrafficSignButton);
        controlsBox.setPadding(new javafx.geometry.Insets(10));
        
        BorderPane root = new BorderPane();
        root.setCenter(centerPane);
        root.setBottom(controlsBox);
        
        // Scene setup
        Scene scene = new Scene(root);
        primaryStage.setTitle("Traffic Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Bind car controls
        car.initKeyControls(scene);
        
        // Animation timeline
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
        car = new Car(environment, 1, 1, CELL_SIZE);
    }
    
    private void updateSimulation() {
        // Cycle traffic lights
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
                int cellX = x * CELL_SIZE;
                int cellY = y * CELL_SIZE;
                
                gc.setFill(cell.getType() == CellType.ROAD ? Color.DARKGRAY : Color.BROWN);
                gc.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);

                if (cell.hasTrafficElements()) {
                    TrafficElement element = cell.getTrafficElements().get(0);
                    if (element instanceof TrafficLight) {
                        drawTrafficLight(gc, cellX, cellY, (TrafficLight)element);
                    } else {
                        drawTrafficSign(gc, cellX, cellY, (TrafficSign)element);
                    }
                }
            }
        }
    }
    
    private void drawTrafficLight(GraphicsContext gc, int x, int y, TrafficLight light) {
        gc.setFill(Color.DARKGREY);
        gc.fillRect(x + PADDING, y + PADDING, CELL_SIZE - 2*PADDING, CELL_SIZE - 2*PADDING);
        switch (light.getState()) {
            case RED    -> gc.setFill(Color.RED);
            case YELLOW -> gc.setFill(Color.YELLOW);
            case GREEN  -> gc.setFill(Color.LIME);
        }
        gc.fillOval(x + CELL_SIZE/4, y + CELL_SIZE/4, CELL_SIZE/2, CELL_SIZE/2);
    }
    
    private void drawTrafficSign(GraphicsContext gc, int x, int y, TrafficSign sign) {
        switch (sign.getType()) {
            case STOP -> {
                gc.setFill(Color.RED);
                double[] xp = new double[8];
                double[] yp = new double[8];
                double r = CELL_SIZE/2 - PADDING;
                double cx = x + CELL_SIZE/2;
                double cy = y + CELL_SIZE/2;
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI/8 + i * Math.PI/4;
                    xp[i] = cx + r * Math.cos(angle);
                    yp[i] = cy + r * Math.sin(angle);
                }
                gc.fillPolygon(xp, yp, 8);
                gc.setFill(Color.WHITE);
                gc.fillText("STOP", x + PADDING + 2, y + CELL_SIZE/2 + 4);
            }
            case YIELD -> {
                gc.setFill(Color.YELLOW);
                gc.fillPolygon(
                    new double[]{x+CELL_SIZE/2, x+PADDING, x+CELL_SIZE-PADDING},
                    new double[]{y+PADDING, y+CELL_SIZE-PADDING, y+CELL_SIZE-PADDING},
                    3
                );
            }
            case SPEED_LIMIT -> {
                gc.setFill(Color.WHITE);
                gc.fillOval(x+PADDING, y+PADDING, CELL_SIZE-2*PADDING, CELL_SIZE-2*PADDING);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeOval(x+PADDING, y+PADDING, CELL_SIZE-2*PADDING, CELL_SIZE-2*PADDING);
                gc.setFill(Color.BLACK);
                gc.fillText("30", x+CELL_SIZE/3, y+CELL_SIZE/2+4);
            }
            case ONE_WAY -> {
                gc.setFill(Color.BLUE);
                gc.fillRect(x+PADDING, y+PADDING, CELL_SIZE-2*PADDING, CELL_SIZE-2*PADDING);
                gc.setFill(Color.WHITE);
                gc.fillPolygon(
                    new double[]{x+CELL_SIZE/2, x+CELL_SIZE/4, x+3*CELL_SIZE/4},
                    new double[]{y+PADDING+2, y+CELL_SIZE-PADDING-2, y+CELL_SIZE-PADDING-2},
                    3
                );
                gc.fillRect(x+CELL_SIZE/2-2, y+CELL_SIZE/3, 4, CELL_SIZE/3);
            }
            case NO_ENTRY -> {
                gc.setFill(Color.WHITE);
                gc.fillOval(x+PADDING, y+PADDING, CELL_SIZE-2*PADDING, CELL_SIZE-2*PADDING);
                gc.setFill(Color.RED);
                gc.fillRect(x+PADDING+2, y+CELL_SIZE/2-3, CELL_SIZE-2*PADDING-4, 6);
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeOval(x+PADDING, y+PADDING, CELL_SIZE-2*PADDING, CELL_SIZE-2*PADDING);
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
