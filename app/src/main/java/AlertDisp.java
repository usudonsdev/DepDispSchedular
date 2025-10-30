import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * 警告メッセージの表示 (Label) に特化したクラス
 */
public class AlertDisp {

    private final Label warningLabel;

    /**
     * コンストラクタ。
     * 警告表示用のLabelを内部で作成・設定する。
     */
    public AlertDisp() {
        this.warningLabel = new Label();
        warningLabel.setText(""); // 
        // 
        warningLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 40px; -fx-font-weight: bold; -fx-padding: 5;");
        // -fx-background-color: rgba(0,0,0,0.5); 
        StackPane.setAlignment(warningLabel, Pos.CENTER);
        // 
        StackPane.setMargin(warningLabel, new Insets(0, 0, 0, 480));
    }

    /**
     * 警告を表示する。
     * @param message 表示する警告メッセージ
     */
    public void show(String message) {
        warningLabel.setText(message);
    }

    /**
     * 警告表示をクリアする（メッセージを空にする）。
     */
    public void clear() {
        warningLabel.setText("");
    }

    /**
     * このクラスが管理するLabelインスタンスを返す。
     * メインアプリがStackPaneに追加するために使用する。
     * @return 管理対象のLabel
     */
    public Label getDisplayLabel() {
        return warningLabel;
    }
}