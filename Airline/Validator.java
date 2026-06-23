package Airline;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Centralised validation for all flight-form inputs.
 * Compatible with Java 8+  (no String.isBlank() — uses trim().isEmpty() instead).
 *
 * Every method returns null on success, or a human-readable error string on failure.
 */
public final class Validator {

    private static final LocalDate MIN_DATE = LocalDate.of(1970, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.now().plusYears(10);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Validator() {}

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Java-8-safe blank check. */
    private static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ── individual field validators ───────────────────────────────────────────

    /** Flight ID: 2–10 alphanumeric characters. */
    public static String validateFlightId(String value) {
        if (blank(value))
            return "Flight ID is required.";
        if (!value.trim().matches("[A-Za-z0-9]{2,10}"))
            return "Flight ID must be 2–10 alphanumeric characters (e.g. SK101).";
        return null;
    }

    /** Source / Destination: letters, spaces, hyphens; 2–50 chars. */
    public static String validateCity(String fieldName, String value) {
        if (blank(value))
            return fieldName + " is required.";
        String v = value.trim();
        if (v.length() < 2)
            return fieldName + " must be at least 2 characters.";
        if (v.length() > 50)
            return fieldName + " must be 50 characters or fewer.";
        if (!v.matches("[A-Za-z\\s\\-]+"))
            return fieldName + " can only contain letters, spaces, and hyphens.";
        return null;
    }

    /**
     * Date: YYYY-MM-DD format, between 1970-01-01 and 10 years from today.
     * Rejects years like 1899 or 9999.
     */
    public static String validateDate(String value) {
        if (blank(value))
            return "Date is required.";
        LocalDate date;
        try {
            date = LocalDate.parse(value.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            return "Date must be in YYYY-MM-DD format (e.g. 2025-08-01).";
        }
        if (date.isBefore(MIN_DATE))
            return "Date cannot be before " + MIN_DATE + ". Did you mean a future date?";
        if (date.isAfter(MAX_DATE))
            return "Date cannot be after " + MAX_DATE + " (max 10 years ahead).";
        return null;
    }

    /** Time: HH:MM in 24-hour format (00:00 – 23:59). */
    public static String validateTime(String value) {
        if (blank(value))
            return "Time is required.";
        if (!value.trim().matches("([01]\\d|2[0-3]):[0-5]\\d"))
            return "Time must be in HH:MM format (00:00 – 23:59).";
        return null;
    }

    /** Seats: integer, 1 – 1 000. */
    public static String validateSeats(String value) {
        if (blank(value))
            return "Seat count is required.";
        int seats;
        try {
            seats = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return "Seats must be a whole number (e.g. 50).";
        }
        if (seats < 1)   return "Seats must be at least 1.";
        if (seats > 1000) return "Seats cannot exceed 1,000.";
        return null;
    }

    /** Price: positive decimal, $1 – $100,000. */
    public static String validatePrice(String value) {
        if (blank(value))
            return "Price is required.";
        double price;
        try {
            price = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return "Price must be a number (e.g. 350 or 149.99).";
        }
        if (price < 1)         return "Price must be at least $1.";
        if (price > 100_000)   return "Price cannot exceed $100,000.";
        return null;
    }

    /** Source and destination must differ (case-insensitive). */
    public static String validateSourceDestDifferent(String source, String destination) {
        if (source != null && destination != null
                && source.trim().equalsIgnoreCase(destination.trim()))
            return "Source and destination cannot be the same city.";
        return null;
    }

    // ── aggregate validator ───────────────────────────────────────────────────

    /**
     * Runs all checks in order; returns the first error or null if everything is valid.
     *
     * @param cabinSeats  seats strings for [Economy, PremiumEconomy, Business, First]
     * @param cabinPrices price strings for [Economy, PremiumEconomy, Business, First]
     */
    public static String validateFlight(String flightId, String source, String destination,
                                        String date, String time,
                                        String[] cabinSeats, String[] cabinPrices) {
        String err;

        if ((err = validateFlightId(flightId))                       != null) return err;
        if ((err = validateCity("Source", source))                   != null) return err;
        if ((err = validateCity("Destination", destination))         != null) return err;
        if ((err = validateSourceDestDifferent(source, destination)) != null) return err;
        if ((err = validateDate(date))                               != null) return err;
        if ((err = validateTime(time))                               != null) return err;

        CabinClass[] cabins = CabinClass.values();
        for (int i = 0; i < cabins.length; i++) {
            String label = cabins[i].getLabel();
            if ((err = validateSeats(cabinSeats[i]))  != null) return label + " seats: " + err;
            if ((err = validatePrice(cabinPrices[i])) != null) return label + " price: " + err;
        }

        // Enforce Economy ≤ PremiumEconomy ≤ Business ≤ First
        double[] prices = new double[4];
        for (int i = 0; i < 4; i++)
            prices[i] = Double.parseDouble(cabinPrices[i].trim());

        if (prices[0] > prices[1])
            return "Economy price ($" + (int)prices[0] + ") cannot exceed Premium Economy ($" + (int)prices[1] + ").";
        if (prices[1] > prices[2])
            return "Premium Economy price cannot exceed Business class price.";
        if (prices[2] > prices[3])
            return "Business price cannot exceed First class price.";

        return null;
    }
}