package Airline;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Entry point for the Airline Reservation System.
 *
 * File persistence:
 *   ~/airline_flights.json   – all flights (loaded on start, saved on every change)
 *   ~/airline_bookings.json  – all bookings (loaded on start, saved on every change)
 *   ~/.airline_reservation_users.dat – user accounts (managed by UserStore, unchanged)
 */
public class Airlinereservationsystem extends Application {

    private final ObservableList<Flight>  flights  = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {

        // ── 1. Load persisted data ────────────────────────────────────────────
        FlightStore.load(flights);
        BookingStore.load(bookings);

        // ── 2. Seed default flights only when the file is empty/new ──────────
        if (flights.isEmpty()) {
            flights.addAll(
                    new Flight("SK101", "Karachi",   "Lahore",    "2025-08-01", "08:00", 50,  80),
                    new Flight("SK202", "Lahore",    "Islamabad", "2025-08-02", "10:30", 40,  60),
                    new Flight("SK303", "Karachi",   "Dubai",     "2025-08-03", "14:00", 80, 350),
                    new Flight("SK404", "Islamabad", "London",    "2025-08-04", "22:15", 60, 700)
            );
            FlightStore.save(flights);   // persist the seeds immediately
        }

        // ── 3. Auto-save whenever the lists are modified ──────────────────────
        //
        // ListChangeListener fires for add, remove, and update events.
        // This means every admin add/update/delete and every passenger
        // book/cancel is flushed to disk automatically — no manual save calls
        // are needed anywhere else in the codebase.
        flights.addListener((javafx.collections.ListChangeListener<Flight>) change -> {
            FlightStore.save(flights);
        });

        bookings.addListener((javafx.collections.ListChangeListener<Booking>) change -> {
            BookingStore.save(bookings);
        });

        // ── 4. Build the home screen (unchanged) ─────────────────────────────
        Label icon  = UIFactory.lbl("✈", "32px", Theme.PRIMARY, true);
        Label title = UIFactory.lbl("Airline Reservation", "26px", Theme.PRIMARY, true);
        Label sub   = UIFactory.lbl("Manage flights and bookings", "13px", Theme.MUTED, false);

        VBox brand = new VBox(4, icon, title, sub);
        brand.setAlignment(Pos.CENTER);

        Separator sep = new Separator();

        Button adminBtn = UIFactory.filledBtn("Admin Panel",                  Theme.PRIMARY);
        Button userBtn  = UIFactory.filledBtn("Passenger Sign In / Register", Theme.ACCENT);
        adminBtn.setPrefWidth(240);
        userBtn.setPrefWidth(240);

        adminBtn.setOnAction(e -> PlaneAnimation.play(primaryStage,
                () -> AdminLoginView.show(() -> AdminDashboardView.show(flights))));
        userBtn.setOnAction(e -> PlaneAnimation.play(primaryStage,
                () -> LoginView.show(Role.PASSENGER, () -> UserDashboardView.show(flights, bookings))));

        VBox buttons = new VBox(12, adminBtn, userBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox inner = new VBox(24, brand, sep, buttons);
        inner.setAlignment(Pos.CENTER);
        VBox card = UIFactory.cardBox(inner);
        card.setMaxWidth(340);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + Theme.BG + ";");

        primaryStage.setTitle("Airline Reservation System");
        primaryStage.setScene(new Scene(root, 520, 420));
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}
