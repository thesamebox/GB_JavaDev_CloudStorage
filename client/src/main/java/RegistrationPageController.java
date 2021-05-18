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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
@Slf4j
public class RegistrationPageController implements Initializable {
    @FXML
    private Button cancelRegistrationButton;
    @FXML
    private TextField login;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private Button makeRegistrationButton;
    @FXML
    private  Label warning;

    private NettyNetwork network;
    private String serverMessage;


    public void makeRegistration(ActionEvent actionEvent) throws IOException {
        if (!login.getText().isEmpty() && !password.getText().isEmpty() && !confirmPassword.getText().isEmpty()) {
            if (password.getText().equals(confirmPassword.getText())) {
                network.write(new RegistrationRequest(login.getText(), password.getText()));
            } else {
                warning.setText("Passwords does not match");
                password.clear();
                confirmPassword.clear();
            }
        } else {
            warning.setText("You have to fill in all the fields ");
            password.clear();
            confirmPassword.clear();
        }
    }

    public void cancelRegistration(ActionEvent actionEvent) throws IOException {
        createAuthPage(cancelRegistrationButton);
    }

    public void createAuthPage(Button button) throws IOException {
        Stage stage = (Stage) button.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("AuthPage.fxml")));
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("CloudStorage");
        stage.show();
    }
    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread regService = new Thread(() -> {
            network = NettyNetwork.getInstance(
                    request -> {
                        if ( request instanceof RequestResponse) {
                            RequestResponse requestResponse = (RequestResponse) request;
                            serverMessage = requestResponse.getServerMessage();
                            log.debug(serverMessage);
                            if (serverMessage.equals(CommandList.LOGIN_IS_TAKEN)) {
                                Platform.runLater(() -> {
                                    warning.setText("The login is taken already");
                                    login.clear();
                                    password.clear();
                                    confirmPassword.clear();
                                });
                            }
                            if (serverMessage.equals(CommandList.REG_OK)) {
                                Platform.runLater(() -> {
//                                    warning.setText("Registration was successful");
                                    login.clear();
                                    password.clear();
                                    confirmPassword.clear();
                                    try {
                                        createAuthPage(makeRegistrationButton);
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
}
