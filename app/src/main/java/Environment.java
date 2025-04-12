public class Environment {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    
    public Environment(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = new Cell(CellType.ROAD);
            }
        }
    }
    
    public void addWall(int x, int y) {
        if (isValidPosition(x, y)) {
            grid[y][x].setType(CellType.WALL);
        }
    }
    
    public void addTrafficSign(int x, int y, TrafficSignType signType) {
        if (isValidPosition(x, y)) {
            grid[y][x].addTrafficElement(new TrafficSign(signType));
        }
    }
    
    public void addTrafficLight(int x, int y, TrafficLightState initialState) {
        if (isValidPosition(x, y)) {
            grid[y][x].addTrafficElement(new TrafficLight(initialState));
        }
    }
    
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    public Cell getCell(int x, int y) {
        if (isValidPosition(x, y)) {
            return grid[y][x];
        }
        return null;
    }
    
    public boolean isWall(int x, int y) {
        if (isValidPosition(x, y)) {
            return grid[y][x].getType() == CellType.WALL;
        }
        return false;
    }
    
    public void createSimpleRoadNetwork() {
        // something light, simple
        for (int x = 0; x < width; x++) {
            addWall(x, 0);
            addWall(x, height - 1);
        }
        for (int y = 0; y < height; y++) {
            addWall(0, y);
            addWall(width - 1, y);
        }
        
        int midX = width / 2;
        int midY = height / 2;
        
        addTrafficLight(midX - 3, midY, TrafficLightState.RED);
        addTrafficLight(midX + 3, midY, TrafficLightState.GREEN);
        addTrafficSign(midX, midY - 3, TrafficSignType.STOP);
        addTrafficSign(midX, midY + 3, TrafficSignType.YIELD);
        
        for (int i = 0; i < 5; i++) {
            addWall(midX - 10, midY + i - 2);
            addWall(midX + 10, midY + i - 2);
        }
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void display() {
        System.out.println("Environment (" + width + "x" + height + "):");
        
        System.out.print("+");
        for (int x = 0; x < width; x++) {
            System.out.print("-");
        }
        System.out.println("+");
        
        for (int y = 0; y < height; y++) {
            System.out.print("|");
            for (int x = 0; x < width; x++) {
                System.out.print(grid[y][x].getDisplayChar());
            }
            System.out.println("|");
        }
        
        System.out.print("+");
        for (int x = 0; x < width; x++) {
            System.out.print("-");
        }
        System.out.println("+");
    }
    
    public void printTrafficElementDetails() {
        System.out.println("Traffic Elements in the Environment:");
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = grid[y][x];
                if (cell.hasTrafficElements()) {
                    System.out.println("Position (" + x + "," + y + "):");
                    for (TrafficElement element : cell.getTrafficElements()) {
                        System.out.println("  - " + element.getDescription());
                    }
                }
            }
        }
    }
}