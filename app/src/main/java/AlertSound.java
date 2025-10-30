import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.Objects;

/**
 * 警告音の読み込みと再生に特化したクラス
 */
public class AlertSound {

    private final AudioClip alertSound;

    /**
     * コンストラクタ。
     * @param soundFileName リソースフォルダ内の音声ファイル名 (例: "alert.wav")
     */
    public AlertSound(String soundFileName) {
        AudioClip sound = null;
        try {
            URL resource = getClass().getResource(soundFileName);
            Objects.requireNonNull(resource, "サウンドファイルが見つかりません: " + soundFileName);
            sound = new AudioClip(resource.toExternalForm());
        } catch (Exception e) {
            System.err.println("エラー: サウンドファイルの読み込みに失敗しました。 " + e.getMessage());
        }
        this.alertSound = sound;
    }

    /**
     * 読み込んだ警告音を再生する。
     */
    public void play() {
        if (alertSound != null) {
            alertSound.play();
        }
    }
}