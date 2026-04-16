import java.io.Serializable;

// Concrete room for Single and Double types; extends abstract Room
public class StandardRoom extends Room implements Serializable {

    private static final long serialVersionUID = 4L;

    public StandardRoom(int roomNumber, String roomType, double pricePerDay) {
        super(roomNumber, roomType, pricePerDay);
    }

    // Provides description for Single/Double rooms (required by abstract parent)
    @Override
    public String getRoomDescription() {
        return getRoomType() + " Room #" + getRoomNumber()
             + " | Rs." + String.format("%.0f", getPricePerDay()) + "/day"
             + " | " + (isAvailable() ? "Available" : "Occupied");
    }
}
