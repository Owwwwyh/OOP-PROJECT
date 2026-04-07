import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BikeRentalSystem {
    private static final Scanner sc = new Scanner(System.in);
    private static final Console console = System.console();
    private static Map<String, User> users; 
    private static List<Rental> allRentals; 

    private static final BikeStation station = new BikeStation("UTM South Gate");
    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String USERS_FILE = "users.txt";
    private static final String RENTALS_FILE = "rentals.txt";
    private static final String DELIMITER = ";;";

    public static void main(String[] args) {

       File f = new File("Bike.txt");
    if (!f.exists()) return;
    try (Scanner file = new Scanner(f)) {
        while (file.hasNextLine()) {
            String bikeId = file.nextLine().trim();
            if (!bikeId.isEmpty() && station.getBikeById(bikeId) == null) {
                station.addBike(new Bike(bikeId));
                System.out.println(bikeId);
            }
        }
    } catch (Exception e) {
        System.out.println(ConsoleColors.RED + "Error loading bikes: " + e.getMessage() + ConsoleColors.RESET);
    }
        users = loadUsers();
        allRentals = loadRentals();

        syncBikeAvailability();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveUsers();
            saveRentals();
        }));

        displayWelcomeMessage();

        while (true) {
            displayMainMenu();
            int choice = getIntInput("Please select an option (1-3): ", 1, 3);
            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    clearScreen();
                    System.out
                            .println(ConsoleColors.YELLOW + "\nThank you for using UTM eBikeGo!" + ConsoleColors.RESET);
                    saveUsers();
                    saveRentals();
                    System.exit(0);
                }
            }
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    private static Map<String, User> loadUsers() {
        Map<String, User> loaded = new HashMap<>();
        File f = new File(USERS_FILE);
        if (!f.exists())
            return loaded; 

        try (Scanner file = new Scanner(f)) {
            while (file.hasNextLine()) {
                String[] parts = file.nextLine().split(DELIMITER);
                if (parts.length != 4)
                    continue; 

                String id = parts[0];
                String type = parts[1];
                int points = Integer.parseInt(parts[2]);
                String password = parts[3];

                User u = type.equals("Student") ? new Student(id, password) : new Staff(id, password);
                u.addGreenPoints(points);
                loaded.put(id, u);
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Error loading users: " + e.getMessage() + ConsoleColors.RESET);
        }
        return loaded;
    }

    private static List<Rental> loadRentals() {
        List<Rental> loaded = new ArrayList<>();
        File f = new File(RENTALS_FILE);
        if (!f.exists())
            return loaded;

        try (Scanner file = new Scanner(f)) {
            while (file.hasNextLine()) {
                String[] p = file.nextLine().split(DELIMITER);
                if (p.length != 4)
                    continue;

                String userId = p[0];
                String bikeId = p[1];
                LocalDateTime start = LocalDateTime.parse(p[2]); 
                LocalDateTime end = p[3].equals("null") ? null : LocalDateTime.parse(p[3]);

                User u = users.get(userId);
                Bike b = station.getBikeById(bikeId);
                if (u == null || b == null)
                    continue; 

                Rental r = new Rental(b, u);
                r.setStartTime(start);
                r.setEndTime(end);

                u.addRental(r);
                loaded.add(r);
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Error loading rentals: " + e.getMessage() + ConsoleColors.RESET);
        }
        return loaded;
    }

    private static void syncBikeAvailability() {
        for (Bike bike : station.getAllBikes()) {
            bike.setAvailable(true);
        }
        for (Rental rental : allRentals) {
            if (rental.getEndTime() == null) {
                rental.getBike().setAvailable(false);
            }
        }
    }

    private static void saveUsers() {
        try (PrintWriter out = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users.values()) {
                String type = (u instanceof Student) ? "Student" : "Staff";
                out.println(u.getUserId() + DELIMITER + type + DELIMITER + u.getGreenPoints() + DELIMITER
                        + u.getPassword());
            }
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Error saving users: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    private static void saveRentals() {
        try (PrintWriter out = new PrintWriter(new FileWriter(RENTALS_FILE))) {
            for (Rental r : allRentals) {
                out.println(r.getUser().getUserId() + DELIMITER +
                        r.getBike().getBikeId() + DELIMITER +
                        r.getStartTime() + DELIMITER +
                        (r.getEndTime() != null ? r.getEndTime() : "null"));
            }
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Error saving rentals: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    private static void saveBikes() {
        try (PrintWriter out = new PrintWriter(new FileWriter("Bike.txt"))) {
            for (Bike bike : station.getAllBikes()) {
                out.println(bike.getBikeId());
            }
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Error saving bikes: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    private static void displayWelcomeMessage() {
        clearScreen();
        System.out.println("\n" + ConsoleColors.GREEN + "=".repeat(60));
        System.out.println(
                " ".repeat(15) + ConsoleColors.YELLOW + " WELCOME TO UTM eBikeGo SYSTEM " + ConsoleColors.GREEN);
        System.out.println(" ".repeat(10) + ConsoleColors.YELLOW + "Sustainable Bike Rental for Low Carbon Campus"
                + ConsoleColors.GREEN);
        System.out.println(ConsoleColors.GREEN + "=".repeat(60));
    }

    private static void displayMainMenu() {
        System.out.println("\n" + ConsoleColors.CYAN + "╔" + "═".repeat(50) + "╗");
        System.out.println(
                "║" + " ".repeat(20) + ConsoleColors.YELLOW + "MAIN MENU" + ConsoleColors.CYAN + " ".repeat(21) + "║");
        System.out.println("╠" + "═".repeat(50) + "╣");
        System.out.println("║  1. Register New User" + " ".repeat(28) + "║");
        System.out.println("║  2. Login" + " ".repeat(40) + "║");
        System.out.println("║  3. Exit System" + " ".repeat(34) + "║");
        System.out.println("╚" + "═".repeat(50) + "╝" + ConsoleColors.RESET);
    }

    private static String getInput(String prompt, boolean mask) {
        if (console != null) {
            if (mask) {
                char[] passwordChars = console.readPassword(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
                String result = new String(passwordChars);
                Arrays.fill(passwordChars, ' '); 
                return result;
            } else {
                return console.readLine(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
            }
        } else {
            System.out.print(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
            return sc.nextLine();
        }
    }

    private static String getNonEmptyInput(String prompt, boolean mask) {
        String input;
        do {
            input = getInput(prompt, mask);
            if (input == null)
                input = ""; 
            input = input.trim();
            if (input.isEmpty()) {
                System.out.println(ConsoleColors.RED + "  Input cannot be empty!" + ConsoleColors.RESET);
            }
        } while (input.isEmpty());
        return input;
    }
    private static int getIntInput(int min, int max) {
        return getIntInput("", min, max);
    }

    private static int getIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                String input = getInput(prompt, false);
                if (input == null || input.trim().isEmpty()) {
                    System.out.printf(
                            ConsoleColors.RED + "Please enter a number between %d and %d: " + ConsoleColors.RESET, min,
                            max);
                    continue;
                }
                int val = Integer.parseInt(input.trim());
                if (val >= min && val <= max)
                    return val;
            } catch (NumberFormatException ignored) {
            }
            System.out.printf(ConsoleColors.RED + "Please enter a number between %d and %d: " + ConsoleColors.RESET,
                    min, max);
        }
    }

    private static void register() {
        String[] staffKey = {"StaffKey001","StaffKey002","StaffKey003"};
        clearScreen();
        System.out.println("\n" + ConsoleColors.CYAN + "═".repeat(50) + ConsoleColors.YELLOW);
        System.out.println(" ".repeat(15) + "USER REGISTRATION" + ConsoleColors.CYAN);
        System.out.println("═".repeat(50) + ConsoleColors.RESET);

        String id = getNonEmptyInput("Enter your UTM ID: ", false);
        if (users.containsKey(id)) {
            System.out.println(ConsoleColors.RED + "Error: This ID is already registered!" + ConsoleColors.RESET);
            return;
        }

        String password = getNonEmptyInput("Set your password: ", true);

        String type = getInput("Are you a Student or Staff? (S/T): ", false);
        if (type != null)
            type = type.trim().toUpperCase();

        User user;
        if ("S".equals(type)) {
            user = new Student(id, password);
        } 
        else if ("T".equals(type)) {
            String key = getNonEmptyInput("Enter key for staff registration: ", true);
            boolean validKey = false;
            for (String k : staffKey) {
                if (k.equals(key)) {
                    validKey = true;
                    break;
                }
            }
            if (!validKey) {
                System.out.println(ConsoleColors.RED + "Error: Invalid staff registration key!" + ConsoleColors.RESET);
                return;
            }
            user = new Staff(id, password);
        } else {
            System.out.println(
                    ConsoleColors.RED + "Error: Invalid user type! Please enter 'S' or 'T'." + ConsoleColors.RESET);
            return;
        }

        users.put(id, user);
        saveUsers();
        System.out.println(ConsoleColors.GREEN + "\nRegistration successful! Welcome " + id + ConsoleColors.RESET);
        System.out.println("You are registered as: " + user.getClass().getSimpleName());
    }

    private static void login() {
        clearScreen();
        System.out.println("\n" + ConsoleColors.CYAN + "═".repeat(50));
        System.out.println(" ".repeat(20) + ConsoleColors.YELLOW + "USER LOGIN" + ConsoleColors.CYAN);
        System.out.println("═".repeat(50));

        String id = getNonEmptyInput("Enter your UTM ID: ", false);
        User user = users.get(id);
        if (user == null) {
            System.out
                    .println(ConsoleColors.RED + "Error: User not found. Please register first." + ConsoleColors.RESET);
            return;
        }

        final int MAX_ATTEMPTS = 3;
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            String password = getNonEmptyInput("Enter your password: ", true);

            if (user.getPassword().equals(password)) {
                System.out
                        .println(ConsoleColors.GREEN + "\nLogin successful! Welcome back " + id + ConsoleColors.RESET);
                if (user instanceof Student) {
                    userMenu(user);
                } else if (user instanceof Staff) {
                    staffMenu(user);
                }
                return;
            } else {
                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;
                if (remaining > 0) {
                    System.out.println(ConsoleColors.RED + "Incorrect password. You have " + remaining
                            + " attempt(s) left." + ConsoleColors.RESET);
                }
            }
        }

        System.out.println(ConsoleColors.RED + "Error: Too many failed login attempts. Returning to main menu."
                + ConsoleColors.RESET);

    }

    private static void userMenu(User user) {
        while (true) {
            Rental activeRental = null;
            for (Rental r : user.getRentalHistory()) {
                if (r.getEndTime() == null) {
                    activeRental = r;
                    break;
                }
            }

            System.out.println("\n" + ConsoleColors.CYAN + "╔" + "═".repeat(50) + "╗");
            System.out.println("║" + " ".repeat(20) + ConsoleColors.YELLOW + "USER MENU" + ConsoleColors.CYAN
                    + " ".repeat(21) + "║");
            System.out.println("╠" + "═".repeat(50) + "╣");

            if (activeRental != null) {
                System.out.println("║" + ConsoleColors.GREEN + " Currently Renting: Bike " +
                        activeRental.getBike().getBikeId() + " ".repeat(21) + ConsoleColors.CYAN + "║");
                System.out.println("║" + ConsoleColors.GREEN + " Started: " +
                        activeRental.getStartTime().format(dtf) + " ".repeat(24) + ConsoleColors.CYAN + "║");
                System.out.println("╠" + "═".repeat(50) + "╣");
            }

            System.out.println("║  1. View Available Bikes" + " ".repeat(25) + "║");
            System.out.println("║  2. Start Rental" + " ".repeat(33) + "║");
            System.out.println("║  3. End Rental" + " ".repeat(35) + "║");
            System.out.println("║  4. View Rental History" + " ".repeat(26) + "║");
            System.out.println("║  5. View Green Points" + " ".repeat(28) + "║");
            System.out.println("║  6. Logout" + " ".repeat(39) + "║");
            System.out.println("╚" + "═".repeat(50) + "╝" + ConsoleColors.RESET);

            int choice = getIntInput("Enter your choice (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> {station.showAvailableBikes();getInput("Press Enter to continue...", false);clearScreen();}
                case 2 -> {startRental(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 3 -> {endRental(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 4 -> {viewRentalHistory(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 5 -> {viewGreenPoints(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 6 -> {
                    clearScreen();
                    return;
                }
            }
        }
    }

    private static void staffMenu(User user) {
        while (true) {  
            Rental activeRental = null;
            for (Rental r : user.getRentalHistory()) {
                if (r.getEndTime() == null) {
                    activeRental = r;
                    break;
                }
            }

            System.out.println("\n" + ConsoleColors.CYAN + "╔" + "═".repeat(50) + "╗");
            System.out.println("║" + " ".repeat(20) + ConsoleColors.YELLOW + "STAFF MENU" + ConsoleColors.CYAN
                    + " ".repeat(20) + "║");
            System.out.println("╠" + "═".repeat(50) + "╣");
            if (activeRental != null) {
                System.out.println("║" + ConsoleColors.GREEN + " Currently Renting: Bike " +
                        activeRental.getBike().getBikeId() + " ".repeat(21) + ConsoleColors.CYAN + "║");
                System.out.println("║" + ConsoleColors.GREEN + " Started: " +
                        activeRental.getStartTime().format(dtf) + " ".repeat(24) + ConsoleColors.CYAN + "║");
                System.out.println("╠" + "═".repeat(50) + "╣");
            }

            System.out.println("║  1. Manage and View all Bikes" + " ".repeat(20) + "║");
            System.out.println("║  2. Start Rental" + " ".repeat(33) + "║");
            System.out.println("║  3. End Rental" + " ".repeat(35) + "║");
            System.out.println("║  4. View Rental History" + " ".repeat(26) + "║");
            System.out.println("║  5. View Green Points" + " ".repeat(28) + "║");
            System.out.println("║  6. Admin Panel" + " ".repeat(34) + "║");
            System.out.println("║  7. Logout" + " ".repeat(39) + "║");
            System.out.println("╚" + "═".repeat(50) + "╝" + ConsoleColors.RESET);

            int choice = getIntInput("Enter your choice (1-7): ", 1, 7);
            switch (choice) {
                case 1 -> manageBikes();
                case 2 -> {startRental(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 3 -> {endRental(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 4 -> {viewRentalHistory(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 5 -> {viewGreenPoints(user);getInput("Press Enter to continue...", false);clearScreen();}
                case 6 -> adminPanel();
                case 7 -> {
                    clearScreen();
                    return;
                }
            }
        }
    }

private static void manageBikes() {
    while (true) {
        clearScreen();
        System.out.println("\n" + ConsoleColors.CYAN + "═".repeat(50));
        System.out.println(" ".repeat(18) + ConsoleColors.YELLOW + "BIKE MANAGEMENT" + ConsoleColors.CYAN);
        System.out.println("═".repeat(50) + ConsoleColors.RESET);

        System.out.println("Available Bikes:");
        station.showAllBikes();

        System.out.println(ConsoleColors.GREEN + "\nOptions:");
        System.out.println("1. Add Bike");
        System.out.println("2. Delete Bike");
        System.out.println("3. Quit to Staff Menu" + ConsoleColors.RESET);

        int choice = getIntInput("Enter your choice (1-3): ", 1, 3);
        switch (choice) {
            case 1 -> {
                String newBikeId = getNonEmptyInput("Enter new Bike ID: ", false).toUpperCase();
                if (station.getBikeById(newBikeId) != null) {
                    System.out.println(ConsoleColors.RED + "Bike ID already exists!" + ConsoleColors.RESET);
                } else {
                    station.addBike(new Bike(newBikeId));
                    saveBikes();
                    System.out.println(ConsoleColors.GREEN + "Bike added successfully!" + ConsoleColors.RESET);
                }
            }
            case 2 -> {
                String delBikeId = getNonEmptyInput("Enter Bike ID to delete: ", false).toUpperCase();
                Bike bike = station.getBikeById(delBikeId);
                if (bike == null) {
                    System.out.println(ConsoleColors.RED + "Bike not found!" + ConsoleColors.RESET);
                } else if (!bike.isAvailable()) {
                    System.out.println(ConsoleColors.RED + "Cannot delete: Bike is currently rented!" + ConsoleColors.RESET);
                } else {
                    station.removeBike(delBikeId);
                    saveBikes();
                    System.out.println(ConsoleColors.GREEN + "Bike deleted successfully!" + ConsoleColors.RESET);
                }
            }
            case 3 -> {
                clearScreen();
                return;
            }
        }

        getInput("Press Enter to continue...", false);

    }
}
    private static void startRental(User user) {
        clearScreen();
        try {
            for (Rental rental : user.getRentalHistory()) {
                if (rental.getEndTime() == null) {
                    System.out.println(
                            ConsoleColors.RED + "Error: You already have an active rental!" + ConsoleColors.RESET);
                    System.out.println(ConsoleColors.CYAN + "Please end your current rental before starting a new one."
                            + ConsoleColors.RESET);
                    return;
                }
            }

            System.out.println("\nSTARTING NEW RENTAL");

            station.showAvailableBikes();

            double ratePerMin = user instanceof Student ? Student.getHourlyRate() / 60 : Staff.getHourlyRate() / 60;
            System.out.println("\n" + ConsoleColors.CYAN + "Fare Rate: " + ConsoleColors.YELLOW + "RM"
                    + String.format("%.2f", ratePerMin) + " " + ConsoleColors.CYAN + "per minute");
            System.out
                    .println("Note: Minimum fare is " + ConsoleColors.YELLOW + "RM" + String.format("%.2f", ratePerMin)
                            + ConsoleColors.CYAN + " (starting price)" + ConsoleColors.RESET);

            String bikeId = getNonEmptyInput("Enter bike ID to rent: ", false).toUpperCase();
            Bike bike = station.getBikeById(bikeId);

            if (bike == null) {
                System.out.println(ConsoleColors.RED + "Error: Bike not found!" + ConsoleColors.RESET);
                return;
            }

            if (!bike.isAvailable()) {
                System.out.println(ConsoleColors.RED + "Error: Bike is not available!" + ConsoleColors.RESET);
                return;
            }

            bike.startRental(user);
            Rental rental = new Rental(bike, user);
            user.addRental(rental);
            allRentals.add(rental); 
            saveRentals(); 

            System.out.println(ConsoleColors.GREEN + "\nRental started successfully!" + ConsoleColors.RESET);
            System.out.println(
                    ConsoleColors.CYAN + "Bike ID   : " + ConsoleColors.YELLOW + bike.getBikeId() + ConsoleColors.CYAN);
            System.out.println(
                    ConsoleColors.CYAN + "User ID   : " + ConsoleColors.YELLOW + user.getUserId() + ConsoleColors.CYAN);
            System.out.println(ConsoleColors.CYAN + "Start Time: " + ConsoleColors.YELLOW
                    + rental.getStartTime().format(dtf) + ConsoleColors.CYAN);
        } catch (BikeNotAvailableException e) {
            System.out.println(ConsoleColors.RED + "Error: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    private static void endRental(User user) {
        clearScreen();
        List<Rental> history = user.getRentalHistory();
        if (history.isEmpty()) {
            System.out.println(ConsoleColors.RED + "Error: No rental history found!" + ConsoleColors.RESET);
            return;
        }

        Rental activeRental = null;
        for (Rental r : history) {
            if (r.getEndTime() == null) {
                activeRental = r;
                break;
            }
        }

        if (activeRental == null) {
            System.out.println(ConsoleColors.RED + "Error: No active rental found!" + ConsoleColors.RESET);
            return;
        }

        System.out.println("\n" + ConsoleColors.CYAN + "╔" + "═".repeat(50) + "╗");
        System.out.println("║" + " ".repeat(17) + ConsoleColors.YELLOW + "CURRENT RENTAL" + ConsoleColors.CYAN
                + " ".repeat(19) + "║");
        System.out.println("╠" + "═".repeat(50) + "╣");
        System.out.println("║  Bike ID: " + activeRental.getBike().getBikeId() + " ".repeat(35) + "║");
        System.out.println("║  Started: " + activeRental.getStartTime().format(dtf) + " ".repeat(23) + "║");
        System.out.println("╚" + "═".repeat(50) + "╝" + ConsoleColors.RESET);

        String confirm = getNonEmptyInput("\nDo you want to end this rental? (Y/N): ", false).toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println(ConsoleColors.YELLOW + "\nReturning to menu..." + ConsoleColors.RESET);
            return;
        }

        try {
            activeRental.endRental(); 
            activeRental.getBike().endRental(user); 

            int durationMinutes = (int) activeRental.getDurationInMinutes();
            double fare = activeRental.getFare();
            int pointsEarned = durationMinutes / 5;
            user.addGreenPoints(pointsEarned);

            saveRentals(); 
            saveUsers();

            System.out.println(ConsoleColors.GREEN + "\nRental ended successfully!");
            System.out.println(
                    ConsoleColors.CYAN + "Bike ID  : " + ConsoleColors.YELLOW + activeRental.getBike().getBikeId());
            System.out.println(ConsoleColors.CYAN + "User ID  : " + ConsoleColors.YELLOW + user.getUserId());
            System.out
                    .println(ConsoleColors.CYAN + "Duration : " + ConsoleColors.YELLOW + durationMinutes + " minutes");
            System.out.println(
                    ConsoleColors.CYAN + "Fare     : " + ConsoleColors.YELLOW + "RM" + String.format("%.2f", fare));
            System.out.println(ConsoleColors.CYAN + "Points   : " + ConsoleColors.YELLOW + "+" + (pointsEarned + 10));
            System.out.println(ConsoleColors.CYAN + "Total GP : " + ConsoleColors.YELLOW + user.getGreenPoints());
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Error ending rental: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    private static void viewRentalHistory(User user) {
        clearScreen();
        List<Rental> history = user.getRentalHistory();
        if (history.isEmpty()) {
            System.out.println("\nNo rental history found.");
            return;
        }

        System.out.println("\n" + ConsoleColors.CYAN + "═".repeat(86) + ConsoleColors.YELLOW);
        System.out.printf("%-15s %-10s %-20s %-20s %-10s %-10s\n",
                "Rental ID", "Bike", "Start Time", "End Time", "Duration", "Fare");
        System.out.println(ConsoleColors.CYAN + "═".repeat(86));

        for (int i = 0; i < history.size(); i++) {
            Rental r = history.get(i);
            System.out.printf("%-15d %-10s %-20s %-20s %-10d RM%-9.2f\n",
                    i + 1,
                    r.getBike().getBikeId(),
                    r.getStartTime().format(dtf),
                    r.getEndTime() != null ? r.getEndTime().format(dtf) : "Ongoing",
                    r.getDurationInMinutes(),
                    r.getFare());
        }
        System.out.println(ConsoleColors.CYAN + "═".repeat(86) + ConsoleColors.RESET);
    }

    private static void viewGreenPoints(User user) {
        clearScreen();
        System.out.println("\n" + ConsoleColors.CYAN + "═".repeat(50));
        System.out.println(" ".repeat(15) + ConsoleColors.YELLOW + "GREEN POINTS SUMMARY" + ConsoleColors.CYAN);
        System.out.println("═".repeat(50));
        System.out.println(
                ConsoleColors.CYAN + "User ID      : " + ConsoleColors.YELLOW + user.getUserId() + ConsoleColors.CYAN);
        System.out.println(ConsoleColors.CYAN + "Total Points : " + ConsoleColors.YELLOW + user.getGreenPoints()
                + ConsoleColors.CYAN);
        System.out.println("═".repeat(50));
    }


    private static void adminPanel() {
        clearScreen();
        Admin admin = new Admin(new ArrayList<>(users.values()), allRentals);

        while (true) {
            System.out.println("\n" + ConsoleColors.PURPLE + "╔" + "═".repeat(50) + "╗");
            System.out.println("║" + " ".repeat(19) + ConsoleColors.YELLOW + "ADMIN PANEL" + ConsoleColors.PURPLE
                    + " ".repeat(20) + "║");
            System.out.println("╠" + "═".repeat(50) + "╣");
            System.out.println("║  1. View All Users" + " ".repeat(31) + "║");
            System.out.println("║  2. View All Rentals" + " ".repeat(29) + "║");
            System.out.println("║  3. View Usage Statistics" + " ".repeat(24) + "║");
            System.out.println("║  4. Return to Staff Menu" + " ".repeat(25) + "║");
            System.out.println("╚" + "═".repeat(50) + "╝" + ConsoleColors.RESET);

            int choice = getIntInput("Enter your choice (1-4): ", 1, 4);
            switch (choice) {
                case 1 -> {admin.viewAllUsers();getInput("Press Enter to continue...", false);clearScreen();}
                case 2 -> {admin.viewAllRentals();getInput("Press Enter to continue...", false);clearScreen();}
                case 3 -> {admin.viewUsageStats();getInput("Press Enter to continue...", false);clearScreen();}
                case 4 -> {
                    clearScreen();
                    return;
                }
            }
        }
    }

    private static void waitForEnter() {
    System.out.println(ConsoleColors.YELLOW + "\nPress Enter to continue..." + ConsoleColors.RESET);
    sc.nextLine();
}

}