import java.util.*;

public class Admin {
    private List<User> users;
    private List<Rental> allRentals;
    private static final String LINE = "─".repeat(80);

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public Admin(List<User> users, List<Rental> allRentals) {
        this.users = users;
        this.allRentals = allRentals;
    }

    public void viewAllUsers() {
        clearScreen();
        System.out.println("\n" + ConsoleColors.PURPLE + "═".repeat(52) + ConsoleColors.YELLOW);
        System.out.printf("%-15s %-10s %-10s %-15s\n", "User ID", "Type", "Points", "Active Rental");
        System.out.println(ConsoleColors.PURPLE + "═".repeat(52));
        
        for (User u : users) {
            String activeRental = u.getRentalHistory().stream()
                .filter(r -> r.getEndTime() == null)
                .findFirst()
                .map(r -> r.getBike().getBikeId())
                .orElse("None");
                
            System.out.printf("%-15s %-10s %-10d %-15s\n", 
                u.getUserId(), 
                u.getClass().getSimpleName(), 
                u.getGreenPoints(),
                activeRental);
        }
        System.out.println("═".repeat(52));
        System.out.println("Total Users: " + ConsoleColors.YELLOW + users.size() + ConsoleColors.RESET);
    }

    public void viewAllRentals() {
        clearScreen();
        if (allRentals.isEmpty()) {
            System.out.println(ConsoleColors.RED + "\nNo rentals recorded yet.");
            return;
        }

        System.out.println("\n" + ConsoleColors.PURPLE + "═".repeat(102)+ ConsoleColors.YELLOW);
        System.out.printf("%-10s %-15s %-15s %-20s %-20s %-10s %-10s\n", 
                         "Rental #", "User ID", "Bike ID", "Start Time", "End Time", "Duration", "Fare");
        System.out.println(ConsoleColors.PURPLE + "═".repeat(102));
        
        for (int i = 0; i < allRentals.size(); i++) {
            Rental r = allRentals.get(i);
            System.out.printf("%-10d %-15s %-15s %-20s %-20s %-10d RM%-9.2f\n",
                            i+1,
                            r.getUser().getUserId(),
                            r.getBike().getBikeId(),
                            r.getStartTime().format(BikeRentalSystem.dtf),
                            r.getEndTime() != null ? r.getEndTime().format(BikeRentalSystem.dtf) : "Ongoing",
                            r.getDurationInMinutes(),
                            r.getFare());
        }
        System.out.println("═".repeat(102));
        System.out.println("Total Rentals: " + ConsoleColors.YELLOW + allRentals.size() + ConsoleColors.RESET);
    }

    public void viewUsageStats() {
        clearScreen();
        System.out.println("\n" + ConsoleColors.PURPLE + "═".repeat(50) + ConsoleColors.YELLOW);
        System.out.println(" ".repeat(15) + " SYSTEM STATISTICS");
        System.out.println(ConsoleColors.PURPLE + "═".repeat(50));
        System.out.println("Total Users: " + users.size());
        
        long studentCount = users.stream()
            .filter(u -> u instanceof Student)
            .count();
        System.out.println("· Students: " + studentCount);
        System.out.println("· Staff: " + (users.size() - studentCount));
        
        System.out.println("\nTotal Rentals: " + allRentals.size());
        
        if (!allRentals.isEmpty()) {
            double totalRevenue = allRentals.stream()
                .mapToDouble(Rental::getFare)
                .sum();
            System.out.println("Total Revenue: RM" + String.format("%.2f", totalRevenue));
            
            double avgDuration = allRentals.stream()
                .mapToLong(Rental::getDurationInMinutes)
                .average()
                .orElse(0);
            System.out.println("Average Rental Duration: " + String.format("%.1f", avgDuration) + " minutes");
        }
        System.out.println("═".repeat(50));
    }
}