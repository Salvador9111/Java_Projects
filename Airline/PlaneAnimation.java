package Airline;


import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;


public final class PlaneAnimation {
    private PlaneAnimation() {}

    public static void play(Stage primaryStage, Runnable onFinished) {

        Scene  mainScene = primaryStage.getScene();
        double W = mainScene.getWidth();
        double H = mainScene.getHeight();

        // ── Sky gradient overlay ───────────────────────────────────────────────
        StackPane overlay = new StackPane();
        overlay.setPrefSize(W, H);
        overlay.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0EA5E9 0%, #BAE6FD 60%, #E0F2FE 100%);"
        );
        overlay.setOpacity(0);

        // ── Clouds (simple static labels for depth) ────────────────────────────
        Label cloud1 = cloudLabel("☁", "28px", 0.55);
        Label cloud2 = cloudLabel("☁", "18px", 0.35);
        Label cloud3 = cloudLabel("☁", "22px", 0.45);
        cloud1.setTranslateX(W * 0.15);  cloud1.setTranslateY(H * 0.28);
        cloud2.setTranslateX(W * 0.55);  cloud2.setTranslateY(H * 0.18);
        cloud3.setTranslateX(W * 0.78);  cloud3.setTranslateY(H * 0.38);

        // ── Destination label ──────────────────────────────────────────────────
        Label destLbl = new Label("Preparing for departure…");
        destLbl.setStyle(
                "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:white;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.4),6,0,0,2);"
        );
        destLbl.setTranslateY(H * 0.18);
        destLbl.setOpacity(0);

        // ── Plane ─────────────────────────────────────────────────────────────
        Label plane = new Label("✈");
        plane.setStyle(
                "-fx-font-size:52px;" +
                        "-fx-text-fill:white;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.35),10,0,0,3);"
        );
        // start fully off-screen left
        plane.setTranslateX(-W * 0.15);
        plane.setTranslateY(H * 0.06);

        overlay.getChildren().addAll(cloud1, cloud2, cloud3, destLbl, plane);

        // ── Inject overlay on top of existing scene graph ─────────────────────
        StackPane root = (StackPane) mainScene.getRoot();
        root.getChildren().add(overlay);

        // ── 1. Fade-in the sky (200 ms) ────────────────────────────────────────
        FadeTransition skyIn = new FadeTransition(Duration.millis(200), overlay);
        skyIn.setFromValue(0); skyIn.setToValue(1);

        // ── 2. Plane flies left → right (1 100 ms, smooth ease-both) ──────────
        TranslateTransition fly = new TranslateTransition(Duration.millis(1100), plane);
        fly.setFromX(-W * 0.15);
        fly.setToX(W * 1.12);           // land fully off-screen right
        fly.setInterpolator(Interpolator.EASE_BOTH);

        // ── 3. Subtle vertical bob during flight (pure sine feel via EASE_BOTH) -
        TranslateTransition bob = new TranslateTransition(Duration.millis(1100), plane);
        bob.setFromY(H * 0.06);
        bob.setToY(H * 0.04);
        bob.setInterpolator(Interpolator.EASE_BOTH);

        // ── 4. Destination label fades in after sky appears ────────────────────
        FadeTransition lblIn = new FadeTransition(Duration.millis(500), destLbl);
        lblIn.setFromValue(0); lblIn.setToValue(1);
        lblIn.setDelay(Duration.millis(220));

        // ── 5. Fade-out entire overlay (300 ms) ───────────────────────────────
        FadeTransition skyOut = new FadeTransition(Duration.millis(300), overlay);
        skyOut.setFromValue(1); skyOut.setToValue(0);

        // ── Chain: skyIn → fly+bob+lblIn together → skyOut → open panel ────────
        ParallelTransition flight = new ParallelTransition(fly, bob, lblIn);

        skyIn.setOnFinished(ev -> {
            flight.setOnFinished(ev2 -> {
                skyOut.setOnFinished(ev3 -> {
                    root.getChildren().remove(overlay);
                    onFinished.run();                   // open the actual panel
                });
                skyOut.play();
            });
            flight.play();
        });

        skyIn.play();
    }

    /** Helper to create a semi-transparent cloud label. */
    private static Label cloudLabel(String text, String size, double opacity) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "; -fx-text-fill:white;");
        l.setOpacity(opacity);
        return l;
    }
}