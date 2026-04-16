// Interface representing any entity that can be booked in the hotel
public interface Bookable {
    String getRoomDescription();
    boolean isAvailable();
    double getPricePerDay();
}
