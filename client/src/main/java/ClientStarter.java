import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class ClientStarter extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("AuthPage.fxml")));
        primaryStage.setTitle("CloudStorage");
        primaryStage.setScene(new Scene(parent, 800, 600));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
