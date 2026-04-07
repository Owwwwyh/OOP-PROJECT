public class Student extends User {
    private static final double RATE_PER_MIN = 0.10;

    public Student(String userId, String password) {
        super(userId, password);
    }

    @Override
    public double calculateFare(int duration) {
        if (duration <= 0) {
            return RATE_PER_MIN;
        }
        return Math.max(RATE_PER_MIN, Math.ceil(duration) * RATE_PER_MIN);
    }

    public static double getHourlyRate() {
        return RATE_PER_MIN * 60; 
    }
}