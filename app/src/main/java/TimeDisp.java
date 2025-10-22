import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeDisp {
    private final DateTimeFormatter timeFormatterWithColon = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter timeFormatterWithoutColon = DateTimeFormatter.ofPattern("HH mm");
    private final Label timeDisplayLabel;
    private int lastMinuteForEvent = -1;
    private boolean colonVisible = true;

    public TimeDisp() {
        timeDisplayLabel = new Label();
        timeDisplayLabel.getStyleClass().add("time-display-label");
        StackPane.setAlignment(timeDisplayLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(timeDisplayLabel, new Insets(0, 0, 80, 56));
        updateTime();
    }

    public Label getLabel() {
        return timeDisplayLabel;
    }

    public void startTick() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateTime();
            LocalTime now = LocalTime.now();
            int currentMinute = now.getMinute();
            if (currentMinute != lastMinuteForEvent) {
                System.out.printf("---- 分が変化しました！現在の時刻: %02d:%02d ----\n", now.getHour(), currentMinute);
                lastMinuteForEvent = currentMinute;
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTime() {
        LocalTime now = LocalTime.now();
        if (colonVisible) {
            timeDisplayLabel.setText(now.format(timeFormatterWithColon));
        } else {
            timeDisplayLabel.setText(now.format(timeFormatterWithoutColon));
        }
        colonVisible = !colonVisible;
    }
}
