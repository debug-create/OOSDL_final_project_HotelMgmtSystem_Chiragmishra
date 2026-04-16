# LUMIÈRE — Hotel Management System
### JavaFX Desktop Application | Java + Collections

---

## 📂 Project Structure

```
HotelManagementSystem/
├── pom.xml                                          ← Maven build file
└── src/main/java/
    ├── module-info.java                             ← JavaFX module declaration
    ├── hotel/
    │   ├── model/
    │   │   ├── Room.java          ← Base room class (Encapsulation)
    │   │   ├── DeluxeRoom.java    ← Inherits Room (Inheritance + Polymorphism)
    │   │   └── Customer.java      ← Guest entity (Encapsulation)
    │   ├── manager/
    │   │   └── HotelManager.java  ← Business logic + Collections (ArrayList, HashMap, Iterator)
    │   └── ui/
    │       └── MainApp.java       ← JavaFX UI (TabPane, TableView, GridPane, etc.)
```

---

## ⚙️ Prerequisites

| Requirement | Version  |
|-------------|----------|
| Java JDK    | 17+      |
| JavaFX SDK  | 17–21    |
| Maven       | 3.6+     |

---

## 🚀 Method 1 — Run via Maven (EASIEST)

Maven automatically downloads JavaFX. No SDK setup needed.

```bash
# Clone / extract the project
cd HotelManagementSystem

# Run directly
mvn javafx:run
```

---

## 🚀 Method 2 — Run via IntelliJ IDEA

1. **Open** IntelliJ → `File → Open` → select `HotelManagementSystem` folder
2. IntelliJ detects `pom.xml` → click **"Load Maven Project"**
3. Wait for dependencies to download
4. Open `src/main/java/hotel/ui/MainApp.java`
5. Click the **▶ green Run button** next to `main()`

---

## 🚀 Method 3 — Run via Command Line (with JavaFX SDK)

If you have JavaFX SDK downloaded separately (e.g., to `/opt/javafx-sdk-21`):

```bash
# Compile
javac --module-path /opt/javafx-sdk-21/lib \
      --add-modules javafx.controls,javafx.fxml \
      -d out \
      src/main/java/module-info.java \
      src/main/java/hotel/model/*.java \
      src/main/java/hotel/manager/*.java \
      src/main/java/hotel/ui/*.java

# Run
java --module-path /opt/javafx-sdk-21/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp out \
     hotel.ui.MainApp
```

> **Windows users:** Replace `/` with `\` and `:` with `;` in classpath.

---

## 🖥️ VM Options (if using IDE without module-info)

If you're getting `Error: JavaFX runtime components are missing`, add these VM options:

```
--module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
```

In IntelliJ: `Run → Edit Configurations → VM Options`

---

## 🎨 UI Overview

| Tab        | Features                                      |
|------------|-----------------------------------------------|
| Dashboard  | KPI cards (total/available/occupied/guests)   |
| Rooms      | Add room, search, sort by price, filter       |
| Guests     | Register guest, view, remove                  |
| Bookings   | Book room, checkout, bill calculator          |

---

## 📚 OOP Concepts Demonstrated

| Concept       | Where                                      |
|---------------|--------------------------------------------|
| Encapsulation | `Room.java`, `Customer.java` (private + getters/setters) |
| Inheritance   | `DeluxeRoom extends Room`                  |
| Polymorphism  | `DeluxeRoom.getRoomDescription()` overrides `Room` |
| Abstraction   | `HotelManager` hides data logic from UI    |

## 🗂️ Collections Used

| Collection            | Purpose                              |
|-----------------------|--------------------------------------|
| `ArrayList<Room>`     | Store all hotel rooms                |
| `ArrayList<Customer>` | Store all guests                     |
| `HashMap<Integer,String>` | Map room# → guestId (bookings)  |
| `Iterator`            | Used in `getAvailableRooms()`, `removeCustomer()` |

---

## 🧪 Sample Data (loaded on startup)

| Room | Type    | Price  | Status   |
|------|---------|--------|----------|
| 101  | Single  | ₹1200  | Free     |
| 102  | Single  | ₹1200  | Free     |
| 201  | Double  | ₹2200  | Occupied |
| 202  | Double  | ₹2200  | Free     |
| 301  | Deluxe  | ₹4500  | Free     |
| 302  | Deluxe  | ₹5000  | Free     |

Guests: C001 (Arjun Sharma), C002 (Priya Menon), C003 (Rahul Verma)
Pre-booked: Room 201 → C001

---

## 🎓 Viva Tips

- **Why ArrayList over array?** — Dynamic size; no fixed capacity needed for a hotel.
- **Why HashMap for bookings?** — O(1) lookup: "who is in room 301?" answered instantly.
- **Why Iterator for remove?** — Avoids `ConcurrentModificationException` when removing while looping.
- **How is polymorphism shown?** — `DeluxeRoom.getRoomDescription()` produces different output than `Room.getRoomDescription()` even when stored as a `Room` reference.
