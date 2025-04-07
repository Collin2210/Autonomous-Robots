public class TrafficLight extends TrafficElement {
    private TrafficLightState state;
    
    public TrafficLight(TrafficLightState state) {
        this.state = state;
    }
    
    public TrafficLightState getState() {
        return state;
    }
    
    public void setState(TrafficLightState state) {
        this.state = state;
    }
    
    public void cycleState() {
        switch (state) {
            case RED -> state = TrafficLightState.GREEN;
            case GREEN -> state = TrafficLightState.YELLOW;
            case YELLOW -> state = TrafficLightState.RED;
        }
    }
    
    @Override
    public String getDescription() {
        return "Traffic Light: " + state;
    }
}