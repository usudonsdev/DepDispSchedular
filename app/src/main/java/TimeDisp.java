import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List; // 

public class TimeDisp {
    private final DateTimeFormatter timeFormatterWithColon = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter timeFormatterWithoutColon = DateTimeFormatter.ofPattern("HH mm");
    private final Label timeDisplayLabel;
    private int lastMinuteForEvent = -1; // 
    private boolean colonVisible = true;

    // (変更点 1) 
    private final List<CsvRecord> taskList;
    private final AlertDisp alertDisp;
    private final AlertSound alertSound;

    // (変更点 2) 
    public TimeDisp(List<CsvRecord> taskList, AlertDisp alertDisp, AlertSound alertSound) {
        this.taskList = taskList;
        this.alertDisp = alertDisp;
        this.alertSound = alertSound;
        
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
        // (変更点 3) 
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateTime();
            
            // 
            LocalTime now = LocalTime.now();
            int currentMinute = now.getMinute();
            
            if (currentMinute != lastMinuteForEvent) {
                System.out.printf("---- 分が変化しました！現在の時刻: %02d:%02d ----\n", now.getHour(), currentMinute);
                lastMinuteForEvent = currentMinute;
                
                // 
                checkTasks(now.getHour(), currentMinute);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * (変更点 4) 
     * 現在の時・分に一致するタスクがないかチェックする
     * @param currentHour 現在の「時」
     * @param currentMinute 現在の「分」
     */
    private void checkTasks(int currentHour, int currentMinute) {
        if (taskList.isEmpty()) {
            return; // 
        }

        String alertMessage = null;

        for (CsvRecord task : taskList) {
            try {
                int taskHour = Integer.parseInt(task.hour);
                int taskMinute = Integer.parseInt(task.minute);
                
                if (currentHour == taskHour && currentMinute == taskMinute) {
                    System.out.println("★★★ 時間です！ ★★★");
                    
                    if (alertMessage == null) {
                        alertMessage = "時間です: " + task.detail;
                    } else {
                        alertMessage += ", " + task.detail; // 
                    }
                }
            } catch (NumberFormatException e) {
                // CSV
            }
        }

        if (alertMessage != null) {
            // 
            alertDisp.show(alertMessage);
            alertSound.play();
        } else {
            // 
            alertDisp.clear();
        }
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