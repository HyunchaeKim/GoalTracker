import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class FocusPane extends VBox {

    private Label timerLabel;
    private Timeline focusTimer;
    private long focusStartMillis = 0;
    private boolean focusing = false;

    private DashboardPane dashboardPane;

    public FocusPane() {
        getChildren().add(createView());
    }

    public void setDashboardPane(DashboardPane dashboardPane) {
        this.dashboardPane = dashboardPane;
    }

    private VBox createView() {
        VBox card = Ui.card();

        Label title = Ui.sectionTitle("집중 제어");

        Label guide = Ui.normalLabel("할 일의 [집중 시작]을 누르면 집중 시간이 기록됩니다. 집중 중 완료를 누르면 자동으로 집중 종료됩니다.");

        timerLabel = new Label("집중 시간: 00:00");
        timerLabel.getStyleClass().add("timer-label");

        Button endButton = Ui.dangerButton("집중 종료");
        endButton.setOnAction(e -> endFocusManually());

        card.getChildren().addAll(title, guide, timerLabel, endButton);

        return card;
    }

    public boolean isFocusing() {
        return focusing;
    }

    //TodoPane에서 집중 시작 시 호출 - 서버에 집중 시작 요청, 타이머 시작, 대시보드 상태 변경
    public void startFocus(Todo todo, DashboardPane dashboardPane) {
        try {
            this.dashboardPane = dashboardPane;

            ServerClient.startFocus();

            focusing = true;
            focusStartMillis = System.currentTimeMillis();

            dashboardPane.setFocusStatus("현재 상태: " + todo.getContent() + " 집중 중");

            startTimer();
            dashboardPane.refresh();

        } catch (Exception e) {
            showError("집중 시작 실패", e.getMessage());
        }
    }

    public String autoEndFocus() throws Exception {
        if (!focusing) return null;

        String response = ServerClient.endFocus();

        focusing = false;
        stopTimer();
        timerLabel.setText("집중 시간: 00:00");

        if (dashboardPane != null) {
            dashboardPane.setRestStatus();
        }

        return Ui.extractValue(response, "exp_added");
    }

    private void endFocusManually() {
        try {
            if (!focusing) {
                showError("집중 상태 아님", "현재 진행 중인 집중이 없습니다.");
                return;
            }

            String expAdded = autoEndFocus();

            if (dashboardPane != null) {
                dashboardPane.refresh();
            }

            if (expAdded != null) {
                showInfo("집중 완료", "집중 시간이 저장되었습니다.\nEXP +" + expAdded);
            }

        } catch (Exception e) {
            showError("집중 종료 실패", e.getMessage());
        }
    }

    private void startTimer() {
        if (focusTimer != null) {
            focusTimer.stop();
        }

        focusTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );

        focusTimer.setCycleCount(Animation.INDEFINITE);
        focusTimer.play();

        timerLabel.setText("집중 시간: 00:00");
    }

    private void stopTimer() {
        if (focusTimer != null) {
            focusTimer.stop();
        }

        focusTimer = null;
        focusStartMillis = 0;
    }

    private void updateTimer() {
        if (focusStartMillis == 0) return;

        long elapsedSeconds = (System.currentTimeMillis() - focusStartMillis) / 1000;
        timerLabel.setText("집중 시간: " + Ui.formatSeconds(elapsedSeconds));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "오류가 발생했습니다." : message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}