public interface Rentable {
    void startRental(User user) throws Exception;
    void endRental(User user) throws Exception;
}