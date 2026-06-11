import java.util.ArrayList;

public class GoalCalculator {

    private static final int EXP_PER_LEVEL = 100;
    private static final int EXP_PER_TODO = 30;
    private static final int EXP_PER_FOCUS_MINUTE = 2;
    private static final int STREAK_BONUS_EXP = 20;

    // 총 EXP로 현재 레벨 계산
    public static int calculateLevel(int totalExp) {
        return (totalExp / EXP_PER_LEVEL) + 1;
    }

    // 현재 레벨 내 EXP 진행도 (0~99)
    public static int calculateCurrentExp(int totalExp) {
        return totalExp % EXP_PER_LEVEL;
    }

    // 다음 레벨까지 남은 EXP
    public static int expToNextLevel(int totalExp) {
        return EXP_PER_LEVEL - calculateCurrentExp(totalExp);
    }

    // EXP 진행도 비율 (0.0 ~ 1.0) — ProgressBar에 바로 사용
    public static double calculateExpRatio(int totalExp) {
        return calculateCurrentExp(totalExp) / (double) EXP_PER_LEVEL;
    }

    // 집중 시간(초)으로 획득 EXP 계산
    public static int calculateFocusExp(long focusSeconds) {
        long minutes = focusSeconds / 60;
        return (int) (minutes * EXP_PER_FOCUS_MINUTE);
    }

    // 할 일 완료 EXP
    public static int getTodoExp() {
        return EXP_PER_TODO;
    }

    // 스트릭 보너스 EXP
    public static int getStreakBonusExp() {
        return STREAK_BONUS_EXP;
    }

    // 레벨 이름 반환
    public static String getLevelTitle(int level) {
        if (level >= 10) return "🏆 전설의 학습자";
        if (level >= 7)  return "💎 다이아 학습자";
        if (level >= 5)  return "🥇 골드 학습자";
        if (level >= 3)  return "🥈 실버 학습자";
        return "🌱 새싹 학습자";
    }

    // Todo 목록에서 완료된 것만 세기
    public static int countCompleted(ArrayList<Todo> todos) {
        int count = 0;
        for (Todo todo : todos) {
            if (todo.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    // Todo 목록에서 미완료 것만 세기
    public static int countPending(ArrayList<Todo> todos) {
        int count = 0;
        for (Todo todo : todos) {
            if (!todo.isCompleted()) {
                count++;
            }
        }
        return count;
    }
}