import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
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

    private final DateTimeFormatter timeFormatterWithColon = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter timeFormatterWithoutColon = DateTimeFormatter.ofPattern("HH mm"); // スペースでコロンを模倣
    private Label timeDisplayLabel;
    private Label dynamicLabel;
    private int lastMinuteForEvent = -1;
    private boolean colonVisible = true;

    // CSVファイルのレコードを保持するクラス
    private static class CsvRecord {
        String hour;
        String minute;
        String type;
        String detail;
        String remark;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Overlay App");

        // メインコンテナ（垂直方向のレイアウト）
        VBox root = new VBox(5);
        root.setPadding(new Insets(10));

        // テキスト入力欄
        TextField entry = new TextField();
        entry.setPromptText("ここに文字を入力してください...");
        VBox.setVgrow(entry, Priority.NEVER); // 拡大しないように設定

        // ファイル選択ボタン
        Button fileChooserButton = new Button("CSVファイルを選択");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSVファイルを選択");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooserButton.setOnAction(e -> onFileSelected(fileChooser.showOpenDialog(primaryStage)));

        // 画像とラベルを重ねるためのStackPane
        StackPane overlay = createOverlay();

        // ウィジェットをVBoxに追加
        root.getChildren().addAll(entry, fileChooserButton, overlay);

        // シーンとステージの設定
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        // 1秒ごとに時刻を更新するタイマー
        setupSecondTickTimer();
    }

    /**
     * 画像とラベルを重ねて表示するStackPaneを作成する
     * @return 作成されたStackPane
     */
    private StackPane createOverlay() {
        StackPane overlay = new StackPane();

        // 画像の読み込み
        ImageView imageView = null;
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("DepDisp_frame.png")));
            imageView = new ImageView(image);
        } catch (Exception e) {
            System.err.println("エラー: 画像ファイル DepDisp_frame.png を読み込めませんでした");
            // 画像が見つからない場合の代替表示
            Label errorLabel = new Label("画像読み込みエラー");
            errorLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: red;");
            overlay.getChildren().add(errorLabel);
        }

        // dynamic_labelの作成と配置
        dynamicLabel = new Label("Loading System");
        dynamicLabel.getStyleClass().add("dynamic-label");
        StackPane.setAlignment(dynamicLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(dynamicLabel, new Insets(0, 0, 280, 470)); // マージンで位置を調整

        // 時刻表示ラベルの作成と配置
        timeDisplayLabel = new Label();
        timeDisplayLabel.getStyleClass().add("time-display-label");
        StackPane.setAlignment(timeDisplayLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(timeDisplayLabel, new Insets(0, 0, 80, 44));

        if (imageView != null) {
            overlay.getChildren().add(imageView);
        }
        overlay.getChildren().addAll(dynamicLabel, timeDisplayLabel);
        VBox.setVgrow(overlay, Priority.ALWAYS);
        
        return overlay;
    }

    /**
     * 1秒ごとに時刻表示を更新するタイマーを設定する
     */
    private void setupSecondTickTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime now = LocalTime.now();
            int currentMinute = now.getMinute();
            int currentSecond = now.getSecond();

            // 時刻表示（コロン点滅）
            if (colonVisible) {
                timeDisplayLabel.setText(now.format(timeFormatterWithColon));
            } else {
                timeDisplayLabel.setText(now.format(timeFormatterWithoutColon));
            }
            colonVisible = !colonVisible; // コロンの表示・非表示を切り替え

            // 「分」が変化したときのイベント
            if (currentMinute != lastMinuteForEvent) {
                System.out.printf("---- 分が変化しました！現在の時刻: %02d:%02d:%02d ----\n",
                        now.getHour(), currentMinute, currentSecond);
                lastMinuteForEvent = currentMinute;
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * CSVファイルが選択されたときに呼び出される
     * @param file 選択されたファイル
     */
    private void onFileSelected(File file) {
        if (file == null) {
            System.out.println("ファイルが選択されませんでした。");
            dynamicLabel.setText("ファイルが選択されていません。");
            dynamicLabel.setStyle("-fx-text-fill: red;"); // 赤色でエラー表示
            return;
        }

        System.out.println("選択されたファイル: " + file.getAbsolutePath());

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

                    // Pango Markupの代わりにHTMLとCSSを使用
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

            // LabelにHTMLコンテンツを設定
            dynamicLabel.setText("<html>" + displayContent.toString() + "</html>");
            dynamicLabel.setStyle(null); // エラー表示のスタイルをリセット
            System.out.println("CSVデータの表示を更新しました。");

        } catch (IOException e) {
            System.err.println("エラー: ファイル '" + file.getAbsolutePath() + "' を開けませんでした。");
            dynamicLabel.setText("ファイルを開けませんでした。");
            dynamicLabel.setStyle("-fx-text-fill: red;");
        }
    }
}