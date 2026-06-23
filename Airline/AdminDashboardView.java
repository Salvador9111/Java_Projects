package Airline;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

/**
 * Admin Dashboard — includes:
 *   • Flight status management  (dropdown per flight)
 *   • Cabin class management    (Economy / Premium Economy / Business / First)
 *   • Fare class management     (per-cabin seat count + price)
 *   • Full input validation     (via Validator.java)
 */
public final class AdminDashboardView {
    private AdminDashboardView() {}

    public static void show(ObservableList<Flight> flights) {
        Stage stage = new Stage();

        // ── Header ────────────────────────────────────────────────────────────
        User current = SessionManager.getCurrentUser();
        Label dashTitle = UIFactory.sectionHead("Admin Dashboard");
        Label whoami = UIFactory.lbl(
                current != null ? "Signed in as " + current.getUsername() : "",
                "12px", Theme.MUTED, false);
        VBox titleBox = new VBox(2, dashTitle, whoami);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button securityBtn = UIFactory.outlineBtn("Account Security", Theme.ACCENT);
        Button logoutBtn   = UIFactory.outlineBtn("Logout",           Theme.DANGER);
        securityBtn.setOnAction(e -> AccountSecurityView.show());
        logoutBtn  .setOnAction(e -> { SessionManager.logout(); stage.close(); });

        HBox header = new HBox(10, titleBox, spacer, securityBtn, logoutBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // ── Flight table ──────────────────────────────────────────────────────
        TableView<Flight> table = TableFactory.buildFlightTable(flights);
        table.setItems(flights);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ── Basic info fields ─────────────────────────────────────────────────
        TextField fId   = UIFactory.textField("Flight ID (e.g. SK101)");
        TextField fSrc  = UIFactory.textField("Source city");
        TextField fDest = UIFactory.textField("Destination city");
        TextField fDate = UIFactory.textField("Date (YYYY-MM-DD)");
        TextField fTime = UIFactory.textField("Time (HH:MM, 24h)");

        ComboBox<FlightStatus> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(FlightStatus.values());
        statusBox.setValue(FlightStatus.SCHEDULED);
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(
                "-fx-background-color:#F1F5F9;-fx-border-color:" + Theme.BORDER + ";" +
                        "-fx-border-radius:7;-fx-background-radius:7;-fx-font-size:13px;");

        GridPane basicGrid = new GridPane();
        basicGrid.setHgap(10); basicGrid.setVgap(10);
        ColumnConstraints bc = new ColumnConstraints();
        bc.setHgrow(Priority.ALWAYS); bc.setFillWidth(true);
        basicGrid.getColumnConstraints().addAll(bc, bc, bc);
        basicGrid.addRow(0, fId,   fSrc,   fDest);
        basicGrid.addRow(1, fDate, fTime,  statusBox);

        // ── Cabin tabs (one tab per CabinClass) ───────────────────────────────
        // Each tab has: seats field + price field
        TextField[] cabinSeatFields  = new TextField[4];
        TextField[] cabinPriceFields = new TextField[4];
        CabinClass[] cabins = CabinClass.values();

        TabPane cabinTabs = new TabPane();
        cabinTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        cabinTabs.setStyle("-fx-background-color:" + Theme.BG + ";");

        // Default seat/price hints per cabin
        int[]    defaultSeats  = {120, 40, 20, 8};
        double[] defaultPrices = {150, 225, 375, 600};

        for (int i = 0; i < cabins.length; i++) {
            TextField seatsF = UIFactory.textField("Seats (e.g. " + defaultSeats[i] + ")");
            TextField priceF = UIFactory.textField("Price $ (e.g. " + (int)defaultPrices[i] + ")");
            seatsF.setText(String.valueOf(defaultSeats[i]));
            priceF.setText(String.valueOf((int)defaultPrices[i]));

            cabinSeatFields[i]  = seatsF;
            cabinPriceFields[i] = priceF;

            GridPane cabinGrid = new GridPane();
            cabinGrid.setHgap(10); cabinGrid.setVgap(10);
            cabinGrid.setPadding(new Insets(12));
            ColumnConstraints cc2 = new ColumnConstraints();
            cc2.setHgrow(Priority.ALWAYS); cc2.setFillWidth(true);
            cabinGrid.getColumnConstraints().addAll(cc2, cc2);

            Label seatsLbl = UIFactory.lbl("Available Seats", "12px", Theme.MUTED, false);
            Label priceLbl = UIFactory.lbl("Fare Price ($)",  "12px", Theme.MUTED, false);
            cabinGrid.addRow(0, seatsLbl, priceLbl);
            cabinGrid.addRow(1, seatsF,  priceF);

            // Price hint label showing multiplier vs economy
            String hint = i == 0 ? "Base fare" :
                    i == 1 ? "~1.5× Economy" :
                    i == 2 ? "~2.5× Economy" : "~4× Economy";
            Label hintLbl = UIFactory.lbl(hint, "11px", Theme.MUTED, false);
            VBox tabContent = new VBox(8, cabinGrid, hintLbl);
            tabContent.setPadding(new Insets(4, 0, 0, 0));

            Tab tab = new Tab(cabins[i].getLabel(), tabContent);
            cabinTabs.getTabs().add(tab);
        }

        // ── Validation message ────────────────────────────────────────────────
        Label msg = UIFactory.lbl("", "12px", Theme.SUCCESS, false);
        msg.setWrapText(true);

        // ── Populate form when a row is selected ──────────────────────────────
        table.getSelectionModel().selectedItemProperty().addListener((o, ov, f) -> {
            if (f == null) return;
            fId  .setText(f.getFlightId());
            fSrc .setText(f.getSource());
            fDest.setText(f.getDestination());
            fDate.setText(f.getDate());
            fTime.setText(f.getTime());
            statusBox.setValue(f.getStatus() != null ? f.getStatus() : FlightStatus.SCHEDULED);

            for (int i = 0; i < cabins.length; i++) {
                cabinSeatFields [i].setText(String.valueOf(f.getCabinSeats(cabins[i])));
                cabinPriceFields[i].setText(String.valueOf((int) f.getCabinPrice(cabins[i])));
            }
            msg.setText("");
        });

        // ── Action buttons ────────────────────────────────────────────────────
        Button addBtn  = UIFactory.outlineBtn("Add Flight",    Theme.SUCCESS);
        Button updBtn  = UIFactory.outlineBtn("Update",        Theme.ACCENT);
        Button delBtn  = UIFactory.outlineBtn("Delete Flight", Theme.DANGER);
        Button clrBtn  = UIFactory.outlineBtn("Clear Form",    Theme.MUTED);

        // ── Sort buttons ──────────────────────────────────────────────────────
        Button sortAscBtn  = UIFactory.outlineBtn("⬆ Price: Low → High", Theme.ACCENT);
        Button sortDescBtn = UIFactory.outlineBtn("⬇ Price: High → Low", "#7C3AED");
        sortAscBtn .setOnAction(e -> FXCollections.sort(flights, Comparator.comparingDouble(Flight::getPrice)));
        sortDescBtn.setOnAction(e -> FXCollections.sort(flights, Comparator.comparingDouble(Flight::getPrice).reversed()));

        // ── Helpers ───────────────────────────────────────────────────────────
        Runnable clearForm = () -> {
            UIFactory.clearFields(fId, fSrc, fDest, fDate, fTime);
            statusBox.setValue(FlightStatus.SCHEDULED);
            int[] ds = {120, 40, 20, 8};
            int[] dp = {150, 225, 375, 600};
            for (int i = 0; i < cabins.length; i++) {
                cabinSeatFields [i].setText(String.valueOf(ds[i]));
                cabinPriceFields[i].setText(String.valueOf(dp[i]));
            }
            table.getSelectionModel().clearSelection();
            msg.setText("");
        };

        clrBtn.setOnAction(e -> clearForm.run());

        // Collect all cabin values into String arrays for Validator
        java.util.function.Supplier<String[]> getSeats  = () -> {
            String[] s = new String[4];
            for (int i = 0; i < 4; i++) s[i] = cabinSeatFields[i].getText().trim();
            return s;
        };
        java.util.function.Supplier<String[]> getPrices = () -> {
            String[] p = new String[4];
            for (int i = 0; i < 4; i++) p[i] = cabinPriceFields[i].getText().trim();
            return p;
        };

        // Build a Flight from current form values (assumes validation passed)
        java.util.function.Supplier<Flight> buildFlight = () -> {
            Map<CabinClass, Integer> seats  = new EnumMap<>(CabinClass.class);
            Map<CabinClass, Double>  prices = new EnumMap<>(CabinClass.class);
            for (int i = 0; i < cabins.length; i++) {
                seats .put(cabins[i], Integer.parseInt(cabinSeatFields [i].getText().trim()));
                prices.put(cabins[i], Double .parseDouble(cabinPriceFields[i].getText().trim()));
            }
            return new Flight(
                    fId.getText().trim(), fSrc.getText().trim(), fDest.getText().trim(),
                    fDate.getText().trim(), fTime.getText().trim(),
                    statusBox.getValue(), seats, prices
            );
        };

        // ── Add ───────────────────────────────────────────────────────────────
        addBtn.setOnAction(e -> {
            String err = Validator.validateFlight(
                    fId.getText(), fSrc.getText(), fDest.getText(),
                    fDate.getText(), fTime.getText(), getSeats.get(), getPrices.get());
            if (err != null) { UIFactory.setMsg(msg, Theme.DANGER, "✗  " + err); return; }

            flights.add(buildFlight.get());
            UIFactory.setMsg(msg, Theme.SUCCESS, "✓  Flight added and saved.");
            clearForm.run();
        });

        // ── Update ────────────────────────────────────────────────────────────
        updBtn.setOnAction(e -> {
            int idx = table.getSelectionModel().getSelectedIndex();
            if (idx < 0) { UIFactory.setMsg(msg, Theme.DANGER, "✗  Select a flight to update."); return; }

            String err = Validator.validateFlight(
                    fId.getText(), fSrc.getText(), fDest.getText(),
                    fDate.getText(), fTime.getText(), getSeats.get(), getPrices.get());
            if (err != null) { UIFactory.setMsg(msg, Theme.DANGER, "✗  " + err); return; }

            flights.set(idx, buildFlight.get());
            table.getSelectionModel().select(idx);
            UIFactory.setMsg(msg, Theme.SUCCESS, "✓  Flight updated and saved.");
        });

        // ── Delete ────────────────────────────────────────────────────────────
        delBtn.setOnAction(e -> {
            Flight f = table.getSelectionModel().getSelectedItem();
            if (f == null) { UIFactory.setMsg(msg, Theme.DANGER, "✗  Select a flight to delete."); return; }
            flights.remove(f);
            UIFactory.setMsg(msg, Theme.SUCCESS, "✓  Flight deleted and saved.");
            clearForm.run();
        });

        // ── Layout ────────────────────────────────────────────────────────────
        HBox editBtns = new HBox(10, addBtn, updBtn, delBtn, clrBtn);
        HBox sortBtns = new HBox(10, sortAscBtn, sortDescBtn);

        Label basicHead  = UIFactory.sectionHead("Basic Flight Info");
        Label cabinHead  = UIFactory.sectionHead("Cabin & Fare Management");

        VBox formCard = UIFactory.cardBox(new VBox(14,
                basicHead, basicGrid,
                new Separator(),
                cabinHead,
                cabinTabs,
                new Separator(),
                editBtns,
                new Separator(),
                sortBtns,
                msg
        ));

        VBox content = new VBox(16,
                header, new Separator(),
                UIFactory.sectionHead("Flight List"), table,
                formCard);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color:" + Theme.BG + ";");
        VBox.setVgrow(table, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + Theme.BG + ";-fx-background:" + Theme.BG + ";");

        stage.setTitle("Admin Dashboard");
        stage.setScene(new Scene(scroll, 980, 820));
        stage.show();
    }
}