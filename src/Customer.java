import java.io.Serializable;

public class Customer implements Serializable {

    private static final long serialVersionUID = 3L;

    private String customerId;
    private String name;
    private String contactNumber;
    private int    allocatedRoomNumber; // -1 means no room assigned

    public Customer(String customerId, String name, String contactNumber) {
        this.customerId          = customerId;
        this.name                = name;
        this.contactNumber       = contactNumber;
        this.allocatedRoomNumber = -1;
    }

    public String  getCustomerId()          { return customerId;          }
    public String  getName()                { return name;                }
    public String  getContactNumber()       { return contactNumber;       }
    public int     getAllocatedRoomNumber()  { return allocatedRoomNumber; }

    public void setName(String name)                             { this.name = name; }
    public void setContactNumber(String contactNumber)           { this.contactNumber = contactNumber; }
    public void setAllocatedRoomNumber(int allocatedRoomNumber)  { this.allocatedRoomNumber = allocatedRoomNumber; }

    @Override
    public String toString() {
        return "[" + customerId + "] " + name + " | " + contactNumber
             + " | Room: " + (allocatedRoomNumber == -1 ? "None" : allocatedRoomNumber);
    }
}
