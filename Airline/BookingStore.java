package Airline;

import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists the bookings list to a binary file using Java Object Serialization.
 *
 * File location: ~/airline_bookings.dat
 */
public final class BookingStore {

    private static final Path STORE_FILE =
            Paths.get(System.getProperty("user.home"), "airline_bookings.dat");

    private BookingStore() {}

    // ── Save ─────────────────────────────────────────────────────────────────

    public static synchronized void save(ObservableList<Booking> bookings) {
        Path tmp = STORE_FILE.resolveSibling("airline_bookings.tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(tmp.toFile())))) {

            out.writeObject(new ArrayList<>(bookings));

            Files.move(tmp, STORE_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (IOException e) {
            System.err.println("BookingStore: could not save bookings — " + e.getMessage());
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static synchronized void load(ObservableList<Booking> bookings) {
        if (!Files.exists(STORE_FILE)) return;

        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(STORE_FILE.toFile())))) {

            List<Booking> loaded = (List<Booking>) in.readObject();
            bookings.clear();
            bookings.addAll(loaded);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("BookingStore: could not load bookings — " + e.getMessage());
            // If the old binary format is incompatible (e.g. serialVersionUID changed),
            // delete the stale file so the app starts clean next run.
            if (e instanceof InvalidClassException) {
                try {
                    Files.deleteIfExists(STORE_FILE);
                    System.err.println("BookingStore: deleted incompatible bookings file — will start fresh.");
                } catch (IOException ignored) {}
            }
        }
    }
}