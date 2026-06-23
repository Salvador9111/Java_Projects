package Airline;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


public final class UIFactory {
    private UIFactory() {}

    public static VBox cardBox(VBox inner) {
        inner.setStyle(
                "-fx-background-color:" + Theme.CARD + ";" +
                        "-fx-background-radius:12;" +
                        "-fx-padding:20;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),14,0,0,4);"
        );
        return inner;
    }

    public static TextField textField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(
                "-fx-background-color:#F1F5F9;" +
                        "-fx-border-color:" + Theme.BORDER + ";" +
                        "-fx-border-radius:7;" +
                        "-fx-background-radius:7;" +
                        "-fx-padding:9 12;" +
                        "-fx-font-size:13px;" +
                        "-fx-text-fill:" + Theme.PRIMARY + ";"
        );
        return f;
    }

    public static PasswordField passField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(
                "-fx-background-color:#F1F5F9;" +
                        "-fx-border-color:" + Theme.BORDER + ";" +
                        "-fx-border-radius:7;" +
                        "-fx-background-radius:7;" +
                        "-fx-padding:9 12;" +
                        "-fx-font-size:13px;"
        );
        return f;
    }

    public static Button filledBtn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color:" + bg + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:10 22;" +
                        "-fx-cursor:hand;"
        );
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e  -> b.setOpacity(1.0));
        return b;
    }

    public static Button outlineBtn(String text, String color) {
        String base =
                "-fx-background-color:" + color + "22;" +
                        "-fx-text-fill:" + color + ";" +
                        "-fx-font-size:12px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:7;" +
                        "-fx-border-color:" + color + "66;" +
                        "-fx-border-radius:7;" +
                        "-fx-padding:8 18;" +
                        "-fx-cursor:hand;";
        String hover =
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:12px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:7;" +
                        "-fx-border-color:" + color + ";" +
                        "-fx-border-radius:7;" +
                        "-fx-padding:8 18;" +
                        "-fx-cursor:hand;";
        Button b = new Button(text);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(base));
        return b;
    }

    public static Label sectionHead(String text) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-font-size:15px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:" + Theme.PRIMARY + ";"
        );
        return l;
    }

    public static Label lbl(String text, String size, String color, boolean bold) {
        Label l = new Label(text);
        l.setStyle(styleOf(size, color, bold));
        return l;
    }

    public static String styleOf(String size, String color, boolean bold) {
        return "-fx-font-size:" + size + ";" +
                "-fx-text-fill:" + color + ";" +
                (bold ? "-fx-font-weight:bold;" : "");
    }

    public static void setMsg(Label l, String color, String text) {
        l.setStyle(styleOf("12px", color, false));
        l.setText(text);
    }

    public static void clearFields(TextField... fields) {
        for (TextField f : fields) f.clear();
    }
}