package Airline;

import java.io.Serializable;

public class Booking implements Serializable {
    private static final long serialVersionUID = 2L;

    private String     passengerName;
    private String     flightId;
    private String     source;
    private String     destination;
    private String     date;
    private String     time;
    private CabinClass cabinClass;

    /** Runtime constructor — passenger books a specific cabin on a flight. */
    public Booking(String passengerName, Flight flight, CabinClass cabinClass) {
        this.passengerName = passengerName;
        this.flightId      = flight.getFlightId();
        this.source        = flight.getSource();
        this.destination   = flight.getDestination();
        this.date          = flight.getDate();
        this.time          = flight.getTime();
        this.cabinClass    = cabinClass;
    }

    /** Legacy constructor (keeps BookingStore backward-compat). */
    public Booking(String passengerName, Flight flight) {
        this(passengerName, flight, CabinClass.ECONOMY);
    }

    /** Deserialisation constructor used by BookingStore. */
    public Booking(String passengerName, String flightId,
                   String source, String destination,
                   String date, String time, CabinClass cabinClass) {
        this.passengerName = passengerName;
        this.flightId      = flightId;
        this.source        = source;
        this.destination   = destination;
        this.date          = date;
        this.time          = time;
        this.cabinClass    = cabinClass != null ? cabinClass : CabinClass.ECONOMY;
    }

    public String     getPassengerName() { return passengerName; }
    public String     getFlightId()      { return flightId; }
    public String     getSource()        { return source; }
    public String     getDestination()   { return destination; }
    public String     getDate()          { return date; }
    public String     getTime()          { return time; }
    public CabinClass getCabinClass()    { return cabinClass; }
}