import java.io.Serializable;

// Abstract base class for all room types; implements Bookable interface
public abstract class Room implements Bookable, Serializable {

    private static final long serialVersionUID = 1L;

    private int     roomNumber;
    private String  roomType;
    private double  pricePerDay;
    private boolean available;

    public Room(int roomNumber, String roomType, double pricePerDay) {
        this.roomNumber  = roomNumber;
        this.roomType    = roomType;
        this.pricePerDay = pricePerDay;
        this.available   = true;
    }

    public int     getRoomNumber()  { return roomNumber;  }
    public String  getRoomType()    { return roomType;    }
    public double  getPricePerDay() { return pricePerDay; }
    public boolean isAvailable()    { return available;   }

    public void setRoomType(String roomType)       { this.roomType    = roomType;    }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public void setAvailable(boolean available)    { this.available   = available;   }

    // Subclasses must provide their own room description (polymorphism)
    public abstract String getRoomDescription();

    @Override
    public String toString() {
        return getRoomDescription();
    }
}
