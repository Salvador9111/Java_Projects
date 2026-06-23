package Airline;


import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class RegisterView {
    private RegisterView() {}

    private static final String[] SECURITY_QUESTIONS = {
            "What was the name of your first pet?",
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What was the model of your first car?",
            "What is the name of your favorite teacher?"
    };

    /**
     * Shows the registration form for a new PASSENGER account.
     *
     * @param onSuccess called once the passenger is registered AND signed in,
     *                   so the caller can open the passenger dashboard directly.
     */
    public static void show(Runnable onSuccess) {
        Stage stage = new Stage();

        Label title = UIFactory.lbl("Create Account", "20px", Theme.PRIMARY, true);
        Label sub   = UIFactory.lbl("Register as a passenger", "12px", Theme.MUTED, false);

        TextField username       = UIFactory.textField("Username");
        TextField email          = UIFactory.textField("Email");
        PasswordField password   = UIFactory.passField("Password");
        Label pwHint = UIFactory.lbl(
                "Use 8+ characters with a mix of letters, numbers & symbols.",
                "11px", Theme.MUTED, false);
        PasswordField confirm    = UIFactory.passField("Confirm Password");

        ComboBox<String> question = new ComboBox<>();
        question.getItems().addAll(SECURITY_QUESTIONS);
        question.setPromptText("Choose a security question");
        question.setMaxWidth(Double.MAX_VALUE);

        TextField answer = UIFactory.textField("Your answer (used for password resets)");

        Label err = UIFactory.lbl("", "12px", Theme.DANGER, false);

        Button registerBtn = UIFactory.filledBtn("Register", Theme.ACCENT);
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink backToLogin = new Hyperlink("Already have an account? Sign in");

        registerBtn.setOnAction(e -> {
            String u  = username.getText().trim();
            String em = email.getText().trim();
            String p  = password.getText();
            String c  = confirm.getText();
            String q  = question.getValue();
            String a  = answer.getText();

            if (u.isEmpty() || em.isEmpty()) {
                UIFactory.setMsg(err, Theme.DANGER, "Username and email are required.");
                return;
            }
            if (q == null) {
                UIFactory.setMsg(err, Theme.DANGER, "Please choose a security question.");
                return;
            }
            if (!p.equals(c)) {
                UIFactory.setMsg(err, Theme.DANGER, "Passwords do not match.");
                return;
            }

            UserStore.RegistrationResult result =
                    UserStore.getInstance().register(u, em, p, Role.PASSENGER, q, a);

            switch (result) {
                case SUCCESS:
                    stage.close();
                    // Registration succeeded — go straight to the login screen,
                    // pre-filled, so the new passenger confirms their password once.
                    LoginView.show(Role.PASSENGER, u, onSuccess);
                    break;
                case USERNAME_TAKEN:
                    UIFactory.setMsg(err, Theme.DANGER, "That username is already taken.");
                    break;
                case WEAK_PASSWORD:
                    UIFactory.setMsg(err, Theme.DANGER, "Password is too weak. Use 8+ chars, mixed case, numbers/symbols.");
                    break;
                case MISSING_SECURITY_ANSWER:
                    UIFactory.setMsg(err, Theme.DANGER, "Please answer the security question.");
                    break;
                default:
                    UIFactory.setMsg(err, Theme.DANGER, "Could not register. Check your details.");
            }
        });

        backToLogin.setOnAction(e -> {
            stage.close();
            LoginView.show(Role.PASSENGER, onSuccess);
        });

        VBox inner = new VBox(10, title, sub, username, email, password, pwHint, confirm,
                question, answer, err, registerBtn, backToLogin);
        VBox card = UIFactory.cardBox(inner);
        card.setMaxWidth(360);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + Theme.BG + ";");
        root.setPadding(new Insets(10));

        stage.setTitle("Register");
        stage.setScene(new Scene(root, 440, 640));
        stage.show();
    }
}
