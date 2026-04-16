import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class LoginController {

    private static final String ADMIN_PASSWORD = "admin123";

    @FXML private TextField     tfStaffName;
    @FXML private PasswordField pfPassword;
    @FXML private Label         lblError;
    @FXML private Button        btnEnter;

    private MainApp mainApp;

    public void setMainApp(MainApp app) {
        this.mainApp = app;

        // Allow Enter key on either field to trigger login
        tfStaffName.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                pfPassword.requestFocus();
            }
        });
        pfPassword.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleEnter();
            }
        });

        tfStaffName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                lblError.setText("");
                resetFieldStyle(tfStaffName);
            }
        });
        pfPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                lblError.setText("");
                resetPasswordStyle();
            }
        });
    }

    @FXML
    private void handleEnter() {
        String name     = tfStaffName.getText().trim();
        String password = pfPassword.getText();

        if (name.isEmpty()) {
            lblError.setText("⚠  Please enter your name.");
            tfStaffName.setStyle(tfStaffName.getStyle() + "-fx-border-color: #e05252;");
            tfStaffName.requestFocus();
            return;
        }

        if (!ADMIN_PASSWORD.equals(password)) {
            lblError.setText("⚠  Incorrect password. Hint: admin123");
            pfPassword.setStyle(pfPassword.getStyle() + "-fx-border-color: #e05252;");
            pfPassword.clear();
            pfPassword.requestFocus();
            return;
        }

        btnEnter.setDisable(true);

        FadeTransition fadeOut = new FadeTransition(
            Duration.millis(500), btnEnter.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (mainApp != null) {
                mainApp.showMainApp();
            }
        });
        fadeOut.play();
    }

    private void resetFieldStyle(TextField tf) {
        tf.setStyle(
            "-fx-background-color: #2a2a2a;"
            + "-fx-text-fill: #f0ead6;"
            + "-fx-prompt-text-fill: #666660;"
            + "-fx-border-color: #383830;"
            + "-fx-border-radius: 4;"
            + "-fx-background-radius: 4;"
            + "-fx-padding: 13 16 13 16;"
            + "-fx-font-size: 14px;"
            + "-fx-font-family: 'Segoe UI';"
        );
    }

    private void resetPasswordStyle() {
        pfPassword.setStyle(
            "-fx-background-color: #2a2a2a;"
            + "-fx-text-fill: #f0ead6;"
            + "-fx-prompt-text-fill: #666660;"
            + "-fx-border-color: #383830;"
            + "-fx-border-radius: 4;"
            + "-fx-background-radius: 4;"
            + "-fx-padding: 13 16 13 16;"
            + "-fx-font-size: 14px;"
            + "-fx-font-family: 'Segoe UI';"
        );
    }
}
