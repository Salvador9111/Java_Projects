package Airline;

/** Lifecycle states a flight can be in. */
public enum FlightStatus {
    SCHEDULED   ("Scheduled",    "#0EA5E9"),   // blue
    BOARDING    ("Boarding",     "#F59E0B"),   // amber
    DEPARTED    ("Departed",     "#8B5CF6"),   // purple
    LANDED      ("Landed",       "#10B981"),   // green
    DELAYED     ("Delayed",      "#F97316"),   // orange
    CANCELLED   ("Cancelled",    "#EF4444");   // red

    private final String label;
    private final String color;

    FlightStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }

    @Override public String toString() { return label; }
}