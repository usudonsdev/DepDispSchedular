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
import java.util.ArrayList; // 
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DepDispSchedulerApp extends Application {

    private TaskDisp taskDispTop;
    private TaskDisp taskDispBottom;
    private TimeDisp timeDisp;

    private AlertDisp alertDisp;
    private AlertSound alertSound;

    // (変更点 1) 
    private final List<CsvRecord> taskList = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("予定表ディスプレイ");
        primaryStage.setResizable(false);
        
        // (修正点 1) 
        VBox root = new VBox(5);
        root.setPadding(new Insets(10));

        // (修正点 2) 
        Button fileChooserButton = new Button("設定");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("設定");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        // onFileSelected が呼び出されるように設定
        fileChooserButton.setOnAction(e -> onFileSelected(fileChooser.showOpenDialog(primaryStage)));

        // --- ここから下はあなたのコードと合っています ---
        alertDisp = new AlertDisp();
        alertSound = new AlertSound("alert.wav");

        Frame frame = new Frame();

        taskDispTop = new TaskDisp(Pos.BOTTOM_CENTER, new Insets(0, 0, 215, 480), 5);
        taskDispBottom = new TaskDisp(Pos.TOP_CENTER, new Insets(245, 0, 0, 480), 5);

        timeDisp = new TimeDisp(taskList, alertDisp, alertSound);

        StackPane overlay = new StackPane();
        overlay.getChildren().addAll(
            frame.getImageView(), 
            taskDispTop.getContainer(), 
            taskDispBottom.getContainer(), 
            timeDisp.getLabel(),
            alertDisp.getDisplayLabel()
        );
        VBox.setVgrow(overlay, Priority.ALWAYS);
        // --- ここまで ---

        // (修正点 3) 
        root.getChildren().addAll(fileChooserButton, overlay);

        // (修正点 4) 
        Scene scene = new Scene(root, 1500, 550);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        timeDisp.startTick();
    }

    private void onFileSelected(File file) {
        if (file == null) {
            System.out.println("ファイルが選択されませんでした。");
            String msg = "ファイルが選択されていません。";
            taskDispTop.displayError(msg);
            taskDispBottom.displayError(msg);
            // (変更点 3) 
            // alertDisp.show(msg); 
            // alertSound.play(); 
            return;
        }
        System.out.println("選択されたファイル: " + file.getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> lines = br.lines().collect(Collectors.toList());

            // (変更点 4) 
            taskList.clear(); 
            for (String line : lines) {
                String[] fields = line.split(",");
                if (fields.length >= 5) {
                    CsvRecord record = new CsvRecord();
                    record.hour = fields[0].trim();
                    record.minute = fields[1].trim();
                    record.type = fields[2].trim();
                    record.detail = fields[3].trim();
                    record.remark = fields[4].trim();
                    taskList.add(record);
                }
            }

            taskDispTop.clear();
            taskDispBottom.clear();
            alertDisp.clear(); 

            // (変更点 5) 
            int totalTasks = taskList.size();
            int half = totalTasks / 2;
            
            List<CsvRecord> topTasks = taskList.subList(0, Math.min(half, 5));
            taskDispTop.displayCsvContent(topTasks); 

            if (totalTasks > half) {
                List<CsvRecord> bottomTasks = taskList.subList(half, Math.min(half + 5, totalTasks));
                taskDispBottom.displayCsvContent(bottomTasks);
            }
        } catch (IOException e) {
            System.err.println("エラー: ファイル '" + file.getAbsolutePath() + "' を開けませんでした。");
            String msg = "ファイル読み込みエラー";
            taskDispTop.displayError(msg);
            taskDispBottom.displayError(msg);
            // (変更点 6) 
            // alertDisp.show(msg); 
            // alertSound.play(); 
        }
    }
}