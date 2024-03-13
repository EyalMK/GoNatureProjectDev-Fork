package CommonUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ConfirmationPopup extends BasePopup {
    private Button yesButton = new Button();
    private Button noButton = new Button();


    public ConfirmationPopup(String question, Runnable onConfirm, Runnable onCancel, int width, int height, boolean fullScreenMode, String FirstBtn, String SecondBtn, boolean exitOnOut) {
        super(fullScreenMode, width, height);
        yesButton.setText(FirstBtn);
        noButton.setText(SecondBtn);
        Label questionLabel = new Label(question);
        popup.getChildren().addAll(questionLabel, yesButton, noButton);

        yesButton.setOnAction(e -> {
            onConfirm.run();
            closePopup();
        });

        noButton.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
            closePopup();
        });
        if (exitOnOut) {
            modalLayer.setOnMouseClicked(e -> {
                if (!popup.getBoundsInParent().contains(e.getSceneX(), e.getSceneY())) {
                    closePopup();
                }
            });
        }
    }

    public ConfirmationPopup(String question, Runnable onConfirm, int width, int height, boolean fullScreenMode, String FirstBtn, boolean exitOnOut) {
        super(fullScreenMode, width, height);
        yesButton.setText(FirstBtn);
        Label questionLabel = new Label(question);
        popup.getChildren().addAll(questionLabel, yesButton, noButton);

        yesButton.setOnAction(e -> {
            onConfirm.run();
            closePopup();
        });
        if (exitOnOut) {
            modalLayer.setOnMouseClicked(e -> {
                if (!popup.getBoundsInParent().contains(e.getSceneX(), e.getSceneY())) {
                    closePopup();
                }
            });
        }
    }
}
