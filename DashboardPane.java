import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class DashboardPane extends VBox {

    private Label statusLabel;
    private Label todayFocusLabel;
    private Label totalFocusLabel;
    private Label levelLabel;
    private Label levelTitleLabel;
    private Label expLabel;
    private Label expToNextLabel;
    private Label streakLabel;
    private Label completedCountLabel;
    private ProgressBar expBar;

    private boolean focusing = false;
    private String focusText = "현재 상태: 휴식 중";

    public DashboardPane() {
        getChildren().add(createView());
    }

    private VBox createView() {
        VBox card = Ui.card();

        Label title = Ui.sectionTitle("내 성장 현황");

        statusLabel     = Ui.normalLabel("현재 상태: 휴식 중");
        todayFocusLabel = Ui.normalLabel("오늘 집중 시간: 00:00");
        totalFocusLabel = Ui.normalLabel("누적 집중 시간: 00:00");
        levelLabel      = Ui.normalLabel("Lv. 1");
        levelTitleLabel = Ui.normalLabel(GoalCalculator.getLevelTitle(1));
        expLabel        = Ui.normalLabel("EXP 0 / 100");
        expToNextLabel  = Ui.normalLabel("다음 레벨까지 100 EXP");
        streakLabel     = Ui.normalLabel("🔥 연속 달성: 0일");
        completedCountLabel = Ui.normalLabel("✅ 완료한 목표: 0개");

        expBar = new ProgressBar(0);
        expBar.setPrefWidth(420);
        expBar.getStyleClass().add("exp-bar");

        Button refreshButton = Ui.grayButton("새로고침");
        refreshButton.setOnAction(e -> refresh());

        card.getChildren().addAll(
                title,
                statusLabel,
                todayFocusLabel,
                totalFocusLabel,
                levelLabel,
                levelTitleLabel,
                expBar,
                expLabel,
                expToNextLabel,
                streakLabel,
                completedCountLabel,
                refreshButton
        );

        return card;
    }

    public void setFocusStatus(String text) {
        focusing = true;
        focusText = text;
        statusLabel.setText(text);
        statusLabel.getStyleClass().add("focus-status");
    }

    public void setRestStatus() {
        focusing = false;
        focusText = "현재 상태: 휴식 중";
        statusLabel.setText(focusText);
        statusLabel.getStyleClass().remove("focus-status");
    }

    public void refresh() {
        try {
            String response = ServerClient.getMyStatus();

            String expStr            = Ui.extractValue(response, "exp");
            String streak            = Ui.extractValue(response, "streak");
            String todayFocusSeconds = Ui.extractValue(response, "today_focus_sec");
            String totalFocusSeconds = Ui.extractValue(response, "total_focus_sec");
            String completedCount    = Ui.extractValue(response, "completed_count");

            statusLabel.setText(focusing ? focusText : "현재 상태: 휴식 중");

            // 집중 시간 — Ui.formatSeconds 활용
            if (todayFocusSeconds != null) {
                long sec = Long.parseLong(todayFocusSeconds);
                todayFocusLabel.setText("오늘 집중 시간: " + Ui.formatSeconds(sec));
            }

            if (totalFocusSeconds != null) {
                long sec = Long.parseLong(totalFocusSeconds);
                totalFocusLabel.setText("누적 집중 시간: " + Ui.formatSeconds(sec));
            }

            // EXP/레벨 — 서버값 받아서 Java(GoalCalculator)로 직접 계산
            if (expStr != null) {
                int totalExp = Integer.parseInt(expStr);

                int level       = GoalCalculator.calculateLevel(totalExp);
                int currentExp  = GoalCalculator.calculateCurrentExp(totalExp);
                int toNext      = GoalCalculator.expToNextLevel(totalExp);
                double ratio    = GoalCalculator.calculateExpRatio(totalExp);
                String title    = GoalCalculator.getLevelTitle(level);

                levelLabel.setText("Lv. " + level);
                levelTitleLabel.setText(title);
                expLabel.setText("EXP " + currentExp + " / 100");
                expToNextLabel.setText("다음 레벨까지 " + toNext + " EXP");
                expBar.setProgress(ratio);
            }

            if (streak != null) {
                streakLabel.setText("🔥 연속 달성: " + streak + "일");
            }

            if (completedCount != null) {
                completedCountLabel.setText("✅ 완료한 목표: " + completedCount + "개");
            }

        } catch (NumberFormatException e) {
            System.out.println("숫자 변환 실패: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("상태 조회 실패: " + e.getMessage());
        }
    }
}