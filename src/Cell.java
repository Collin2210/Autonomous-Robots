import java.util.ArrayList;
import java.util.List;

public class Cell {
    private CellType type;
    private final List<TrafficElement> trafficElements;
    
    public Cell(CellType type) {
        this.type = type;
        this.trafficElements = new ArrayList<>();
    }
    
    public CellType getType() {
        return type;
    }
    
    public void setType(CellType type) {
        this.type = type;
    }
    
    public void addTrafficElement(TrafficElement element) {
        trafficElements.add(element);
    }
    
    public boolean removeTrafficElement(TrafficElement element) {
        return trafficElements.remove(element);
    }
    
    public boolean hasTrafficElements() {
        return !trafficElements.isEmpty();
    }
    
    public List<TrafficElement> getTrafficElements() {
        return trafficElements;
    }
    
    public char getDisplayChar() {
        if (type == CellType.WALL) {
            return '#';
        }
        
        if (!trafficElements.isEmpty()) {
            TrafficElement element = trafficElements.get(0);
            switch (element) {
                case TrafficLight light -> {
                    switch (light.getState()) {
                        case RED -> {
                            return 'R';
                        }
                        case YELLOW -> {
                            return 'Y';
                        }
                        case GREEN -> {
                            return 'G';
                        }
                    }
                }
                case TrafficSign sign -> {
                    return switch (sign.getType()) {
                        case STOP -> 'S';
                        case YIELD -> 'y';
                        case SPEED_LIMIT -> 'L';
                        default -> 'T';
                    };
                }
                default -> {
                }
            }
        }
        
        return ' ';
    }
}