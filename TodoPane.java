import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class TodoPane extends VBox {

    private TextField subjectInput;
    private TextField todoInput;
    private CheckBox hideCompletedCheckBox;
    private VBox todoListBox;

    private final Runnable afterChange;
    private final FocusPane focusPane;
    private final DashboardPane dashboardPane;

    public TodoPane(Runnable afterChange, FocusPane focusPane, DashboardPane dashboardPane) {
        this.afterChange = afterChange;
        this.focusPane = focusPane;
        this.dashboardPane = dashboardPane;
        this.focusPane.setDashboardPane(dashboardPane);

        getChildren().add(createView());
    }

    private VBox createView() {
        VBox card = Ui.card();

        Label title = Ui.sectionTitle("오늘 할 일");

        subjectInput = new TextField();
        subjectInput.setPromptText("과목 / 분야 예: 자료구조, Java, 영어");

        todoInput = new TextField();
        todoInput.setPromptText("오늘 할 일을 입력하세요");

        Button addButton = Ui.primaryButton("추가");
        addButton.setOnAction(e -> createTodo());

        HBox inputBox = new HBox(12, subjectInput, todoInput, addButton);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(subjectInput, Priority.ALWAYS);
        HBox.setHgrow(todoInput, Priority.ALWAYS);

        hideCompletedCheckBox = new CheckBox("완료된 할 일 숨기기");
        hideCompletedCheckBox.getStyleClass().add("hide-check");
        hideCompletedCheckBox.setOnAction(e -> refresh());

        todoListBox = new VBox(12);

        card.getChildren().addAll(title, inputBox, hideCompletedCheckBox, todoListBox);

        return card;
    }

    private void createTodo() {
        String subject = subjectInput.getText().trim();
        String content = todoInput.getText().trim();

        if (content.isEmpty()) {
            showError("입력 오류", "할 일을 입력하세요.");
            return;
        }

        if (subject.isEmpty()) {
            subject = "기타";
        }

        try {
            String response = ServerClient.createTodo(content, subject);

            if (!response.contains("\"ok\":true")) {
                showError("할 일 생성 실패", Ui.extractValue(response, "message"));
                return;
            }

            subjectInput.clear();
            todoInput.clear();

            refresh();
            afterChange.run();

        } catch (Exception e) {
            showError("할 일 생성 실패", e.getMessage());
        }
    }

    public void refresh() {
        try {
            String response = ServerClient.getTodos();

            todoListBox.getChildren().clear();

            if (!response.contains("\"todos\":[") || response.contains("\"todos\":[]")) {
                todoListBox.getChildren().add(Ui.normalLabel("아직 등록된 할 일이 없습니다."));
                return;
            }

            String todosPart = response.substring(response.indexOf("[") + 1, response.lastIndexOf("]"));

            if (todosPart.isBlank()) {
                todoListBox.getChildren().add(Ui.normalLabel("아직 등록된 할 일이 없습니다."));
                return;
            }

            String[] items = todosPart.split("\\},\\{");

            for (String item : items) {
                String id = Ui.extractValue(item, "id");
                String title = Ui.extractValue(item, "title");
                String subject = Ui.extractValue(item, "subject");
                String completed = Ui.extractValue(item, "completed");
                String learnedNote = Ui.extractValue(item, "learned_note");

                if (subject == null || subject.equals("null")) subject = "기타";
                if (learnedNote == null || learnedNote.equals("null")) learnedNote = "";

                boolean isCompleted = "1".equals(completed) || "true".equalsIgnoreCase(completed);

                if (hideCompletedCheckBox.isSelected() && isCompleted) continue;

                Todo todo = new Todo(id, title, subject, learnedNote, isCompleted);
                addTodoItem(todo);
            }

            if (todoListBox.getChildren().isEmpty()) {
                todoListBox.getChildren().add(Ui.normalLabel("표시할 할 일이 없습니다."));
            }

        } catch (Exception e) {
            todoListBox.getChildren().clear();
            todoListBox.getChildren().add(Ui.normalLabel("할 일 조회 실패: " + e.getMessage()));
        }
    }

    private void addTodoItem(Todo todo) {
        VBox textBox = new VBox(5);

        Label subjectLabel = new Label("[" + todo.getSubject() + "]");
        subjectLabel.getStyleClass().add("subject-tag");

        Label todoLabel = Ui.normalLabel(todo.getContent());

        Label learnedLabel = Ui.normalLabel("");
        learnedLabel.getStyleClass().add("learned-note");

        if (todo.getLearnedNote() != null && !todo.getLearnedNote().isBlank()) {
            learnedLabel.setText("배운 점: " + todo.getLearnedNote());
        }

        textBox.getChildren().addAll(subjectLabel, todoLabel);

        if (!learnedLabel.getText().isBlank()) {
            textBox.getChildren().add(learnedLabel);
        }

        Button focusButton = Ui.primaryButton("집중 시작");
        Button completeButton = Ui.successButton("완료");

        if (todo.isCompleted()) {
            completeButton.setDisable(true);
            completeButton.setText("완료됨");
            focusButton.setDisable(true);
            todoLabel.getStyleClass().add("done-text");
        }

        focusButton.setOnAction(e -> {
            if (focusPane.isFocusing()) {
                showError("집중 중", "이미 집중 중입니다. 먼저 집중 종료를 눌러주세요.");
                return;
            }
            focusPane.startFocus(todo, dashboardPane);
        });

        completeButton.setOnAction(e -> completeTodo(todo));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox item = new HBox(14, textBox, spacer, focusButton, completeButton);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.getStyleClass().add("todo-item");

        if (todo.isCompleted()) {
            item.getStyleClass().add("todo-done");
        }

        todoListBox.getChildren().add(item);
    }

    private void completeTodo(Todo todo) {
        try {
            boolean autoEndedFocus = false;
            String focusExpAdded = null;

            if (focusPane.isFocusing()) {
                focusExpAdded = focusPane.autoEndFocus();
                autoEndedFocus = true;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("학습 회고");
            dialog.setHeaderText("오늘 이 할 일을 하면서 배운 점을 적어주세요.");
            dialog.setContentText("한줄 회고:");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return;

            String learnedNote = result.get().trim();
            if (learnedNote.isEmpty()) learnedNote = "회고 없음";

            String response = ServerClient.completeTodo(todo.getId(), learnedNote);

            if (!response.contains("\"ok\":true")) {
                showError("할 일 완료 실패", Ui.extractValue(response, "message"));
                return;
            }

            String message = "할 일을 완료했습니다.\n학습 회고가 저장되고 EXP가 지급되었습니다.";

            if (autoEndedFocus) {
                message = "집중 시간이 자동 저장되었습니다.\n" + message;
                if (focusExpAdded != null) {
                    message += "\n집중 EXP +" + focusExpAdded;
                }
            }

            showInfo("완료 처리", message);

            refresh();
            afterChange.run();

        } catch (Exception e) {
            showError("할 일 완료 실패", e.getMessage());
        }
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