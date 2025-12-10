package swe.group04.libraryms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/swe/group04/libraryms/view/main.fxml")
        );
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/swe/group04/libraryms/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Library Management System");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
