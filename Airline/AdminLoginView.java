package Airline;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public final class AdminLoginView {
    private static final String ADMIN_USERNAME = "admin";

    private AdminLoginView() {}

    public static void show(Runnable onSuccess) {
        Stage stage = new Stage();

        Label title = UIFactory.lbl("Admin Login", "20px", Theme.PRIMARY, true);
        Label hint  = UIFactory.lbl("Default password: admin123", "12px", Theme.MUTED, false);

        PasswordField password = UIFactory.passField("Password");
        Label err = UIFactory.lbl("", "12px", Theme.DANGER, false);

        Button loginBtn = UIFactory.filledBtn("Sign In", Theme.PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink forgot = new Hyperlink("Forgot password?");

        Runnable attempt = () -> {
            UserStore.LoginResult result = UserStore.getInstance().login(ADMIN_USERNAME, password.getText());
            switch (result) {
                case SUCCESS:
                    UserStore.getInstance().findByUsername(ADMIN_USERNAME).ifPresent(SessionManager::login);
                    stage.close();
                    onSuccess.run();
                    break;
                case LOCKED:
                    UIFactory.setMsg(err, Theme.DANGER,
                            "Account locked after too many attempts. Use \"Forgot password?\" to reset it.");
                    break;
                default:
                    err.setText("Incorrect password.");
                    password.clear();
            }
        };

        loginBtn.setOnAction(e -> attempt.run());
        password.setOnAction(e -> attempt.run());

        forgot.setOnAction(e -> {
            stage.close();
            PasswordResetView.show(ADMIN_USERNAME, Role.ADMIN, onSuccess);
        });

        VBox inner = new VBox(12, title, hint, password, err, loginBtn, forgot);
        VBox card = UIFactory.cardBox(inner);
        card.setMaxWidth(320);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + Theme.BG + ";");

        stage.setTitle("Admin Login");
        stage.setScene(new Scene(root, 400, 360));
        stage.show();
    }
}