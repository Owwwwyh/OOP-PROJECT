import java.time.*;

public class Rental {
    private Bike bike;
    private User user;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Rental(Bike bike, User user) {
        this.bike = bike;
        this.user = user;
        this.startTime = LocalDateTime.now();
    }

    public void endRental() {
        if (this.endTime != null) {
            throw new IllegalStateException("Rental already ended");
        }
        this.endTime = LocalDateTime.now();

        user.addGreenPoints(10);
    }

    public long getDurationInMinutes() {
        if (endTime == null) {
            return Duration.between(startTime, LocalDateTime.now()).toMinutes();
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    public double getFare() {
        return user.calculateFare((int) getDurationInMinutes());
    }

    public Bike getBike() { return bike; }
    public User getUser() { return user; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    @Override
    public String toString() {
        return String.format(
            "Rental [User: %s, Bike: %s, Duration: %d mins, Fare: RM%.2f]",
            user.getUserId(),
            bike.getBikeId(),
            getDurationInMinutes(),
            getFare()
        );
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
