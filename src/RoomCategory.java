// Enum for room categories with default base prices
public enum RoomCategory {
    SINGLE(1200),
    DOUBLE(2200),
    DELUXE(4500);

    private final double basePrice;

    RoomCategory(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }

    // Calculates total cost for a given number of nights
    public double calculateTotal(int nights) {
        return basePrice * nights;
    }
}
