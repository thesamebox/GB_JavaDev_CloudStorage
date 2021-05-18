import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AuthPageController implements Initializable {
    @FXML
    private Button AuthButton;
    @FXML
    private Button goToRegistrationPageButton;
    @FXML
    private PasswordField password;
    @FXML
    private TextField login;
    @FXML
    private Label warning;

    private NettyNetwork network;
    private String serverMessage;


    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread regService = new Thread(() -> {
            network = NettyNetwork.getInstance(
                    request -> {
                        if (request instanceof RequestResponse) {
                            RequestResponse requestResponse = (RequestResponse) request;
                            serverMessage = requestResponse.getServerMessage();
                            if (serverMessage.equals(CommandList.WRONG_PASSWORD)) {
                                Platform.runLater(() -> {
                                    warning.setText("Wrong password");
                                    login.clear();
                                    password.clear();
                                });
                            }
                            if (serverMessage.equals(CommandList.NO_REGISTERED_USER)) {
                                Platform.runLater(() -> {
                                    warning.setText(String.format("The user with login %s doesn't exist", login.getText()));
                                    login.clear();
                                    password.clear();
                                });
                            }
                            if (serverMessage.equals(CommandList.AUTH_OK)) {

                                Platform.runLater(() -> {
//                                    warning.setText(String.format("Welcome, %s", login.getText()));
                                    login.clear();
                                    password.clear();
                                    try {
                                        goToStorage();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                    }
            );
        });
        regService.setDaemon(true);
        regService.start();
    }

    public void goToStorage() throws Exception {
        Stage stage = (Stage) AuthButton.getScene().getWindow();
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client.fxml")));
        stage.setTitle("CloudStorage");
        stage.setScene(new Scene(parent, 800, 600));
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    public void Enter(ActionEvent actionEvent) throws IOException {
        if (!login.getText().isEmpty() && !password.getText().isEmpty()) {
            network.write(new AuthRequest(login.getText(), password.getText()));
            login.clear();
            password.clear();
        }else {
            warning.setText("You have to fill in all the fields ");
            login.clear();
            password.clear();
        }
    }

    @FXML
    public void goToRegistrationPage(ActionEvent actionEvent) throws IOException {

        Stage stage = (Stage) goToRegistrationPageButton.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("RegistrationPage.fxml")));
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("CloudStorage: Create your account");
        stage.show();
    }

}
