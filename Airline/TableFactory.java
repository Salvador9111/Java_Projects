package Airline;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public final class TableFactory {
    private TableFactory() {}

    public static TableView<Flight> buildFlightTable(ObservableList<Flight> flights) {
        TableView<Flight> table = new TableView<>();
        styleTable(table);

        // ── Status column with coloured badge ─────────────────────────────────
        TableColumn<Flight, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus().getLabel() : "Scheduled"));
        colStatus.setCellFactory(col -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Flight f = getTableRow() != null ? (Flight) getTableRow().getItem() : null;
                String color = (f != null && f.getStatus() != null)
                        ? f.getStatus().getColor() : "#0EA5E9";
                Circle dot = new Circle(5);
                dot.setFill(Color.web(color));
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
                HBox box = new HBox(5, dot, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        });

        table.getColumns().addAll(
                strCol("Flight ID",    d -> d.getValue().getFlightId()),
                strCol("Source",       d -> d.getValue().getSource()),
                strCol("Destination",  d -> d.getValue().getDestination()),
                strCol("Date",         d -> d.getValue().getDate()),
                strCol("Time",         d -> d.getValue().getTime()),
                colStatus,
                strCol("Total Seats",  d -> String.valueOf(d.getValue().getSeats())),
                strCol("Eco ($)",      d -> String.format("%.0f", d.getValue().getCabinPrice(CabinClass.ECONOMY))),
                strCol("Prem.Eco ($)", d -> String.format("%.0f", d.getValue().getCabinPrice(CabinClass.PREMIUM_ECONOMY))),
                strCol("Biz ($)",      d -> String.format("%.0f", d.getValue().getCabinPrice(CabinClass.BUSINESS))),
                strCol("First ($)",    d -> String.format("%.0f", d.getValue().getCabinPrice(CabinClass.FIRST)))
        );

        // Coloured rows
        table.setRowFactory(tv -> new TableRow<Flight>() {
            @Override
            protected void updateItem(Flight item, boolean empty) {
                super.updateItem(item, empty);
                applyStyle();
            }
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                applyStyle();
            }
            private void applyStyle() {
                Flight item = getItem();
                if (isEmpty() || item == null) { setStyle(""); return; }
                int idx = flights.indexOf(item);
                if (idx < 0) idx = getIndex();
                String bg = Theme.ROW_COLORS[Math.abs(idx) % Theme.ROW_COLORS.length];
                if (isSelected()) {
                    setStyle("-fx-background-color:derive(" + bg + ",-30%);-fx-text-fill:#000;-fx-font-weight:bold;-fx-font-size:13px;");
                } else {
                    setStyle("-fx-background-color:" + bg + ";-fx-text-fill:#1E293B;-fx-font-weight:normal;-fx-font-size:13px;");
                }
            }
        });

        return table;
    }

    public static TableView<Booking> buildBookingTable() {
        TableView<Booking> table = new TableView<>();
        styleTable(table);
        table.setPlaceholder(new Label("No bookings to display."));

        TableColumn<Booking, String> colId    = new TableColumn<>("Flight ID");
        TableColumn<Booking, String> colPax   = new TableColumn<>("Passenger");
        TableColumn<Booking, String> colCabin = new TableColumn<>("Cabin");
        TableColumn<Booking, String> colSrc   = new TableColumn<>("Source");
        TableColumn<Booking, String> colDest  = new TableColumn<>("Destination");
        TableColumn<Booking, String> colDate  = new TableColumn<>("Date");
        TableColumn<Booking, String> colTime  = new TableColumn<>("Time");

        colId   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFlightId()));
        colPax  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPassengerName()));
        colCabin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCabinClass() != null ? d.getValue().getCabinClass().getLabel() : "Economy"));
        colSrc  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSource()));
        colDest .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDestination()));
        colDate .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate()));
        colTime .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTime()));

        table.getColumns().addAll(colId, colPax, colCabin, colSrc, colDest, colDate, colTime);
        return table;
    }

    private static void styleTable(TableView<?> table) {
        table.setStyle(
                "-fx-background-color:" + Theme.CARD + ";" +
                        "-fx-border-color:" + Theme.BORDER + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;-fx-font-size:13px;"
        );
        table.setFixedCellSize(38);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No records available."));
    }

    private static TableColumn<Flight, String> strCol(
            String header,
            java.util.function.Function<TableColumn.CellDataFeatures<Flight, String>, String> fn) {
        TableColumn<Flight, String> col = new TableColumn<>(header);
        col.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d)));
        return col;
    }
}