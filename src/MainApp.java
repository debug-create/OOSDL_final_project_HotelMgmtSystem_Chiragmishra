import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.scene.chart.*;

public class MainApp extends Application {

    private static final String BG_DEEP   = "#141414";
    private static final String BG_CARD   = "#1f1f1f";
    private static final String BG_INPUT  = "#2a2a2a";
    private static final String ACCENT    = "#c8963e";
    private static final String ACCENT2   = "#e8b86d";
    private static final String TEXT_MAIN = "#f0ead6";
    private static final String TEXT_DIM  = "#888880";
    private static final String BORDER    = "#333333";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private Stage primaryStage;

    private final HotelManager hotel = new HotelManager();

    private ObservableList<RoomRow>     roomData     = FXCollections.observableArrayList();
    private ObservableList<CustomerRow> customerData = FXCollections.observableArrayList();

    private Label lblTotalRooms     = new Label("0");
    private Label lblAvailableRooms = new Label("0");
    private Label lblOccupiedRooms  = new Label("0");
    private Label lblTotalGuests    = new Label("0");

    private TableView<RoomRow> occupiedTableRef;
    private PieChart                 occupancyChart; // Analytics — occupancy pie
    private BarChart<String, Number> roomTypeChart;  // Analytics — rooms by type
    private Label                    lblRevenue;     // Analytics — revenue label

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent loginRoot = loader.load();
            LoginController lc = loader.getController();
            lc.setMainApp(this);

            Scene loginScene = new Scene(loginRoot, 1100, 740);
            primaryStage.setTitle("LUMIÈRE — Hotel Management");
            primaryStage.setScene(loginScene);
            primaryStage.setMinWidth(940);
            primaryStage.setMinHeight(640);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("[MainApp] FXML load failed: " + e.getMessage());
            showMainApp(); // fallback — skip login if FXML missing
        }
    }

    public void showMainApp() {
        hotel.loadData();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DEEP + ";");
        root.setOpacity(0);
        root.setTop(buildHeader());

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: " + BG_DEEP + "; -fx-tab-min-height: 40px; -fx-font-size: 13px;");

        tabs.getTabs().addAll(
            buildTab("Dashboard", buildDashboardTab()),
            buildTab("Rooms",     buildRoomsTab()),
            buildTab("Guests",    buildGuestsTab()),
            buildTab("Bookings",  buildBookingsTab()),
            buildTab("Analytics", buildAnalyticsTab())
        );

        root.setCenter(tabs);
        root.setBottom(buildFooter());

        Scene scene = new Scene(root, 1100, 740);
        applyGlobalStyles(scene);

        primaryStage.setTitle("LUMIÈRE — Hotel Management");
        primaryStage.setScene(scene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        refreshAll();
    }

    private void showAlert(String title, String message, boolean isSuccess) {
        Alert alert = new Alert(isSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private HBox buildHeader() {
        Label brand = new Label("LUMIERE");
        brand.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        brand.setTextFill(Color.web(ACCENT));

        Label tagline = new Label("HOTEL MANAGEMENT SYSTEM");
        tagline.setFont(Font.font("Courier New", FontWeight.NORMAL, 11));
        tagline.setTextFill(Color.web(TEXT_DIM));

        VBox brandBox = new VBox(2, brand, tagline);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(brandBox);
        header.setPadding(new Insets(16, 28, 16, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + ACCENT + "; -fx-border-width: 0 0 1 0;");
        return header;
    }

    private HBox buildFooter() {
        Label lbl = new Label("Hotel Management System  |  Java + JavaFX  |  Serialization Demo");
        lbl.setFont(Font.font("Courier New", 10));
        lbl.setTextFill(Color.web(TEXT_DIM));

        HBox footer = new HBox(lbl);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(8));
        footer.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0;");
        return footer;
    }

    private Tab buildTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tab.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_MAIN + ";");
        return tab;
    }

    private VBox buildDashboardTab() {
        Label heading = sectionHeading("Overview");

        HBox kpiRow = new HBox(18,
            kpiCard("Total Rooms",      lblTotalRooms,     "[Rooms]"),
            kpiCard("Available",         lblAvailableRooms, "[Free]"),
            kpiCard("Occupied",          lblOccupiedRooms,  "[Booked]"),
            kpiCard("Registered Guests", lblTotalGuests,    "[Guests]")
        );
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        // RAF button — demonstrates RandomAccessFile I/O on the dashboard
        Button btnRAF = ghostButton("Export Summary (RAF)");
        Label lblRAF = statusLabel();
        btnRAF.setOnAction(e -> {
            String rafResult = hotel.exportRoomSummaryRAF();
            lblRAF.setText(rafResult);
            lblRAF.setTextFill(javafx.scene.paint.Color.web(ACCENT));
        });
        HBox rafRow = new HBox(10, btnRAF, lblRAF);
        rafRow.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + ACCENT + ";");

        Label infoHeading = sectionHeading("Currently Occupied Rooms");

        TableView<RoomRow> occupiedTable = buildRoomTable();
        occupiedTable.setMaxHeight(260);

        VBox container = new VBox(20, heading, kpiRow, rafRow, sep, infoHeading, occupiedTable);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: " + BG_DEEP + ";");

        occupiedTable.setItems(FXCollections.observableArrayList());
        this.occupiedTableRef = occupiedTable;
        return container;
    }

    private VBox kpiCard(String label, Label valueLabel, String icon) {
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(11));
        iconLabel.setTextFill(Color.web(TEXT_DIM));

        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(ACCENT2));

        Label nameLabel = new Label(label.toUpperCase());
        nameLabel.setFont(Font.font("Courier New", 10));
        nameLabel.setTextFill(Color.web(TEXT_DIM));

        VBox card = new VBox(6, iconLabel, valueLabel, nameLabel);
        card.setPadding(new Insets(20, 26, 20, 26));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(190);
        card.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER
            + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        return card;
    }

    private VBox buildRoomsTab() {
        Label heading = sectionHeading("Room Management");

        TextField tfRoomNo    = styledField("Room Number");
        ComboBox<String> cbType = styledCombo("Single", "Double", "Deluxe");
        TextField tfPrice     = styledField("Price per Day (Rs.)");
        TextField tfAmenities = styledField("Amenities (Deluxe only)");
        Button btnAdd         = accentButton("Add Room");
        btnAdd.setTooltip(new Tooltip("Add a new room to the system"));

        // Enum-based quick price selector — auto-fills price from RoomCategory base price
        ComboBox<String> cbCategory = styledCombo(
            "Use Custom Price", "Single Base (Rs.1200)", "Double Base (Rs.2200)", "Deluxe Base (Rs.4500)");
        cbCategory.setMaxWidth(210);
        cbCategory.setOnAction(e -> {
            switch (cbCategory.getValue()) {
                case "Single Base (Rs.1200)": tfPrice.setText("1200"); break;
                case "Double Base (Rs.2200)": tfPrice.setText("2200"); break;
                case "Deluxe Base (Rs.4500)": tfPrice.setText("4500"); break;
                default: break;
            }
        });

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12);
        form.addRow(0, formLabel("Room No."), tfRoomNo, formLabel("Type"), cbType);
        form.addRow(1, formLabel("Price/Day"), tfPrice, formLabel("Amenities"), tfAmenities);
        form.addRow(2, formLabel("Quick Price:"), cbCategory, new Label(""), new Label(""));
        form.add(btnAdd, 0, 3, 2, 1);
        form.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 18; -fx-background-radius: 4;");

        btnAdd.setOnAction(e -> {
            try {
                int    no    = Integer.parseInt(tfRoomNo.getText().trim());
                String type  = cbType.getValue();
                double price = Double.parseDouble(tfPrice.getText().trim());
                Room room;
                if ("Deluxe".equals(type)) {
                    String am = tfAmenities.getText().trim();
                    if (am.isEmpty()) am = "Standard Amenities";
                    room = new DeluxeRoom(no, price, am);
                } else {
                    room = new StandardRoom(no, type, price); // Single/Double use StandardRoom
                }
                if (hotel.addRoom(room)) {
                    new Thread(() -> hotel.saveData()).start(); // background thread — non-blocking UI
                    refreshAll();
                    showAlert("Room Added", "Room #" + no + " added successfully.", true);
                    tfRoomNo.clear(); tfPrice.clear(); tfAmenities.clear();
                } else {
                    showAlert("Duplicate Room", "Room #" + no + " already exists.", false);
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Enter a valid Room Number and Price.", false);
            }
        });

        ToggleGroup tg = new ToggleGroup();
        ToggleButton tbAll  = styledToggle("All Rooms",      tg);
        ToggleButton tbFree = styledToggle("Available Only", tg);
        ToggleButton tbOcc  = styledToggle("Occupied Only",  tg);
        tbAll.setSelected(true);
        tbAll.setTooltip(new Tooltip("Show all rooms"));
        tbFree.setTooltip(new Tooltip("Show only available rooms"));
        tbOcc.setTooltip(new Tooltip("Show only occupied rooms"));

        HBox toggleBar = new HBox(8, tbAll, tbFree, tbOcc);
        toggleBar.setAlignment(Pos.CENTER_LEFT);

        TableView<RoomRow> roomTable = buildRoomTable();
        roomTable.setItems(roomData);
        VBox.setVgrow(roomTable, Priority.ALWAYS);

        tbAll.setOnAction(e  -> roomData.setAll(toRoomRows(hotel.getAllRooms())));
        tbFree.setOnAction(e -> roomData.setAll(toRoomRows(hotel.getAvailableRooms())));
        tbOcc.setOnAction(e  -> roomData.setAll(toRoomRows(hotel.getOccupiedRooms())));

        TextField tfSearch  = styledField("Search by Room No.");
        tfSearch.setMaxWidth(200);
        Button btnSearch    = ghostButton("Search");
        Button btnSortPrice = ghostButton("Sort by Price");
        Button btnShowAll   = ghostButton("Show All");
        btnSearch.setTooltip(new Tooltip("Search for a room by number"));
        btnSortPrice.setTooltip(new Tooltip("Sort rooms by price ascending"));
        btnShowAll.setTooltip(new Tooltip("Reset and show all rooms"));

        HBox searchBar = new HBox(10, tfSearch, btnSearch, btnSortPrice, btnShowAll);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        btnSearch.setOnAction(e -> {
            try {
                int no = Integer.parseInt(tfSearch.getText().trim());
                Room found = hotel.searchRoom(no);
                if (found != null) {
                    roomData.setAll(toRoomRows(new ArrayList<>() {{ add(found); }}));
                } else {
                    showAlert("Not Found", "Room #" + no + " not found.", false);
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Enter a valid room number.", false);
            }
        });
        btnSortPrice.setOnAction(e -> roomData.setAll(toRoomRows(hotel.getRoomsSortedByPrice())));
        btnShowAll.setOnAction(e   -> roomData.setAll(toRoomRows(hotel.getAllRooms())));

        VBox tab = new VBox(16, heading, form, toggleBar, searchBar, roomTable);
        tab.setPadding(new Insets(24));
        tab.setStyle("-fx-background-color: " + BG_DEEP + ";");
        return tab;
    }

    private VBox buildGuestsTab() {
        Label heading = sectionHeading("Guest Management");

        TextField tfId      = styledField("Customer ID (e.g. C004)");
        TextField tfName    = styledField("Full Name");
        TextField tfContact = styledField("Contact Number");
        Button btnAdd       = accentButton("Register Guest");
        btnAdd.setTooltip(new Tooltip("Register a new guest in the system"));

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12);
        form.addRow(0, formLabel("Guest ID"),  tfId,      formLabel("Name"),    tfName);
        form.addRow(1, formLabel("Contact"),   tfContact, new Label(""), new Label(""));
        form.add(btnAdd, 0, 2, 2, 1);
        form.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 18; -fx-background-radius: 4;");

        btnAdd.setOnAction(e -> {
            String id  = tfId.getText().trim();
            String nm  = tfName.getText().trim();
            String ph  = tfContact.getText().trim();
            if (id.isEmpty() || nm.isEmpty() || ph.isEmpty()) {
                showAlert("Input Error", "All fields are required.", false);
                return;
            }
            if (hotel.addCustomer(new Customer(id, nm, ph))) {
                new Thread(() -> hotel.saveData()).start(); // background thread — non-blocking UI
                refreshAll();
                showAlert("Guest Registered", nm + " registered successfully.", true);
                tfId.clear(); tfName.clear(); tfContact.clear();
            } else {
                showAlert("Duplicate ID", "Customer ID '" + id + "' already exists.", false);
            }
        });

        TextField tfRemId = styledField("Guest ID to Remove");
        tfRemId.setMaxWidth(220);
        Button btnRemove  = ghostButton("Remove Guest");
        btnRemove.setTooltip(new Tooltip("Remove the guest and free their room"));

        HBox removeBar = new HBox(10, tfRemId, btnRemove);
        removeBar.setAlignment(Pos.CENTER_LEFT);

        btnRemove.setOnAction(e -> {
            String id = tfRemId.getText().trim();
            if (hotel.removeCustomer(id)) {
                new Thread(() -> hotel.saveData()).start(); // background thread — non-blocking UI
                refreshAll();
                showAlert("Guest Removed", "Guest '" + id + "' removed.", true);
                tfRemId.clear();
            } else {
                showAlert("Not Found", "Guest '" + id + "' not found.", false);
            }
        });

        Label spinLabel = sectionSubheading("Quick Bill — Days Stayed:");
        Spinner<Integer> daySpinner = new Spinner<>(1, 365, 1);
        daySpinner.setEditable(true);
        daySpinner.setPrefWidth(100);
        daySpinner.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-border-color: " + BORDER + ";");

        TextField tfBillId = styledField("Guest ID for Bill");
        tfBillId.setMaxWidth(180);
        Button btnQuickBill = ghostButton("Quick Bill");
        btnQuickBill.setTooltip(new Tooltip("Calculate a quick bill for a guest"));

        HBox spinRow = new HBox(12, formLabel("Guest ID"), tfBillId,
                                     formLabel("Days"), daySpinner,
                                     btnQuickBill);
        spinRow.setAlignment(Pos.CENTER_LEFT);
        spinRow.setPadding(new Insets(12));
        spinRow.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 4;");

        btnQuickBill.setOnAction(e -> {
            String cid = tfBillId.getText().trim();
            int days   = daySpinner.getValue();
            double bill = hotel.calculateBill(cid, days);
            if (bill < 0) {
                showAlert("Bill Error", "Guest not found or has no room allocated.", false);
            } else {
                double tax   = bill * 0.18;
                double total = bill + tax;
                showAlert("Quick Bill",
                    "Guest: " + cid + "\nDays: " + days
                    + "\nSubtotal: Rs." + String.format("%.2f", bill)
                    + "\nTax (18%): Rs." + String.format("%.2f", tax)
                    + "\nTOTAL: Rs." + String.format("%.2f", total),
                    true);
            }
        });

        TableView<CustomerRow> custTable = buildCustomerTable();
        custTable.setItems(customerData);
        VBox.setVgrow(custTable, Priority.ALWAYS);

        VBox tab = new VBox(16, heading, form, removeBar,
                                sectionSubheading("Quick Billing"), spinRow,
                                sectionHeading("Registered Guests"), custTable);
        tab.setPadding(new Insets(24));
        tab.setStyle("-fx-background-color: " + BG_DEEP + ";");
        return tab;
    }

    private VBox buildBookingsTab() {
        Label heading = sectionHeading("Bookings & Checkout");

        TextField tfBookRoom = styledField("Room Number");
        TextField tfBookCust = styledField("Guest ID");
        tfBookRoom.setMaxWidth(160); tfBookCust.setMaxWidth(160);
        Button btnBook = accentButton("Book Room");
        btnBook.setTooltip(new Tooltip("Book the selected room for a guest"));

        HBox bookRow = new HBox(12, formLabel("Room No."), tfBookRoom,
                                    formLabel("Guest ID"), tfBookCust, btnBook);
        bookRow.setAlignment(Pos.CENTER_LEFT);
        bookRow.setPadding(new Insets(16));
        bookRow.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 4;");

        btnBook.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(tfBookRoom.getText().trim());
                String cid = tfBookCust.getText().trim();
                String result = hotel.bookRoom(roomNo, cid);
                new Thread(() -> hotel.saveData()).start(); // background thread — non-blocking UI
                refreshAll();
                showAlert("Booking", result, result.startsWith("SUCCESS"));
                tfBookRoom.clear(); tfBookCust.clear();
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Enter a valid Room Number.", false);
            }
        });

        TextField tfCheckout = styledField("Room Number to Free");
        tfCheckout.setMaxWidth(200);
        Button btnCheckout = ghostButton("Checkout");
        btnCheckout.setTooltip(new Tooltip("Free the room and checkout the guest"));

        HBox checkoutRow = new HBox(12, formLabel("Room No."), tfCheckout, btnCheckout);
        checkoutRow.setAlignment(Pos.CENTER_LEFT);
        checkoutRow.setPadding(new Insets(16));
        checkoutRow.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 4;");

        btnCheckout.setOnAction(e -> {
            try {
                int no = Integer.parseInt(tfCheckout.getText().trim());
                String result = hotel.checkoutRoom(no);
                new Thread(() -> hotel.saveData()).start(); // background thread — non-blocking UI
                refreshAll();
                showAlert("Checkout", result, result.startsWith("SUCCESS"));
                tfCheckout.clear();
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Enter a valid Room Number.", false);
            }
        });

        Label invoiceHeading = sectionHeading("Invoice Generator");

        TextField tfInvGuest = styledField("Guest ID");
        tfInvGuest.setMaxWidth(160);

        TextField tfDays = styledField("Days Stayed");
        tfDays.setMaxWidth(100);

        DatePicker checkInDate = new DatePicker(LocalDate.now());
        checkInDate.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-border-color: " + BORDER + ";");
        checkInDate.setPrefWidth(160);

        Button btnInvoice = accentButton("Generate Invoice");
        btnInvoice.setTooltip(new Tooltip("Generate a detailed invoice for the guest"));
        Button btnSaveInv = ghostButton("Save to File");
        btnSaveInv.setTooltip(new Tooltip("Save the invoice as a .txt file in the invoices/ folder"));

        HBox invoiceControls = new HBox(12,
            formLabel("Guest ID"),    tfInvGuest,
            formLabel("Days"),        tfDays,
            formLabel("Check-in"),    checkInDate,
            btnInvoice, btnSaveInv);
        invoiceControls.setAlignment(Pos.CENTER_LEFT);

        TextArea invoiceArea = new TextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setPrefHeight(260);
        invoiceArea.setWrapText(false);
        invoiceArea.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-control-inner-background: #1f1f1f;" +
            "-fx-text-fill: " + TEXT_MAIN + ";" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 13px;"
        );
        invoiceArea.setText("(Generate an invoice to see it here)");

        btnInvoice.setOnAction(e -> {
            try {
                String cid  = tfInvGuest.getText().trim();
                int    days = Integer.parseInt(tfDays.getText().trim());
                LocalDate cin  = checkInDate.getValue();
                LocalDate cout = cin.plusDays(days);

                Customer c = hotel.findCustomer(cid);
                if (c == null) {
                    showAlert("Not Found", "Guest '" + cid + "' not found.", false);
                    return;
                }
                if (c.getAllocatedRoomNumber() == -1) {
                    showAlert("No Room", "Guest '" + cid + "' has no room allocated.", false);
                    return;
                }
                Room r = hotel.searchRoom(c.getAllocatedRoomNumber());
                if (r == null) {
                    showAlert("Room Error", "Could not find the room.", false);
                    return;
                }

                double subtotal = r.getPricePerDay() * days;
                double tax      = subtotal * 0.18;
                double total    = subtotal + tax;

                String invoice =
                    "================================\n" +
                    "         LUMIERE HOTEL          \n" +
                    "           INVOICE              \n" +
                    "================================\n" +
                    String.format("Guest ID   : %s%n", c.getCustomerId()) +
                    String.format("Guest Name : %s%n", c.getName()) +
                    String.format("Contact    : %s%n", c.getContactNumber()) +
                    String.format("Room No    : %d%n", r.getRoomNumber()) +
                    String.format("Room Type  : %s%n", r.getRoomType()) +
                    String.format("Price/Day  : Rs.%.0f%n", r.getPricePerDay()) +
                    String.format("Check-in   : %s%n", cin.format(DATE_FMT)) +
                    String.format("Check-out  : %s%n", cout.format(DATE_FMT)) +
                    String.format("Days Stayed: %d%n", days) +
                    "--------------------------------\n" +
                    String.format("Subtotal   : Rs.%.2f%n", subtotal) +
                    String.format("Tax (18%%)  : Rs.%.2f%n", tax) +
                    String.format("TOTAL      : Rs.%.2f%n", total) +
                    "================================\n" +
                    "   Thank you for your stay!     \n" +
                    "================================";

                invoiceArea.setText(invoice);
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Enter a valid number of days.", false);
            }
        });

        btnSaveInv.setOnAction(e -> {
            String text = invoiceArea.getText();
            if (text.startsWith("(Generate")) {
                showAlert("No Invoice", "Please generate an invoice first before saving.", false);
                return;
            }
            String cid  = tfInvGuest.getText().trim();
            String path = hotel.saveInvoiceToFile(text, cid.isEmpty() ? "guest" : cid);
            if (path != null) {
                showAlert("Invoice Saved ✓",
                    "Invoice saved successfully to:\n" + path
                    + "\n\nCheck the 'invoices' folder in your project directory.", true);
            } else {
                showAlert("Save Error", "Failed to save invoice. Check file permissions.", false);
            }
        });

        VBox invoiceBox = new VBox(12, invoiceHeading, invoiceControls, invoiceArea);
        invoiceBox.setPadding(new Insets(16));
        invoiceBox.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 4;");

        VBox tab = new VBox(16,
            heading,
            sectionSubheading("Book a Room"),     bookRow,
            sectionSubheading("Checkout Guest"),   checkoutRow,
            invoiceBox
        );
        tab.setPadding(new Insets(24));
        tab.setStyle("-fx-background-color: " + BG_DEEP + ";");
        return tab;
    }

    private TableView<RoomRow> buildRoomTable() {
        TableView<RoomRow> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setFixedCellSize(36);
        styleTable(tv);
        tv.getColumns().addAll(
            col("Room No.",    "roomNumber",   100),
            col("Type",        "roomType",     110),
            col("Price/Day",   "pricePerDay",  120),
            col("Status",      "status",       110),
            col("Description", "description",  300)
        );
        return tv;
    }

    private TableView<CustomerRow> buildCustomerTable() {
        TableView<CustomerRow> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setFixedCellSize(36);
        styleTable(tv);
        tv.getColumns().addAll(
            col("Guest ID",  "customerId",  100),
            col("Name",      "name",        160),
            col("Contact",   "contact",     140),
            col("Room No.",  "roomNumber",  100)
        );
        return tv;
    }

    private <T> TableColumn<T, String> col(String title, String property, double minWidth) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setMinWidth(minWidth);
        c.setStyle("-fx-alignment: CENTER-LEFT;");
        return c;
    }

    private void styleTable(TableView<?> tv) {
        tv.setStyle("-fx-background-color: " + BG_CARD + "; -fx-table-cell-border-color: "
            + BORDER + "; -fx-font-size: 13px;");
        tv.setPlaceholder(new Label("No data to display."));
    }

    private void refreshAll() {
        roomData.setAll(toRoomRows(hotel.getAllRooms()));

        customerData.clear();
        for (Customer c : hotel.getAllCustomers()) customerData.add(new CustomerRow(c));

        int total     = hotel.getAllRooms().size();
        int available = hotel.getAvailableRooms().size();
        lblTotalRooms.setText(String.valueOf(total));
        lblAvailableRooms.setText(String.valueOf(available));
        lblOccupiedRooms.setText(String.valueOf(total - available));
        lblTotalGuests.setText(String.valueOf(hotel.getAllCustomers().size()));

        if (occupiedTableRef != null) {
            occupiedTableRef.setItems(FXCollections.observableArrayList(
                toRoomRows(hotel.getOccupiedRooms())));
        }
        updateCharts(); // refresh Analytics tab
    }

    private VBox buildAnalyticsTab() {
        Label heading = sectionHeading("Analytics & Insights");
        Label subInfo = sectionSubheading("Live occupancy overview and revenue projection");

        // PieChart — Available vs Occupied
        occupancyChart = new PieChart();
        occupancyChart.setTitle("Occupancy");
        occupancyChart.setPrefSize(360, 290);
        occupancyChart.setLegendVisible(true);
        occupancyChart.setAnimated(false);
        occupancyChart.setStyle("-fx-background-color: " + BG_CARD + ";");

        // BarChart — rooms by type (Total vs Occupied)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Room Type");
        yAxis.setLabel("Count");
        roomTypeChart = new BarChart<>(xAxis, yAxis);
        roomTypeChart.setTitle("Rooms by Type");
        roomTypeChart.setPrefSize(420, 290);
        roomTypeChart.setAnimated(false);
        roomTypeChart.setStyle("-fx-background-color: " + BG_CARD + ";");

        HBox chartsRow = new HBox(20, occupancyChart, roomTypeChart);
        chartsRow.setAlignment(Pos.CENTER_LEFT);

        // Revenue projection label
        lblRevenue = new Label("—");
        lblRevenue.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        lblRevenue.setTextFill(Color.web(ACCENT));
        lblRevenue.setWrapText(true);
        lblRevenue.setMaxWidth(Double.MAX_VALUE);
        lblRevenue.setStyle("-fx-background-color: " + BG_CARD
            + "; -fx-padding: 16; -fx-background-radius: 4;");

        Button btnRefresh = accentButton("Refresh Analytics");
        btnRefresh.setTooltip(new Tooltip("Refresh charts with current data"));
        btnRefresh.setOnAction(e -> updateCharts());

        VBox tab = new VBox(16, heading, subInfo, btnRefresh, chartsRow,
                                sectionSubheading("Revenue Projection"), lblRevenue);
        tab.setPadding(new Insets(24));
        tab.setStyle("-fx-background-color: " + BG_DEEP + ";");
        return tab;
    }

    private void updateCharts() {
        if (occupancyChart == null || roomTypeChart == null) return;

        // Pie chart — occupancy breakdown
        int total     = hotel.getAllRooms().size();
        int available = hotel.getAvailableRooms().size();
        int occupied  = total - available;
        occupancyChart.getData().clear();
        occupancyChart.getData().addAll(
            new PieChart.Data("Available (" + available + ")", Math.max(available, 0.01)),
            new PieChart.Data("Occupied ("  + occupied  + ")", Math.max(occupied,  0.01))
        );

        // Bar chart — count and occupancy per room type
        roomTypeChart.getData().clear();
        XYChart.Series<String, Number> totalSeries    = new XYChart.Series<>();
        XYChart.Series<String, Number> occupiedSeries = new XYChart.Series<>();
        totalSeries.setName("Total");
        occupiedSeries.setName("Occupied");
        int sT = 0, dT = 0, xT = 0;
        int sO = 0, dO = 0, xO = 0;
        for (Room r : hotel.getAllRooms()) {
            switch (r.getRoomType()) {
                case "Single": sT++; if (!r.isAvailable()) sO++; break;
                case "Double": dT++; if (!r.isAvailable()) dO++; break;
                default:       xT++; if (!r.isAvailable()) xO++; break;
            }
        }
        totalSeries.getData().addAll(
            new XYChart.Data<>("Single", sT),
            new XYChart.Data<>("Double", dT),
            new XYChart.Data<>("Deluxe", xT));
        occupiedSeries.getData().addAll(
            new XYChart.Data<>("Single", sO),
            new XYChart.Data<>("Double", dO),
            new XYChart.Data<>("Deluxe", xO));
        roomTypeChart.getData().addAll(totalSeries, occupiedSeries);

        // Revenue projection
        double dailyRev     = hotel.getOccupiedRooms().stream()
                                   .mapToDouble(Room::getPricePerDay).sum();
        double monthlyRev   = dailyRev * 30;
        double occupancyPct = total == 0 ? 0 : (occupied * 100.0 / total);
        if (lblRevenue != null) {
            lblRevenue.setText(String.format(
                "Occupancy Rate: %.0f%%   |   Daily Revenue: Rs.%.0f   |   Monthly Projection: Rs.%.0f",
                occupancyPct, dailyRev, monthlyRev));
        }
    }

    private java.util.List<RoomRow> toRoomRows(java.util.List<Room> list) {
        java.util.List<RoomRow> rows = new java.util.ArrayList<>();
        for (Room r : list) rows.add(new RoomRow(r));
        return rows;
    }

    private Label sectionHeading(String text) {
        Label l = new Label(text.toUpperCase());
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        l.setTextFill(Color.web(ACCENT));
        l.setStyle("-fx-border-color: " + ACCENT + "; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 6 0;");
        return l;
    }

    private Label sectionSubheading(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        l.setTextFill(Color.web(TEXT_DIM));
        return l;
    }

    private Label formLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Courier New", 12));
        l.setTextFill(Color.web(TEXT_DIM));
        l.setMinWidth(80);
        return l;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_MAIN
            + "; -fx-prompt-text-fill: " + TEXT_DIM + "; -fx-border-color: " + BORDER
            + "; -fx-border-radius: 3; -fx-background-radius: 3; -fx-padding: 7 10 7 10; -fx-font-size: 13px;");
        tf.setPrefWidth(170);
        return tf;
    }

    private ComboBox<String> styledCombo(String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(items[0]);
        cb.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_MAIN
            + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 3; -fx-font-size: 13px;");
        return cb;
    }

    private ToggleButton styledToggle(String text, ToggleGroup group) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(group);
        tb.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_DIM
            + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 3; -fx-font-size: 12px;"
            + " -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        tb.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tb.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: #1a1a1a;"
                    + " -fx-font-weight: bold; -fx-border-color: " + ACCENT
                    + "; -fx-border-radius: 3; -fx-font-size: 12px; -fx-padding: 6 14 6 14;");
            } else {
                tb.setStyle("-fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_DIM
                    + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 3; -fx-font-size: 12px;"
                    + " -fx-padding: 6 14 6 14; -fx-cursor: hand;");
            }
        });
        return tb;
    }

    private Button accentButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: #1a1a1a; -fx-font-weight: bold;"
            + " -fx-font-size: 13px; -fx-padding: 8 18 8 18; -fx-background-radius: 3; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(ACCENT, ACCENT2)));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace(ACCENT2, ACCENT)));
        return b;
    }

    private Button ghostButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_DIM + "; -fx-border-color: "
            + BORDER + "; -fx-border-radius: 3; -fx-font-size: 12px; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace("transparent", BG_CARD).replace(TEXT_DIM, TEXT_MAIN)));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace(BG_CARD, "transparent").replace(TEXT_MAIN, TEXT_DIM)));
        return b;
    }

    // Dim inline status label used to show results next to buttons (e.g. RAF output)
    private Label statusLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Courier New", 12));
        l.setTextFill(Color.web(TEXT_DIM));
        return l;
    }

    private void applyGlobalStyles(Scene scene) {
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI', sans-serif;");
        String css =
            ".tab-pane .tab-header-area .tab-header-background { -fx-background-color: " + BG_CARD + "; }" +
            ".tab-pane .tab { -fx-background-color: " + BG_CARD + "; }" +
            ".tab-pane .tab:selected { -fx-background-color: " + BG_DEEP + "; }" +
            ".tab-pane .tab .tab-label { -fx-text-fill: " + TEXT_DIM + "; -fx-font-size: 13px; }" +
            ".tab-pane .tab:selected .tab-label { -fx-text-fill: " + ACCENT + "; -fx-font-weight: bold; }" +
            ".tab-pane .tab-header-area { -fx-background-color: " + BG_CARD + "; }" +
            ".tab-pane { -fx-tab-min-height: 42px; }" +
            ".table-view .column-header { -fx-background-color: " + BG_CARD + "; }" +
            ".table-view .column-header .label { -fx-text-fill: " + ACCENT + "; -fx-font-weight: bold; }" +
            ".table-view .table-row-cell { -fx-background-color: " + BG_DEEP + "; -fx-text-fill: " + TEXT_MAIN + "; }" +
            ".table-view .table-row-cell:selected { -fx-background-color: #2d2208; }" +
            ".table-view .table-row-cell:odd { -fx-background-color: #1c1c1c; }" +
            ".table-view .table-cell { -fx-text-fill: " + TEXT_MAIN + "; -fx-border-color: transparent; }" +
            ".table-view { -fx-border-color: " + BORDER + "; }" +
            ".scroll-bar { -fx-background-color: " + BG_DEEP + "; }" +
            ".scroll-bar .thumb { -fx-background-color: " + BORDER + "; }" +
            ".text-area { -fx-background-color: #1f1f1f; }" +
            ".text-area .content { -fx-background-color: #1f1f1f; }" +
            ".spinner .text-field { -fx-background-color: " + BG_INPUT + "; -fx-text-fill: " + TEXT_MAIN + "; }";
        scene.getStylesheets().add("data:text/css," + css.replace("#", "%23").replace(" ", "%20"));
    }

    public static class RoomRow {
        private final SimpleStringProperty roomNumber;
        private final SimpleStringProperty roomType;
        private final SimpleStringProperty pricePerDay;
        private final SimpleStringProperty status;
        private final SimpleStringProperty description;

        public RoomRow(Room r) {
            this.roomNumber  = new SimpleStringProperty(String.valueOf(r.getRoomNumber()));
            this.roomType    = new SimpleStringProperty(r.getRoomType());
            this.pricePerDay = new SimpleStringProperty("Rs." + String.format("%.0f", r.getPricePerDay()));
            this.status      = new SimpleStringProperty(r.isAvailable() ? "Free" : "Occupied");
            this.description = new SimpleStringProperty(r.getRoomDescription());
        }

        public String getRoomNumber()  { return roomNumber.get();  }
        public String getRoomType()    { return roomType.get();    }
        public String getPricePerDay() { return pricePerDay.get(); }
        public String getStatus()      { return status.get();      }
        public String getDescription() { return description.get(); }
    }

    public static class CustomerRow {
        private final SimpleStringProperty customerId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty contact;
        private final SimpleStringProperty roomNumber;

        public CustomerRow(Customer c) {
            this.customerId = new SimpleStringProperty(c.getCustomerId());
            this.name       = new SimpleStringProperty(c.getName());
            this.contact    = new SimpleStringProperty(c.getContactNumber());
            this.roomNumber = new SimpleStringProperty(
                c.getAllocatedRoomNumber() == -1 ? "None" : String.valueOf(c.getAllocatedRoomNumber()));
        }

        public String getCustomerId() { return customerId.get(); }
        public String getName()       { return name.get();       }
        public String getContact()    { return contact.get();    }
        public String getRoomNumber() { return roomNumber.get(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
