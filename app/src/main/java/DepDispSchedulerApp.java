import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        primaryStage.setTitle("予定表ディスプレイ");
        primaryStage.setResizable(false);
        VBox root = new VBox(5);
        root.setPadding(new Insets(10));

        Button fileChooserButton = new Button("設定");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("設定");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooserButton.setOnAction(e -> onFileSelected(fileChooser.showOpenDialog(primaryStage)));

        // 各クラスをトップレベルクラスとしてインスタンス化
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

    // CsvRecord, TimeDisp, TaskDisp, Frame の各インナークラス定義はここから削除
}