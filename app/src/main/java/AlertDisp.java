import javafx.animation.FadeTransition;
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
    
    private final TranslateTransition slideTransition;
    private final double slideInPositionX = 0.0; 
    private final double slideOutPositionX = -250.0; // 

    private final FadeTransition blinkTransition; // 点滅用アニメーション

    /**
     * コンストラクタ。
     * 警告表示用のLabelを内部で作成・設定する。
     */
    public AlertDisp() {
        this.warningLabel = new Label();
        warningLabel.setText(""); 
        // 
        warningLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 60px; -fx-font-weight: bold; -fx-padding: 5;");
        StackPane.setAlignment(warningLabel, Pos.CENTER);
        StackPane.setMargin(warningLabel, new Insets(0, 0, 10, 380));
        
        warningLabel.setTranslateX(slideOutPositionX); 
        warningLabel.setVisible(false); 

        // 
        slideTransition = new TranslateTransition(Duration.millis(2000), warningLabel);
        slideTransition.setCycleCount(1);

        // (変更点 1) 
        blinkTransition = new FadeTransition(Duration.millis(250), warningLabel); // 0.25秒で消灯
        blinkTransition.setFromValue(1.0); // 
        blinkTransition.setToValue(0.1);   // 
        blinkTransition.setCycleCount(8);  // 4回 (消灯→点灯→消灯→点灯) = 2回点滅
        blinkTransition.setAutoReverse(true); // 
        
        // (変更点 1) 
        blinkTransition.setOnFinished(e -> {
            clear(); // 
        });
    }

    /**
     * 警告を表示する。（左からスライドインし、2回点滅）
     * @param message 表示する警告メッセージ
     */
    public void show(String message) {
        // (変更点 2) 
        slideTransition.stop();
        blinkTransition.stop(); // 
        warningLabel.setOpacity(1.0); // 
        
        warningLabel.setText(message);
        warningLabel.setVisible(true);
        
        slideTransition.setToX(slideInPositionX); 
        
        // (変更点 3) 
        slideTransition.setOnFinished(e -> {
            blinkTransition.playFromStart(); // 
        }); 
        slideTransition.playFromStart();
    }

    /**
     * 警告表示をクリアする。（左へスライドアウト）
     */
    public void clear() {
        // (変更点 2) 
        slideTransition.stop();
        blinkTransition.stop(); 
        
        // (変更点 3) 
        warningLabel.setVisible(false);
        warningLabel.setText("");
        warningLabel.setOpacity(1.0); // 
        warningLabel.setTranslateX(slideOutPositionX); // 
        
        // (変更点 4) 
        // slideTransition.setToX(slideOutPositionX); 
        // slideTransition.setOnFinished(...);
        // slideTransition.playFromStart();
    }

    /**
     * このクラスが管理するLabelインスタンスを返す。
     * @return 管理対象のLabel
     */
    
    public Label getDisplayLabel() {
        return warningLabel;
    }
}