public class Bike implements Rentable {
    private String bikeId;
    private boolean isAvailable;

    public Bike(String bikeId) {
        this.bikeId = bikeId;
        this.isAvailable = true;
    }

    public String getBikeId() {
        return bikeId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    @Override
    public void startRental(User user) throws BikeNotAvailableException {
        if (!isAvailable) throw new BikeNotAvailableException("Bike " + bikeId + " is not available.");
        setAvailable(false);
    }

    @Override
    public void endRental(User user) {
        setAvailable(true);
    }
}