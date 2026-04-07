import java.io.*;
import java.util.*;
import java.time.LocalDateTime;

public class FileHandler {
    private static final String USERS_FILE = "users.txt";
    private static final String RENTALS_FILE = "rentals.txt";
    private static final String DELIMITER = ";;";

    public static void saveUsers(Map<String, User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                String type = (user instanceof Student) ? "Student" : "Staff";
                writer.println(user.getUserId() + DELIMITER + 
                             type + DELIMITER + 
                             user.getGreenPoints());
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public static Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        File file = new File(USERS_FILE);
        
        if (!file.exists()) {
            return users;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(DELIMITER);
                if (parts.length == 4) {
                    String userId = parts[0];
                    String password = parts[3];
                    String type = parts[1];
                    int points = Integer.parseInt(parts[2]);
                    
                    User user = type.equals("Student") ? new Student(userId, password) : new Staff(userId, password);
                    user.addGreenPoints(points);
                    users.put(userId, user);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public static void saveRentals(List<Rental> rentals) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RENTALS_FILE))) {
            for (Rental rental : rentals) {
                writer.println(rental.getUser().getUserId() + DELIMITER + 
                              rental.getBike().getBikeId() + DELIMITER + 
                              rental.getStartTime() + DELIMITER + 
                              (rental.getEndTime() != null ? rental.getEndTime() : "null"));
            }
        } catch (IOException e) {
            System.out.println("Error saving rentals: " + e.getMessage());
        }
    }

    public static List<Rental> loadRentals(Map<String, User> users, BikeStation station) {
        List<Rental> rentals = new ArrayList<>();
        File file = new File(RENTALS_FILE);
        
        if (!file.exists()) {
            return rentals;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(DELIMITER);
                if (parts.length == 4) {
                    String userId = parts[0];
                    String bikeId = parts[1];
                    LocalDateTime startTime = LocalDateTime.parse(parts[2]);
                    LocalDateTime endTime = parts[3].equals("null") ? null : LocalDateTime.parse(parts[3]);
                    
                    User user = users.get(userId);
                    Bike bike = station.getBikeById(bikeId);
                    
                    if (user != null && bike != null) {
                        Rental rental = new Rental(bike, user);
                        rental.setStartTime(startTime);
                        if (endTime != null) {
                            rental.setEndTime(endTime);
                        }
                        rentals.add(rental);
                        user.addRental(rental);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading rentals: " + e.getMessage());
        }
        return rentals;
    }
}