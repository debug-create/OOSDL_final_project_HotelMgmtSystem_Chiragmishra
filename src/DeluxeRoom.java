import java.io.Serializable;

public class DeluxeRoom extends Room implements Serializable {

    private static final long serialVersionUID = 2L;

    private String amenities;

    public DeluxeRoom(int roomNumber, double pricePerDay, String amenities) {
        super(roomNumber, "Deluxe", pricePerDay);
        this.amenities = amenities;
    }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    // Extends parent description by appending amenities (polymorphism)
    @Override
    public String getRoomDescription() {
        return getRoomType() + " Room #" + getRoomNumber()
             + " | Rs." + String.format("%.0f", getPricePerDay()) + "/day"
             + " | " + (isAvailable() ? "Available" : "Occupied")
             + " | Amenities: " + amenities;
    }
}
