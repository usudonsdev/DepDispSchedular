import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class DepDispSchedulerApp extends Application {

    private TaskDisp taskDisp; // taskDispのインスタンスを保持
    private TimeDisp timeDisp; // timeDispのインスタンスを保持

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Overlay App");

        primaryStage.setResizable(false);

        VBox root = new VBox(5);
        root.setPadding(new Insets(10));

        TextField entry = new TextField();
        entry.setPromptText("ここに文字を入力してください...");
        VBox.setVgrow(entry, Priority.NEVER);

        Button fileChooserButton = new Button("CSVファイルを選択");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSVファイルを選択");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooserButton.setOnAction(e -> onFileSelected(fileChooser.showOpenDialog(primaryStage)));

        // インナークラスのインスタンス化
        Frame frame = new Frame();
        taskDisp = new TaskDisp();
        timeDisp = new TimeDisp();

        // StackPaneに各要素を重ねて配置
        StackPane overlay = new StackPane();
        overlay.getChildren().addAll(frame.getImageView(), taskDisp.getLabel(), timeDisp.getLabel());
        VBox.setVgrow(overlay, Priority.ALWAYS);

        root.getChildren().addAll(entry, fileChooserButton, overlay);

        Scene scene = new Scene(root, 1500, 550);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        // 時刻表示を開始
        timeDisp.startTick();
    }
    
    // CSVファイル選択時の処理
    private void onFileSelected(File file) {
        if (file == null) {
            System.out.println("ファイルが選択されませんでした。");
            taskDisp.displayError("ファイルが選択されていません。");
            return;
        }
        System.out.println("選択されたファイル: " + file.getAbsolutePath());
        taskDisp.displayCsvContent(file);
    }

    // CSVレコードの構造体（ヘルパークラス）
    private static class CsvRecord {
        String hour;
        String minute;
        String type;
        String detail;
        String remark;
    }

    // --- インナークラスの定義 ---

    // 時刻ラベルを管理するインナークラス
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
            StackPane.setMargin(timeDisplayLabel, new Insets(0, 0, 78, 55));
            updateTime(); // 初期表示
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
    
    // 予定ラベルを管理するインナークラス
    private class TaskDisp {
        private final Label dynamicLabel;

        public TaskDisp() {
            dynamicLabel = new Label("Loading System");
            dynamicLabel.getStyleClass().add("dynamic-label");
            StackPane.setAlignment(dynamicLabel, Pos.BOTTOM_LEFT);
            StackPane.setMargin(dynamicLabel, new Insets(0, 0, 280, 470));
        }

        public Label getLabel() {
            return dynamicLabel;
        }

        public void displayError(String message) {
            dynamicLabel.setText(message);
            dynamicLabel.setStyle("-fx-text-fill: red;");
        }
        
        public void displayCsvContent(File file) {
            StringBuilder displayContent = new StringBuilder();
            AtomicInteger lineCount = new AtomicInteger();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.lines().forEach(line -> {
                    lineCount.getAndIncrement();
                    String[] fields = line.split(",");

                    if (fields.length >= 5) {
                        CsvRecord record = new CsvRecord();
                        record.hour = fields[0].trim();
                        record.minute = fields[1].trim();
                        record.type = fields[2].trim();
                        record.detail = fields[3].trim();
                        record.remark = fields[4].trim();

                        displayContent.append(String.format(
                            "<span class='time-text'>%s:%s </span>" +
                            "<span class='type-text'>%s </span>" +
                            "<span class='detail-text'>%s </span>" +
                            "<span class='remark-text'>%s</span><br>",
                            record.hour, record.minute, record.type, record.detail, record.remark
                        ));
                    } else {
                        displayContent.append(String.format("<span class='warning-text'>行 %d: 試運転</span><br>", lineCount.get()));
                    }
                });

                dynamicLabel.setText("<html>" + displayContent.toString() + "</html>");
                dynamicLabel.setStyle(null); // エラー表示のスタイルをリセット
                System.out.println("CSVデータの表示を更新しました。");

            } catch (IOException e) {
                System.err.println("エラー: ファイル '" + file.getAbsolutePath() + "' を開けませんでした。");
                displayError("ファイルを開けませんでした。");
            }
        }
    }
    
    // 画像フレームを管理するインナークラス
    private class Frame {
        private final ImageView imageView;

        public Frame() {
            imageView = new ImageView();
            try {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("DepDisp_frame.png")));
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("エラー: 画像ファイル DepDisp_frame.png を読み込めませんでした");
                // 画像が見つからない場合の代替表示
                Label errorLabel = new Label("画像読み込みエラー");
                errorLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: red;");
                StackPane.setAlignment(errorLabel, Pos.CENTER);
                imageView.setFitWidth(400); // サイズを調整
                imageView.setFitHeight(300);
            }
        }
        
        public ImageView getImageView() {
            return imageView;
        }
    }
}