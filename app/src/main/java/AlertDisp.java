import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * 警告メッセージの表示 (Label) に特化したクラス
 * アニメーション（スライドイン/アウト）機能を持つ
 */
public class AlertDisp {

    private final Label warningLabel;
    
    // (変更点 1) 
    private final TranslateTransition slideTransition;
    private final double slideInPositionX = 0.0; // 
    private final double slideOutPositionX = -250.0; // 
    

    /**
     * コンストラクタ。
     * 警告表示用のLabelを内部で作成・設定する。
     */
    public AlertDisp() {
        this.warningLabel = new Label();
        warningLabel.setText(""); 
        warningLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 60px; -fx-font-weight: bold; -fx-padding: 5;");
        StackPane.setAlignment(warningLabel, Pos.CENTER);
        StackPane.setMargin(warningLabel, new Insets(0, 0, 10, 380));
        
        // (変更点 2) 
        warningLabel.setTranslateX(slideOutPositionX); // 
        warningLabel.setVisible(false); // 

        // (変更点 3) 
        slideTransition = new TranslateTransition(Duration.millis(4000), warningLabel);
        slideTransition.setCycleCount(1);
    }

    /**
     * 警告を表示する。（左からスライドイン）
     * @param message 表示する警告メッセージ
     */
    public void show(String message) {
        // (変更点 4) 
        slideTransition.stop(); // 
        
        warningLabel.setText(message);
        warningLabel.setVisible(true);
        
        slideTransition.setToX(slideInPositionX); // 
        slideTransition.setOnFinished(null); // 
        slideTransition.playFromStart();
    }

    /**
     * 警告表示をクリアする。（左へスライドアウト）
     */
    public void clear() {
        // (変更点 5) 
        slideTransition.stop();
        
        slideTransition.setToX(slideOutPositionX); // 
        // 
        slideTransition.setOnFinished(e -> {
            warningLabel.setVisible(false);
            warningLabel.setText("");
        });
        slideTransition.playFromStart();
    }

    /**
     * このクラスが管理するLabelインスタンスを返す。
     * @return 管理対象のLabel
     */
    public Label getDisplayLabel() {
        return warningLabel;
    }
}