public class TrafficSign extends TrafficElement {
    private final TrafficSignType type;
    
    public TrafficSign(TrafficSignType type) {
        this.type = type;
    }
    
    public TrafficSignType getType() {
        return type;
    }
    
    @Override
    public String getDescription() {
        return "Traffic Sign: " + type;
    }
}