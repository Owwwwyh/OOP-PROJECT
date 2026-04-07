import java.util.*;

public abstract class User {
    protected String userId;
    protected int greenPoints;
    protected List<Rental> rentalHistory = new ArrayList<>();
    protected String password;

    public User(String userId, String password) {
        this.userId = userId;
        this.password = password;
        this.greenPoints = 0;
    }

    public User(String userId) {
        this(userId, "");
    }

    public String getUserId() {
        return userId;
    }

    public int getGreenPoints() {
        return greenPoints;
    }

    public void addGreenPoints(int points) {
        this.greenPoints += points;
    }

    public List<Rental> getRentalHistory() {
        return rentalHistory;
    }

    public void addRental(Rental rental) {
        rentalHistory.add(rental);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public abstract double calculateFare(int duration);
}