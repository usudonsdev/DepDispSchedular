import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DepDispSchedulerApp extends Application {

    private TaskDisp taskDispTop; 
    private TaskDisp taskDispBottom;
    private TimeDisp timeDisp;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Overlay App");
        primaryStage.setResizable(false);
        VBox root = new VBox(5);
        root.setPadding(new Insets(10));

        Button fileChooserButton = new Button("CSVファイルを選択");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSVファイルを選択");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooserButton.setOnAction(e -> onFileSelected(fileChooser.showOpenDialog(primaryStage)));

        Frame frame = new Frame();
        
        // 上下にタスク表示を配置
        // Pos.TOP_CENTERとPos.BOTTOM_CENTERで上下中央に配置
        taskDispTop = new TaskDisp(Pos.BOTTOM_CENTER, new Insets(0, 0, 215, 480), 5); // 下部5行用
        taskDispBottom = new TaskDisp(Pos.TOP_CENTER, new Insets(245, 0, 0, 480), 5); // 上部5行用

        timeDisp = new TimeDisp();

        StackPane overlay = new StackPane();
        overlay.getChildren().addAll(frame.getImageView(), taskDispTop.getContainer(), taskDispBottom.getContainer(), timeDisp.getLabel());
        VBox.setVgrow(overlay, Priority.ALWAYS);

        root.getChildren().addAll(fileChooserButton, overlay);

        Scene scene = new Scene(root, 1500, 550);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        timeDisp.startTick();
    }
    
    private void onFileSelected(File file) {
        if (file == null) {
            System.out.println("ファイルが選択されませんでした。");
            taskDispTop.displayError("ファイルが選択されていません。");
            taskDispBottom.displayError("ファイルが選択されていません。");
            return;
        }
        System.out.println("選択されたファイル: " + file.getAbsolutePath());
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> lines = br.lines().collect(Collectors.toList());
            
            taskDispTop.clear();
            taskDispBottom.clear();

            int totalLines = lines.size();
            int half = totalLines / 2;
            
            // 上部タスク表示
            List<String> topLines = lines.subList(0, Math.min(half, 5));
            taskDispTop.displayCsvContent(topLines);
            
            // 下部タスク表示
            if (totalLines > half) {
                List<String> bottomLines = lines.subList(half, totalLines);
                taskDispBottom.displayCsvContent(bottomLines);
            }
        } catch (IOException e) {
            System.err.println("エラー: ファイル '" + file.getAbsolutePath() + "' を開けませんでした。");
            taskDispTop.displayError("ファイルを開けませんでした。");
        }
    }

    private static class CsvRecord {
        String hour;
        String minute;
        String type;
        String detail;
        String remark;
    }

    private class TimeDisp {
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
    
    private class TaskDisp {
        private final VBox container;
        private final Pos alignment;
        private final Insets margin;
        private final int maxLines;

        public TaskDisp(Pos alignment, Insets margin, int maxLines) {
            this.alignment = alignment;
            this.margin = margin;
            this.maxLines = maxLines;
            // メインコンテナをVBoxにし、タスク行を縦に並べる
            container = new VBox(20);
            container.getStyleClass().add("task-disp-container");
        }

        public VBox getContainer() {
            StackPane.setAlignment(container, alignment);
            StackPane.setMargin(container, margin);
            return container;
        }

        public void clear() {
            container.getChildren().clear();
        }

        public void displayError(String message) {
            container.getChildren().clear();
            Label errorLabel = new Label(message);
            errorLabel.setStyle("-fx-text-fill: red;");
            container.getChildren().add(errorLabel);
        }
        
        public void displayCsvContent(List<String> lines) {
            container.getChildren().clear();

            for (String line : lines) {
                String[] fields = line.split(",");

                if (fields.length >= 5) {
                    CsvRecord record = new CsvRecord();
                    record.hour = fields[0].trim();
                    record.minute = fields[1].trim();
                    record.type = fields[2].trim();
                    record.detail = fields[3].trim();
                    record.remark = fields[4].trim();
                    
                    // 各タスク項目を横一列に並べるHBoxを作成
                    HBox taskItemBox = new HBox(15);
                    taskItemBox.getStyleClass().add("task-item-box");
                    taskItemBox.setAlignment(Pos.CENTER_LEFT);

                    Label timeLabel = new Label(String.format("%s:%s", record.hour, record.minute));
                    timeLabel.getStyleClass().add("time-text");
                    
                    Label typeLabel = new Label(record.type);
                    typeLabel.getStyleClass().add("type-text");

                    Label detailLabel = new Label(record.detail);
                    detailLabel.getStyleClass().add("detail-text");

                    Label remarkLabel = new Label(record.remark);
                    remarkLabel.getStyleClass().add("remark-text");

                    taskItemBox.getChildren().addAll(timeLabel, typeLabel, detailLabel, remarkLabel);
                    container.getChildren().add(taskItemBox);
                } else {
                    Label warningLabel = new Label(String.format("行 %d: 試運転", container.getChildren().size() + 1));
                    warningLabel.setStyle("-fx-text-fill: orange;");
                    container.getChildren().add(warningLabel);
                }
            }
        }
    }
    
    private class Frame {
        private final ImageView imageView;

        public Frame() {
            imageView = new ImageView();
            try {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("DepDisp_frame.png")));
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("エラー: 画像ファイル DepDisp_frame.png を読み込めませんでした");
                Label errorLabel = new Label("画像読み込みエラー");
                errorLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: red;");
                StackPane.setAlignment(errorLabel, Pos.CENTER);
                imageView.setFitWidth(400);
                imageView.setFitHeight(300);
            }
        }
        
        public ImageView getImageView() {
            return imageView;
        }
    }
}
