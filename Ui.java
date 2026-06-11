import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Ui {

    public static VBox card() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(26));
        card.getStyleClass().add("card");
        return card;
    }

    public static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    public static Label normalLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("normal-label");
        return label;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("btn-primary");
        return button;
    }

    public static Button successButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("btn-success");
        return button;
    }

    public static Button dangerButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("btn-danger");
        return button;
    }

    public static Button grayButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("btn-gray");
        return button;
    }

    public static String extractValue(String json, String key) {
    if (json == null || key == null) return null;

    try {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex + search.length());
        if (colonIndex == -1) return null;

        int valueStart = colonIndex + 1;

        // 공백 건너뛰기
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') {
            valueStart++;
        }

        if (valueStart >= json.length()) return null;

        char firstChar = json.charAt(valueStart);

        // 문자열 값 ("..." 형태)
        if (firstChar == '"') {
            int endIndex = json.indexOf('"', valueStart + 1);
            if (endIndex == -1) return null;
            return json.substring(valueStart + 1, endIndex);
        }

        // 숫자 / boolean / null 값
        int endIndex = valueStart;
        while (endIndex < json.length()) {
            char c = json.charAt(endIndex);
            if (c == ',' || c == '}' || c == ']') break;
            endIndex++;
        }

        String result = json.substring(valueStart, endIndex).trim();
        return result.equals("null") ? null : result;

    } catch (Exception e) {
        return null;
    }
}

    public static String formatSeconds(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        return String.format("%02d:%02d", minutes, seconds);
    }
}