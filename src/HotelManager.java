import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class HotelManager {

    private static final String DATA_FILE = "hotel_data.ser";

    private ArrayList<Room>           rooms      = new ArrayList<>();
    private ArrayList<Customer>       customers  = new ArrayList<>();
    private HashMap<Integer, String>  bookingMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            oos.writeObject(rooms);
            oos.writeObject(customers);
            oos.writeObject(bookingMap);
            System.out.println("[HotelManager] Data saved to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("[HotelManager] Save failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("[HotelManager] No saved data found — loading sample data.");
            loadSampleData();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
            rooms      = (ArrayList<Room>)          ois.readObject();
            customers  = (ArrayList<Customer>)      ois.readObject();
            bookingMap = (HashMap<Integer, String>) ois.readObject();
            System.out.println("[HotelManager] Data loaded from " + DATA_FILE);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[HotelManager] Load failed: " + e.getMessage()
                + " — loading sample data instead.");
            loadSampleData();
        }
    }

    /** Returns false if room number already exists. */
    public boolean addRoom(Room room) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == room.getRoomNumber()) return false;
        }
        rooms.add(room);
        return true;
    }

    public ArrayList<Room> getAllRooms() { return rooms; }

    public ArrayList<Room> getAvailableRooms() {
        ArrayList<Room> available = new ArrayList<>();
        Iterator<Room> it = rooms.iterator(); // safe remove via iterator
        while (it.hasNext()) {
            Room r = it.next();
            if (r.isAvailable()) available.add(r);
        }
        return available;
    }

    public ArrayList<Room> getOccupiedRooms() {
        ArrayList<Room> occ = new ArrayList<>();
        for (Room r : rooms) {
            if (!r.isAvailable()) occ.add(r);
        }
        return occ;
    }

    public Room searchRoom(int roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) return r;
        }
        return null;
    }

    public ArrayList<Room> getRoomsSortedByPrice() {
        ArrayList<Room> sorted = new ArrayList<>(rooms);
        Collections.sort(sorted, Comparator.comparingDouble(Room::getPricePerDay));
        return sorted;
    }

    /** Returns false if customer ID already taken. */
    public boolean addCustomer(Customer customer) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(customer.getCustomerId())) return false;
        }
        customers.add(customer);
        return true;
    }

    public ArrayList<Customer> getAllCustomers() { return customers; }

    /** Also frees the customer's room if allocated. */
    public boolean removeCustomer(String customerId) {
        Iterator<Customer> it = customers.iterator(); // safe remove via iterator
        while (it.hasNext()) {
            Customer c = it.next();
            if (c.getCustomerId().equalsIgnoreCase(customerId)) {
                if (c.getAllocatedRoomNumber() != -1) checkoutRoom(c.getAllocatedRoomNumber());
                it.remove();
                return true;
            }
        }
        return false;
    }

    public Customer findCustomer(String customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(customerId)) return c;
        }
        return null;
    }

    /** Returns a message starting with "SUCCESS" or "ERROR". */
    public String bookRoom(int roomNumber, String customerId) {
        Room room = searchRoom(roomNumber);
        if (room == null)        return "ERROR: Room #" + roomNumber + " does not exist.";
        if (!room.isAvailable()) return "ERROR: Room #" + roomNumber + " is already occupied.";

        Customer customer = findCustomer(customerId);
        if (customer == null)    return "ERROR: Customer '" + customerId + "' not found.";
        if (customer.getAllocatedRoomNumber() != -1)
            return "ERROR: Customer already has Room #" + customer.getAllocatedRoomNumber() + ".";

        room.setAvailable(false);
        customer.setAllocatedRoomNumber(roomNumber);
        bookingMap.put(roomNumber, customerId);
        return "SUCCESS: Room #" + roomNumber + " booked for " + customer.getName() + ".";
    }

    /** Returns a message starting with "SUCCESS" or "ERROR". */
    public String checkoutRoom(int roomNumber) {
        Room room = searchRoom(roomNumber);
        if (room == null)       return "ERROR: Room #" + roomNumber + " does not exist.";
        if (room.isAvailable()) return "ERROR: Room #" + roomNumber + " is not currently booked.";

        String customerId = bookingMap.get(roomNumber);
        room.setAvailable(true);
        bookingMap.remove(roomNumber);

        if (customerId != null) {
            Customer c = findCustomer(customerId);
            if (c != null) c.setAllocatedRoomNumber(-1); // -1 means no room assigned
        }
        return "SUCCESS: Room #" + roomNumber + " is now free.";
    }

    /** Returns -1 if customer or room not found. */
    public double calculateBill(String customerId, int days) {
        Customer c = findCustomer(customerId);
        if (c == null || c.getAllocatedRoomNumber() == -1) return -1;
        Room r = searchRoom(c.getAllocatedRoomNumber());
        if (r == null) return -1;
        return r.getPricePerDay() * days;
    }

    /** Saves invoice to invoices/invoice_<guestId>_<date>.txt. Returns file path or null on failure. */
    public String saveInvoiceToFile(String invoiceText, String guestId) {
        File dir = new File("invoices");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String date     = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String filename = "invoices" + File.separator + "invoice_" + guestId + "_" + date + ".txt";

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print(invoiceText);
            System.out.println("[HotelManager] Invoice saved to " + filename);
            return filename;
        } catch (IOException e) {
            System.err.println("[HotelManager] Invoice save failed: " + e.getMessage());
            return null;
        }
    }

    /** Returns base price from the RoomCategory enum. */
    public double getBasePriceForCategory(RoomCategory category) {
        return category.getBasePrice();
    }

    /** Writes and reads room counts using RandomAccessFile (Week 6 RAF demo). */
    public String exportRoomSummaryRAF() {
        String filePath = "room_summary.dat";
        StringBuilder result = new StringBuilder();
        try {
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(filePath, "rw");
            raf.seek(0);
            raf.writeInt(rooms.size());              // write total count
            raf.writeInt(getAvailableRooms().size()); // write available count
            raf.seek(0);                              // rewind and read back
            int total     = raf.readInt();
            int available = raf.readInt();
            result.append("Total Rooms: ").append(total)
                  .append(" | Available: ").append(available);
            raf.close();
        } catch (java.io.IOException e) {
            result.append("RAF export failed: ").append(e.getMessage());
        }
        return result.toString();
    }

    /** Called only when hotel_data.ser does not exist. */
    public void loadSampleData() {
        addRoom(new StandardRoom(101, "Single", 1200)); // StandardRoom for Single/Double
        addRoom(new StandardRoom(102, "Single", 1200));
        addRoom(new StandardRoom(201, "Double", 2200));
        addRoom(new StandardRoom(202, "Double", 2200));
        addRoom(new DeluxeRoom(301, 4500, "Sea View, Jacuzzi"));
        addRoom(new DeluxeRoom(302, 5000, "Mountain View, Mini Bar"));

        addCustomer(new Customer("C001", "Arjun Sharma",  "9876543210"));
        addCustomer(new Customer("C002", "Priya Menon",   "9123456780"));
        addCustomer(new Customer("C003", "Rahul Verma",   "9988776655"));

        bookRoom(201, "C001");
    }
}
