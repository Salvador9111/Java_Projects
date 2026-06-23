package Airline;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Comparator;

public final class UserDashboardView {
    private UserDashboardView() {}

    public static void show(ObservableList<Flight> flights, ObservableList<Booking> bookings) {
        Stage stage = new Stage();
        User current = SessionManager.getCurrentUser();
        String myUsername = current != null ? current.getUsername() : "";

        // ── Header ────────────────────────────────────────────────────────────
        Label dashTitle = UIFactory.sectionHead("Passenger Dashboard");
        Label whoami = UIFactory.lbl("Signed in as " + myUsername, "12px", Theme.MUTED, false);
        VBox titleBox = new VBox(2, dashTitle, whoami);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button securityBtn = UIFactory.outlineBtn("Account Security", Theme.ACCENT);
        Button logoutBtn   = UIFactory.outlineBtn("Logout",           Theme.DANGER);
        securityBtn.setOnAction(e -> AccountSecurityView.show());
        logoutBtn  .setOnAction(e -> { SessionManager.logout(); stage.close(); });

        HBox header = new HBox(10, titleBox, spacer, securityBtn, logoutBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Search bar ────────────────────────────────────────────────────────
        TextField searchField = UIFactory.textField("🔍  Search by ID, city, or date…");
        searchField.setMaxWidth(Double.MAX_VALUE);

        FilteredList<Flight> filteredFlights = new FilteredList<>(flights, f -> true);
        searchField.textProperty().addListener((obs, ov, nv) -> {
            String q = nv == null ? "" : nv.toLowerCase().trim();
            filteredFlights.setPredicate(f ->
                    q.isEmpty()
                            || f.getFlightId()   .toLowerCase().contains(q)
                            || f.getSource()     .toLowerCase().contains(q)
                            || f.getDestination().toLowerCase().contains(q)
                            || f.getDate()       .contains(q));
        });

        // ── Flight table ──────────────────────────────────────────────────────
        TableView<Flight> flightTable = TableFactory.buildFlightTable(flights);
        SortedList<Flight> sorted = new SortedList<>(filteredFlights);
        sorted.comparatorProperty().bind(flightTable.comparatorProperty());
        flightTable.setItems(sorted);
        flightTable.setPrefHeight(230);

        // ── Sort buttons ──────────────────────────────────────────────────────
        Button sortAscBtn  = UIFactory.outlineBtn("⬆ Price: Low → High", Theme.ACCENT);
        Button sortDescBtn = UIFactory.outlineBtn("⬇ Price: High → Low", "#7C3AED");
        sortAscBtn .setOnAction(e -> FXCollections.sort(flights, Comparator.comparingDouble(Flight::getPrice)));
        sortDescBtn.setOnAction(e -> FXCollections.sort(flights, Comparator.comparingDouble(Flight::getPrice).reversed()));
        HBox sortBtns = new HBox(10, sortAscBtn, sortDescBtn);

        // ── Cabin class selector ──────────────────────────────────────────────
        ComboBox<CabinClass> cabinBox = new ComboBox<>();
        cabinBox.getItems().addAll(CabinClass.values());
        cabinBox.setValue(CabinClass.ECONOMY);
        cabinBox.setMaxWidth(Double.MAX_VALUE);
        cabinBox.setStyle(
                "-fx-background-color:#F1F5F9;-fx-border-color:" + Theme.BORDER + ";" +
                        "-fx-border-radius:7;-fx-background-radius:7;-fx-font-size:13px;");

        // Dynamic fare label — updates when flight or cabin selection changes
        Label fareLabel = UIFactory.lbl("Select a flight and cabin to see the fare.", "12px", Theme.MUTED, false);

        Runnable updateFare = () -> {
            Flight sel    = flightTable.getSelectionModel().getSelectedItem();
            CabinClass cc = cabinBox.getValue();
            if (sel == null || cc == null) {
                fareLabel.setText("Select a flight and cabin to see the fare.");
                return;
            }
            int seats = sel.getCabinSeats(cc);
            double price = sel.getCabinPrice(cc);
            if (seats <= 0) {
                UIFactory.setMsg(fareLabel, Theme.DANGER, cc.getLabel() + " is fully booked on this flight.");
            } else {
                UIFactory.setMsg(fareLabel, Theme.SUCCESS,
                        cc.getLabel() + " — $" + String.format("%.0f", price) + "  |  " + seats + " seat(s) available");
            }
        };

        flightTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> updateFare.run());
        cabinBox.valueProperty().addListener((o, ov, nv) -> updateFare.run());

        // ── Book button ───────────────────────────────────────────────────────
        Button bookBtn = UIFactory.filledBtn("Book Selected Flight", Theme.ACCENT);
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        Label bookMsg = UIFactory.lbl("", "12px", Theme.SUCCESS, false);

        bookBtn.setOnAction(e -> {
            Flight sel    = flightTable.getSelectionModel().getSelectedItem();
            CabinClass cc = cabinBox.getValue();
            if (sel == null) { UIFactory.setMsg(bookMsg, Theme.DANGER, "Select a flight first."); return; }
            if (cc == null)  { UIFactory.setMsg(bookMsg, Theme.DANGER, "Select a cabin class."); return; }

            // Block booking on cancelled flights
            if (sel.getStatus() == FlightStatus.CANCELLED) {
                UIFactory.setMsg(bookMsg, Theme.DANGER, "This flight has been cancelled and cannot be booked.");
                return;
            }
            if (sel.getCabinSeats(cc) <= 0) {
                UIFactory.setMsg(bookMsg, Theme.DANGER, cc.getLabel() + " is fully booked on this flight.");
                return;
            }

            bookings.add(new Booking(myUsername, sel, cc));

            // Decrement the seat count for the booked cabin and persist/refresh.
            // Mutating the Flight object in place won't auto-notify the
            // ObservableList or TableView, so we save + refresh explicitly.
            sel.setCabinSeats(cc, sel.getCabinSeats(cc) - 1);
            FlightStore.save(flights);
            flightTable.refresh();

            UIFactory.setMsg(bookMsg, Theme.SUCCESS,
                    "✓  Booked " + sel.getFlightId() + " (" + cc.getLabel() + ") for " + myUsername);
            updateFare.run();
        });

        Label cabinLabel = UIFactory.lbl("Select Cabin Class:", "12px", Theme.MUTED, false);
        VBox bookCard = UIFactory.cardBox(new VBox(10,
                UIFactory.sectionHead("Book a Flight"),
                cabinLabel, cabinBox, fareLabel, bookBtn, bookMsg));

        // ── My Bookings ───────────────────────────────────────────────────────
        TableView<Booking> bookingTable = TableFactory.buildBookingTable();
        bookingTable.setPrefHeight(180);

        Button refreshBtn = UIFactory.outlineBtn("Refresh My Bookings", Theme.ACCENT);
        Button cancelBtn  = UIFactory.outlineBtn("Cancel Selected",     Theme.DANGER);
        Label  listMsg    = UIFactory.lbl("", "12px", Theme.SUCCESS, false);

        Runnable refreshMine = () -> {
            ObservableList<Booking> mine = FXCollections.observableArrayList();
            for (Booking b : bookings)
                if (b.getPassengerName().equalsIgnoreCase(myUsername))
                    mine.add(b);
            bookingTable.setItems(mine);
            if (mine.isEmpty())
                UIFactory.setMsg(listMsg, Theme.MUTED, "You have no bookings yet.");
            else
                listMsg.setText("");
        };

        refreshBtn.setOnAction(e -> refreshMine.run());
        cancelBtn .setOnAction(e -> {
            Booking sel = bookingTable.getSelectionModel().getSelectedItem();
            if (sel == null) { UIFactory.setMsg(listMsg, Theme.DANGER, "Select a booking to cancel."); return; }

            // Remove exactly this booking instance — NOT every booking that
            // happens to match the same flight/cabin/passenger, otherwise
            // duplicate bookings on the same flight+cabin all get wiped out.
            bookings.remove(sel);

            // Restore the seat that this cancellation frees up.
            for (Flight f : flights) {
                if (f.getFlightId().equals(sel.getFlightId())) {
                    f.setCabinSeats(sel.getCabinClass(), f.getCabinSeats(sel.getCabinClass()) + 1);
                    break;
                }
            }
            FlightStore.save(flights);
            flightTable.refresh();

            refreshMine.run();
            UIFactory.setMsg(listMsg, Theme.SUCCESS, "✓  Booking cancelled.");
        });

        refreshMine.run();

        VBox myBookCard = UIFactory.cardBox(new VBox(12,
                UIFactory.sectionHead("My Bookings"),
                new HBox(10, refreshBtn, cancelBtn),
                bookingTable, listMsg));

        // ── Assemble ──────────────────────────────────────────────────────────
        VBox searchBox = UIFactory.cardBox(new VBox(10,
                UIFactory.sectionHead("Search Flights"), searchField, sortBtns));

        VBox content = new VBox(16,
                header, new Separator(),
                searchBox,
                UIFactory.sectionHead("Available Flights"), flightTable,
                bookCard, myBookCard);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color:" + Theme.BG + ";");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + Theme.BG + ";-fx-background:" + Theme.BG + ";");

        stage.setTitle("Passenger Dashboard");
        stage.setScene(new Scene(scroll, 980, 820));
        stage.show();
    }
}