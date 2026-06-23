package Airline;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a single flight with:
 *  - Basic info  : ID, source, destination, date, time
 *  - Status      : one of {@link FlightStatus} (Scheduled, Boarding, …)
 *  - Cabin data  : per-{@link CabinClass} seat count and fare price
 *
 * Legacy single-cabin constructor is kept for backward compatibility with
 * existing FlightStore binary files — it maps everything to Economy.
 */
public class Flight implements Serializable {
    private static final long serialVersionUID = 2L;   // bumped because fields changed

    // ── Core fields ───────────────────────────────────────────────────────────
    private String       flightId;
    private String       source;
    private String       destination;
    private String       date;
    private String       time;
    private FlightStatus status;

    // ── Per-cabin data ────────────────────────────────────────────────────────
    /** Number of available seats per cabin class. */
    private Map<CabinClass, Integer> cabinSeats  = new EnumMap<>(CabinClass.class);
    /** Fare price per cabin class. */
    private Map<CabinClass, Double>  cabinPrices = new EnumMap<>(CabinClass.class);

    // ── Full constructor (used by AdminDashboardView) ─────────────────────────

    public Flight(String flightId, String source, String destination,
                  String date, String time, FlightStatus status,
                  Map<CabinClass, Integer> cabinSeats,
                  Map<CabinClass, Double>  cabinPrices) {
        this.flightId    = flightId;
        this.source      = source;
        this.destination = destination;
        this.date        = date;
        this.time        = time;
        this.status      = status != null ? status : FlightStatus.SCHEDULED;
        this.cabinSeats  = new EnumMap<>(cabinSeats);
        this.cabinPrices = new EnumMap<>(cabinPrices);
    }

    /**
     * Legacy single-cabin constructor — kept so default seed flights in
     * FlightStore and existing tests compile without changes.
     * Maps all seats/price to Economy; other cabins get proportional defaults.
     */
    public Flight(String flightId, String source, String destination,
                  String date, String time, int seats, double price) {
        this.flightId    = flightId;
        this.source      = source;
        this.destination = destination;
        this.date        = date;
        this.time        = time;
        this.status      = FlightStatus.SCHEDULED;

        // Distribute seats across cabins with sensible ratios
        int eco   = Math.max(1, (int)(seats * 0.60));
        int pe    = Math.max(1, (int)(seats * 0.20));
        int biz   = Math.max(1, (int)(seats * 0.12));
        int first = Math.max(1, seats - eco - pe - biz);

        cabinSeats.put(CabinClass.ECONOMY,         eco);
        cabinSeats.put(CabinClass.PREMIUM_ECONOMY, pe);
        cabinSeats.put(CabinClass.BUSINESS,        biz);
        cabinSeats.put(CabinClass.FIRST,           first);

        // Price tiers relative to the base economy price
        cabinPrices.put(CabinClass.ECONOMY,         price);
        cabinPrices.put(CabinClass.PREMIUM_ECONOMY, Math.round(price * 1.5 * 100.0) / 100.0);
        cabinPrices.put(CabinClass.BUSINESS,        Math.round(price * 2.5 * 100.0) / 100.0);
        cabinPrices.put(CabinClass.FIRST,           Math.round(price * 4.0 * 100.0) / 100.0);
    }

    // ── Core getters / setters ────────────────────────────────────────────────

    public String getFlightId()    { return flightId; }
    public String getSource()      { return source; }
    public String getDestination() { return destination; }
    public String getDate()        { return date; }
    public String getTime()        { return time; }
    public FlightStatus getStatus(){ return status; }

    public void setFlightId(String v)    { this.flightId    = v; }
    public void setSource(String v)      { this.source      = v; }
    public void setDestination(String v) { this.destination = v; }
    public void setDate(String v)        { this.date        = v; }
    public void setTime(String v)        { this.time        = v; }
    public void setStatus(FlightStatus v){ this.status      = v != null ? v : FlightStatus.SCHEDULED; }

    // ── Cabin getters / setters ───────────────────────────────────────────────

    public int    getCabinSeats (CabinClass c) { return cabinSeats .getOrDefault(c, 0);   }
    public double getCabinPrice (CabinClass c) { return cabinPrices.getOrDefault(c, 0.0); }

    public void setCabinSeats (CabinClass c, int seats)    { cabinSeats .put(c, seats); }
    public void setCabinPrice (CabinClass c, double price) { cabinPrices.put(c, price); }

    // ── Legacy helpers (used by TableFactory / UserDashboard) ────────────────

    /** Total seats across all cabin classes. */
    public int getSeats() {
        return cabinSeats.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Economy price (lowest fare — used for sorting / display). */
    public double getPrice() {
        return cabinPrices.getOrDefault(CabinClass.ECONOMY, 0.0);
    }

    /** Cheapest available fare across all cabins. */
    public double getLowestPrice() {
        return cabinPrices.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }
}