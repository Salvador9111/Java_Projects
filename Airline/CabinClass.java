package Airline;

/** The four cabin classes available on a flight. */
public enum CabinClass {
    ECONOMY          ("Economy",           "E"),
    PREMIUM_ECONOMY  ("Premium Economy",   "PE"),
    BUSINESS         ("Business",          "B"),
    FIRST            ("First",             "F");

    private final String label;
    private final String code;

    CabinClass(String label, String code) {
        this.label = label;
        this.code  = code;
    }

    public String getLabel() { return label; }
    public String getCode()  { return code;  }

    @Override public String toString() { return label; }
}