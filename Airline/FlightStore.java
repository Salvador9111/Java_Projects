package Airline;


import javafx.collections.ObservableList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists the flights list to a human-readable JSON file.

 * File location: ~/airline_flights.json
 * Usage:
 *   FlightStore.load(flights);   // call once at startup to populate the ObservableList
 *   FlightStore.save(flights);   // call whenever the list changes (add/update/delete)
 */
public final class FlightStore {

    private static final Path STORE_FILE =
            Paths.get(System.getProperty("user.home"), "airline_flights.json");

    private FlightStore() {}

    // ── Save ─────────────────────────────────────────────────────────────────

    /**
     * Serializes every flight in the list to JSON and writes it atomically.
     */
    public static synchronized void save(ObservableList<Flight> flights) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            sb.append("  {\n");
            sb.append("    \"flightId\": ")    .append(jsonStr(f.getFlightId()))    .append(",\n");
            sb.append("    \"source\": ")      .append(jsonStr(f.getSource()))      .append(",\n");
            sb.append("    \"destination\": ") .append(jsonStr(f.getDestination())).append(",\n");
            sb.append("    \"date\": ")        .append(jsonStr(f.getDate()))        .append(",\n");
            sb.append("    \"time\": ")        .append(jsonStr(f.getTime()))        .append(",\n");
            sb.append("    \"seats\": ")       .append(f.getSeats())               .append(",\n");
            sb.append("    \"price\": ")       .append(f.getPrice())               .append("\n");
            sb.append("  }");
            if (i < flights.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");

        try {
            // Write to a temp file first, then move — prevents partial writes
            Path tmp = STORE_FILE.resolveSibling(STORE_FILE.getFileName() + ".tmp");
            Files.write(tmp, sb.toString().getBytes(StandardCharsets.UTF_8));
            Files.move(tmp, STORE_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("FlightStore: could not save flights — " + e.getMessage());
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    /**
     * Reads the JSON file and populates {@code flights}.
     * Clears the list first so no duplicates appear on repeated loads.
     * If the file does not exist, the list is left untouched (callers can
     * then seed default data themselves).
     */
    public static synchronized void load(ObservableList<Flight> flights) {
        if (!Files.exists(STORE_FILE)) return;

        try {
            String json = new String(Files.readAllBytes(STORE_FILE), StandardCharsets.UTF_8).trim();
            List<Flight> loaded = parseJson(json);
            flights.clear();
            flights.addAll(loaded);
        } catch (IOException e) {
            System.err.println("FlightStore: could not read flights file — " + e.getMessage());
        } catch (Exception e) {
            System.err.println("FlightStore: could not parse flights JSON — " + e.getMessage());
        }
    }

    // ── Minimal handwritten JSON parser ─────────────────────────────────────
    // Keeps the project dependency-free (no Gson / Jackson required).

    private static List<Flight> parseJson(String json) {
        List<Flight> list = new ArrayList<>();
        // Split on object boundaries: each flight is between { ... }
        int i = 0;
        while (i < json.length()) {
            int start = json.indexOf('{', i);
            if (start < 0) break;
            int end = json.indexOf('}', start);
            if (end < 0) break;
            String obj = json.substring(start + 1, end);
            Flight f = parseObject(obj);
            if (f != null) list.add(f);
            i = end + 1;
        }
        return list;
    }

    private static Flight parseObject(String obj) {
        try {
            String flightId    = field(obj, "flightId");
            String source      = field(obj, "source");
            String destination = field(obj, "destination");
            String date        = field(obj, "date");
            String time        = field(obj, "time");
            int    seats       = Integer.parseInt(field(obj, "seats"));
            double price       = Double.parseDouble(field(obj, "price"));
            return new Flight(flightId, source, destination, date, time, seats, price);
        } catch (Exception e) {
            System.err.println("FlightStore: skipping malformed entry — " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the value of a JSON key from a single object's contents.
     * Works for both quoted strings and bare numbers.
     */
    private static String field(String obj, String key) {
        String search = "\"" + key + "\"";
        int ki = obj.indexOf(search);
        if (ki < 0) throw new IllegalArgumentException("Missing key: " + key);
        int colon = obj.indexOf(':', ki + search.length());
        if (colon < 0) throw new IllegalArgumentException("Missing colon after: " + key);
        int valStart = colon + 1;
        while (valStart < obj.length() && Character.isWhitespace(obj.charAt(valStart))) valStart++;
        if (obj.charAt(valStart) == '"') {
            // Quoted string — find closing quote (skip escaped quotes)
            int q = valStart + 1;
            while (q < obj.length() && !(obj.charAt(q) == '"' && obj.charAt(q - 1) != '\\')) q++;
            return obj.substring(valStart + 1, q);
        } else {
            // Number or keyword — ends at comma, newline, or closing brace
            int end = valStart;
            while (end < obj.length() && ",\n\r}".indexOf(obj.charAt(end)) < 0) end++;
            return obj.substring(valStart, end).trim();
        }
    }

    /** Produces a JSON-safe quoted string (escapes backslashes and double-quotes). */
    private static String jsonStr(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
