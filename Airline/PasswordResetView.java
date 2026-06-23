package Airline;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public final class PasswordResetView {
    private PasswordResetView() {}

    /**
     * @param prefillUsername username to pre-fill (may be empty/null)
     * @param role            the role the account must have (keeps admin/passenger resets separate)
     * @param onDone          called after a successful reset, once the person signs back in
     */
    public static void show(String prefillUsername, Role role, Runnable onDone) {
        Stage stage = new Stage();

        Label title = UIFactory.lbl("Reset Password", "20px", Theme.PRIMARY, true);

        TextField username = UIFactory.textField("Username");
        username.setText(prefillUsername == null ? "" : prefillUsername);
        Button findBtn = UIFactory.filledBtn("Find Account", Theme.ACCENT);
        findBtn.setMaxWidth(Double.MAX_VALUE);

        Label err = UIFactory.lbl("", "12px", Theme.DANGER, false);

        Label questionLbl = UIFactory.lbl("", "13px", Theme.PRIMARY, true);
        TextField answer = UIFactory.textField("Your answer");
        PasswordField newPass = UIFactory.passField("New password");
        Label pwHint = UIFactory.lbl("Use 8+ characters with a mix of letters, numbers & symbols.",
                "11px", Theme.MUTED, false);
        PasswordField confirmPass = UIFactory.passField("Confirm new password");
        Button resetBtn = UIFactory.filledBtn("Reset Password", Theme.SUCCESS);
        resetBtn.setMaxWidth(Double.MAX_VALUE);

        VBox step2 = new VBox(10, questionLbl, answer, newPass, pwHint, confirmPass, resetBtn);
        step2.setVisible(false);
        step2.setManaged(false);

        final String[] verifiedUsername = new String[1];

        findBtn.setOnAction(e -> {
            String u = username.getText().trim();
            Optional<User> found = UserStore.getInstance().findByUsername(u);
            if (!found.isPresent() || found.get().getRole() != role) {
                UIFactory.setMsg(err, Theme.DANGER, "No account found with that username.");
                step2.setVisible(false);
                step2.setManaged(false);
                return;
            }
            questionLbl.setText(found.get().getSecurityQuestion());
            verifiedUsername[0] = u;
            err.setText("");
            step2.setVisible(true);
            step2.setManaged(true);
        });

        resetBtn.setOnAction(e -> {
            String u = verifiedUsername[0];
            if (u == null) {
                UIFactory.setMsg(err, Theme.DANGER, "Please find your account first.");
                return;
            }
            if (!UserStore.getInstance().verifySecurityAnswer(u, answer.getText())) {
                UIFactory.setMsg(err, Theme.DANGER, "Security answer is incorrect.");
                return;
            }
            if (!newPass.getText().equals(confirmPass.getText())) {
                UIFactory.setMsg(err, Theme.DANGER, "Passwords do not match.");
                return;
            }
            boolean ok = UserStore.getInstance().resetPassword(u, newPass.getText());
            if (!ok) {
                UIFactory.setMsg(err, Theme.DANGER, "Password too weak. Use 8+ chars, mixed case, numbers/symbols.");
                return;
            }
            stage.close();
            LoginView.show(role, u, onDone);
        });

        VBox inner = new VBox(14, title, username, findBtn, err, step2);
        VBox card = UIFactory.cardBox(inner);
        card.setMaxWidth(360);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + Theme.BG + ";");
        root.setPadding(new Insets(10));

        stage.setTitle("Reset Password");
        stage.setScene(new Scene(root, 420, 520));
        stage.show();
    }
}