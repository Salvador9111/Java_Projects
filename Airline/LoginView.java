package Airline;


import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public final class LoginView {
    private LoginView() {}

    public static void show(Role role, Runnable onSuccess) {
        show(role, "", onSuccess);
    }

    public static void show(Role role, String prefillUsername, Runnable onSuccess) {
        Stage stage = new Stage();

        String roleLabel = role == Role.ADMIN ? "Admin" : "Passenger";
        Label title = UIFactory.lbl(roleLabel + " Login", "20px", Theme.PRIMARY, true);

        TextField username = UIFactory.textField("Username");
        username.setText(prefillUsername == null ? "" : prefillUsername);
        PasswordField password = UIFactory.passField("Password");

        Label err = UIFactory.lbl("", "12px", Theme.DANGER, false);

        Button loginBtn = UIFactory.filledBtn("Sign In", Theme.PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink forgot   = new Hyperlink("Forgot your password?");
        Hyperlink register = new Hyperlink("New passenger? Create an account");
        if (role == Role.ADMIN) {
            register.setVisible(false);
            register.setManaged(false);
        }

        Runnable attempt = () -> {
            String u = username.getText().trim();
            String p = password.getText();

            // Check the role up front (without revealing whether the username
            // exists) so a passenger account can't be used on the admin screen
            // or vice versa.
            Optional<User> existing = UserStore.getInstance().findByUsername(u);
            if (existing.isPresent() && existing.get().getRole() != role) {
                UIFactory.setMsg(err, Theme.DANGER, "Incorrect username or password.");
                password.clear();
                return;
            }

            UserStore.LoginResult result = UserStore.getInstance().login(u, p);
            switch (result) {
                case SUCCESS:
                    existing.ifPresent(SessionManager::login);
                    stage.close();
                    onSuccess.run();
                    break;
                case LOCKED:
                    UIFactory.setMsg(err, Theme.DANGER,
                            "Account locked after too many failed attempts. Reset your password to unlock it.");
                    break;
                default:
                    UIFactory.setMsg(err, Theme.DANGER, "Incorrect username or password.");
                    password.clear();
            }
        };

        loginBtn.setOnAction(e -> attempt.run());
        password.setOnAction(e -> attempt.run());

        forgot.setOnAction(e -> {
            stage.close();
            PasswordResetView.show(username.getText().trim(), role, onSuccess);
        });

        register.setOnAction(e -> {
            stage.close();
            RegisterView.show(onSuccess);
        });

        VBox inner = new VBox(12, title, username, password, err, loginBtn, forgot, register);
        VBox card = UIFactory.cardBox(inner);
        card.setMaxWidth(340);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + Theme.BG + ";");

        stage.setTitle(roleLabel + " Login");
        stage.setScene(new Scene(root, 420, role == Role.ADMIN ? 320 : 380));
        stage.show();
    }
}