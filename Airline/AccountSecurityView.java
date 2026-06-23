package Airline;


import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class AccountSecurityView {
    private AccountSecurityView() {}

    public static void show() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        Stage stage = new Stage();

        Label title = UIFactory.lbl("Account Security", "20px", Theme.PRIMARY, true);
        Label info = UIFactory.lbl(
                "Username: " + user.getUsername() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Role: " + user.getRole() + "\n" +
                        "Last login: " + user.getLastLogin() + "\n" +
                        "Security question: " + user.getSecurityQuestion(),
                "12px", Theme.MUTED, false);
        info.setWrapText(true);

        PasswordField current = UIFactory.passField("Current password");
        PasswordField newP    = UIFactory.passField("New password");
        Label pwHint = UIFactory.lbl("Use 8+ characters with a mix of letters, numbers & symbols.",
                "11px", Theme.MUTED, false);
        PasswordField confirm = UIFactory.passField("Confirm new password");

        Label err = UIFactory.lbl("", "12px", Theme.DANGER, false);

        Button changeBtn = UIFactory.filledBtn("Update Password", Theme.ACCENT);
        changeBtn.setMaxWidth(Double.MAX_VALUE);
        Button closeBtn = UIFactory.outlineBtn("Close", Theme.MUTED);

        changeBtn.setOnAction(e -> {
            if (!newP.getText().equals(confirm.getText())) {
                UIFactory.setMsg(err, Theme.DANGER, "New passwords do not match.");
                return;
            }
            boolean ok = UserStore.getInstance()
                    .changePassword(user.getUsername(), current.getText(), newP.getText());
            if (ok) {
                UIFactory.setMsg(err, Theme.SUCCESS, "✓  Password updated.");
                current.clear();
                newP.clear();
                confirm.clear();
            } else {
                UIFactory.setMsg(err, Theme.DANGER, "Current password incorrect, or new password too weak.");
            }
        });

        closeBtn.setOnAction(e -> stage.close());

        VBox inner = new VBox(12, title, info, new Separator(),
                UIFactory.sectionHead("Change Password"),
                current, newP, pwHint, confirm, err, changeBtn, closeBtn);
        VBox card = UIFactory.cardBox(inner);
        card.setMaxWidth(380);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + Theme.BG + ";");
        root.setPadding(new Insets(10));

        stage.setTitle("Account Security");
        stage.setScene(new Scene(root, 440, 560));
        stage.show();
    }
}
