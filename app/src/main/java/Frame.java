import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class Frame {
    private final ImageView imageView;

    public Frame() {
        imageView = new ImageView();
        try {
            // このクラス (Frame.class) を基準にリソースを読み込む
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