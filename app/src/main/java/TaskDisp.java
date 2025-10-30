import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * タスク一覧（CSVの内容）を表示するUIコンポーネント。
 * VBoxコンテナを管理し、CsvRecordのリストを行に変換して表示する。
 */
public class TaskDisp {
    private final VBox container;
    private final Pos alignment;
    private final Insets margin;
    private final int maxLines;

    /**
     * コンストラクタ。
     * @param alignment StackPane内での配置
     * @param margin StackPane内でのマージン
     * @param maxLines 表示する最大行数 (現在は表示ロジックで直接使用されていないが、将来の拡張用)
     */
    public TaskDisp(Pos alignment, Insets margin, int maxLines) {
        this.alignment = alignment;
        this.margin = margin;
        this.maxLines = maxLines;
        // メインコンテナをVBoxにし、タスク行を縦に並べる
        container = new VBox(20);
        container.getStyleClass().add("task-disp-container");
    }

    /**
     * このコンポーネントのメインコンテナ(VBox)を返す。
     * DepDispSchedulerAppがStackPaneに追加するために使用する。
     * @return VBoxコンテナ
     */
    public VBox getContainer() {
        StackPane.setAlignment(container, alignment);
        StackPane.setMargin(container, margin);
        return container;
    }

    /**
     * コンテナ内の表示をすべてクリアする。
     */
    public void clear() {
        container.getChildren().clear();
    }

    /**
     * エラーメッセージ（赤文字）を表示する。
     * @param message 表示するエラーメッセージ
     */
    public void displayError(String message) {
        container.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        container.getChildren().add(errorLabel);
    }
    
    /**
     * CsvRecordのリストを受け取り、タスク一覧として表示する。
     * @param records 表示するCsvRecordのリスト
     */
    public void displayCsvContent(List<CsvRecord> records) {
        container.getChildren().clear();

        // CsvRecordのリストを反復処理
        for (CsvRecord record : records) {
            
            // 各タスク項目を横一列に並べるHBoxを作成
            HBox taskItemBox = new HBox(15);
            taskItemBox.getStyleClass().add("task-item-box");
            taskItemBox.setAlignment(Pos.CENTER_LEFT);

            // CsvRecordから直接データを取得してLabelを作成
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
        }
    }
}