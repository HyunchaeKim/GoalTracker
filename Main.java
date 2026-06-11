import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private DashboardPane dashboardPane;
    private TodoPane todoPane;
    private FocusPane focusPane;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Goal Tracker");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        VBox header = new VBox(6);
        header.setPadding(new Insets(34, 44, 24, 44));

        Label title = new Label("Goal Tracker");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("게임처럼 목표를 달성하고 경험치를 얻어 성장하세요.");
        subtitle.getStyleClass().add("app-subtitle");

        header.getChildren().addAll(title, subtitle);

        dashboardPane = new DashboardPane();
        focusPane = new FocusPane();
        todoPane = new TodoPane(
                () -> {
                    dashboardPane.refresh();
                    todoPane.refresh();
                },
                focusPane,
                dashboardPane
        );

        VBox content = new VBox(26);
        content.setPadding(new Insets(0, 44, 44, 44));
        content.getChildren().addAll(dashboardPane, todoPane, focusPane);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        root.setTop(header);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1120, 880);
        scene.getStylesheets().add(new File("style.css").toURI().toString());

        stage.setScene(scene);
        stage.show();

        dashboardPane.refresh();
        todoPane.refresh();
    }

    public static void main(String[] args) {
        launch(args);
    }
}