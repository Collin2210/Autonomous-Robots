public class EnvironmentTest {
    public static void main(String[] args) {

        Environment env = new Environment(30, 20);
        
        env.createSimpleRoadNetwork();
        
        env.addTrafficSign(10, 5, TrafficSignType.ONE_WAY);
        env.addTrafficLight(5, 10, TrafficLightState.GREEN);
        
        env.display();
        
        env.printTrafficElementDetails();
        

        // changing lights
        for (int y = 0; y < env.getHeight(); y++) {
            for (int x = 0; x < env.getWidth(); x++) {
                Cell cell = env.getCell(x, y);
                if (cell != null && cell.hasTrafficElements()) {
                    for (TrafficElement element : cell.getTrafficElements()) {
                        if (element instanceof TrafficLight trafficLight) {
                            trafficLight.cycleState();
                        }
                    }
                }
            }
        }
        
        System.out.println("\nAfter cycling traffic lights:");
        env.display();
    }
}