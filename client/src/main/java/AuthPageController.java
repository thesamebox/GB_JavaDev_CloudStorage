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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
@Slf4j
public class AuthPageController implements Initializable {
    @FXML
    private Label warning;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button goToRegistrationPageButton;
    @FXML
    private Button authButton;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button makeRegistrationButton;
    @FXML
    private Button cancelRegistrationButton;
    @FXML
    private Label confPassLabel;

    private String serverResponse;
    private CurrentLogin currentLogin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ConnectServer.setConnection();
        Thread serverListener = new Thread(() -> {
            while (ConnectServer.isActive()) {
                Object request = null;
                try {
                    request = ConnectServer.getResponse();
                } catch (IOException | ClassNotFoundException e) {
                    log.error("Exception - ", e);
                }
                if (request instanceof RequestResponse) {
                    RequestResponse requestResponse = (RequestResponse) request;
                    serverResponse = requestResponse.getServerMessage();
                    if (serverResponse.equals(CommandList.WRONG_PASSWORD)) {
                        log.info(" !!!!! gottenmes is Wrngpass");
                        Platform.runLater(() -> {
                            warning.setText("Wrong password");
                            loginField.clear();
                            passwordField.clear();
                        });
                    }
                    if (serverResponse.equals(CommandList.NO_REGISTERED_USER)) {
                        log.info(" !!!!! gottenmes is NoUser");
                        Platform.runLater(() -> {
                            warning.setText(String.format("The user with login %s doesn't exist", loginField.getText()));
                            loginField.clear();
                            passwordField.clear();
                        });
                    }
                    if (serverResponse.equals(CommandList.LOGIN_IS_TAKEN)) {
                        log.info(" !!!!! gottenmes is LoginIsTaken");
                        Platform.runLater(() -> {
                            warning.setText("The login is taken already");
                            loginField.clear();
                            passwordField.clear();
                            confirmPasswordField.clear();
                        });
                    }
                    if (serverResponse.equals(CommandList.AUTH_OK)) {
                        log.info(" !!!!! gottenmes is AuthOk");
                        CurrentLogin.setCurrentLogin(loginField.getText());
                        System.out.println(CurrentLogin.getCurrentLogin());
                        Platform.runLater(() -> {
                            warning.setText(String.format("Welcome, %s", loginField.getText()));
                            try {
                                switchScenes(loginField.getText());
                            } catch (Exception e) {
                                log.error("Exception - ", e);
                            }
                        });
                    }
                    if (serverResponse.equals(CommandList.REG_OK)) {
                        log.info(" !!!!! gottenmes is RegIsDone");
                        CurrentLogin.setCurrentLogin(loginField.getText());
                        System.out.println(CurrentLogin.getCurrentLogin());
                        Platform.runLater(() -> {
                            warning.setText("Registration was successful");
                            try {
                                switchScenes(loginField.getText());
                            } catch (Exception e) {
                                log.error("Exception - ", e);                            }
                        });
                    }
                }
            }
        });
        serverListener.setDaemon(true);
        serverListener.start();
    }
    @FXML
    public void goToRegistrationPage(ActionEvent actionEvent) {
        goToRegistrationPageButton.setVisible(false);
        authButton.setVisible(false);
        confPassLabel.setVisible(true);
        confirmPasswordField.setVisible(true);
        makeRegistrationButton.setVisible(true);
        cancelRegistrationButton.setVisible(true);
        loginField.clear();
        passwordField.clear();
    }
    @FXML
    public void cancelRegistration(ActionEvent actionEvent) {
        confPassLabel.setVisible(false);
        confirmPasswordField.setVisible(false);
        makeRegistrationButton.setVisible(false);
        cancelRegistrationButton.setVisible(false);
        goToRegistrationPageButton.setVisible(true);
        authButton.setVisible(true);
        loginField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
    @FXML
    public void register(ActionEvent actionEvent) {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty() && !confirmPasswordField.getText().isEmpty()) {
            if (passwordField.getText().equals(confirmPasswordField.getText())) {
                ConnectServer.RegRequest(loginField.getText(), passwordField.getText());
            } else {
                warning.setText("Passwords does not match");
                passwordField.clear();
                confirmPasswordField.clear();
            }
        } else {
            warning.setText("You have to fill in all the fields ");
            passwordField.clear();
            confirmPasswordField.clear();
        }
    }
    @FXML
    public void authorize(ActionEvent actionEvent) {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()){
            ConnectServer.AuthRequest(loginField.getText(),passwordField.getText());
        }else {
            warning.setText("You have to fill in all the fields ");
        }
        loginField.clear();
        passwordField.clear();
    }

    public void switchScenes(String login) throws IOException {
        Stage stage = (Stage) authButton.getScene().getWindow();
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client.fxml")));
        stage.setScene(new Scene(parent, 1000, 600));
        stage.setResizable(false);
        stage.setTitle(login + " in the Frankenstein's stash");
        stage.show();
    }
}
