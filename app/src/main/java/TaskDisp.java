import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class TaskDisp {
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
                CsvRecord record = new CsvRecord(); // CsvRecordクラスをインスタンス化
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