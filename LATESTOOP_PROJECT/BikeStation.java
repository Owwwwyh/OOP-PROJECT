import java.util.*;

public class BikeStation {
    private String stationName;
    private ArrayList<Bike> bikes = new ArrayList<>();
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public BikeStation(String stationName) {
        this.stationName = stationName;
    }

    public void addBike(Bike bike) {
        for (int i = 0; i < bikes.size(); i++) {
            if (bike.getBikeId().compareTo(bikes.get(i).getBikeId()) < 0) {
                bikes.add(i, bike);
                return;
            }
        }
        bikes.add(bike);
    }

    public void removeBike(String bikeId) {
        bikes.removeIf(b -> b.getBikeId().equals(bikeId));
    }

    public Bike getAvailableBike() throws BikeNotAvailableException {
        for (Bike b : bikes) {
            if (b.isAvailable()) return b;
        }
        throw new BikeNotAvailableException("No bikes available at station " + stationName);
    }

    public void showAvailableBikes() {
        clearScreen();
        System.out.println("\n" + ConsoleColors.CYAN + "╔" + "═".repeat(50) + "╗");
        System.out.println("║" + " ".repeat(9) + ConsoleColors.YELLOW + "AVAILABLE BIKES - " + stationName + ConsoleColors.CYAN + " ".repeat(9) + "║");
        System.out.println("╠" + "═".repeat(50) + "╣");
        
        boolean hasAvailableBikes = false;
        for (Bike b : bikes) {
            if (b.isAvailable()) {
                hasAvailableBikes = true;
                System.out.println("║  · " + b.getBikeId() + " ".repeat(42) + "║");
            }
        }
        
        if (!hasAvailableBikes) {
            System.out.println("║  No bikes available at this time" + " ".repeat(17) + "║");
        }
        
        System.out.println("╚" + "═".repeat(50) + "╝" + ConsoleColors.RESET);
    }

public void showAllBikes() {
    clearScreen();
    System.out.println("\n" + ConsoleColors.CYAN + "╔" + "═".repeat(50) + "╗");
    System.out.println("║" + " ".repeat(12) + ConsoleColors.YELLOW + "ALL BIKES - " + stationName + ConsoleColors.CYAN + " ".repeat(12) + "║");
    System.out.println("╠" + "═".repeat(50) + "╣");

    if (bikes.isEmpty()) {
        System.out.println("║  No bikes at this station." + " ".repeat(29) + "║");
    } else {
        for (Bike b : bikes) {
            String status = b.isAvailable() ? "Available" : "Rented";
            System.out.println("║  · " + b.getBikeId() + " - " + status + " ".repeat(43 - b.getBikeId().length() - status.length()) + "║");
        }
    }

    System.out.println("╚" + "═".repeat(50) + "╝" + ConsoleColors.RESET);
}

    public Bike getBikeById(String bikeId) {
        for (Bike b : bikes) {
            if (b.getBikeId().equals(bikeId)) {
                return b;
            }
        }
        return null;
    }

    public List<Bike> getAllBikes() {
        return new ArrayList<>(bikes); 
    }
}