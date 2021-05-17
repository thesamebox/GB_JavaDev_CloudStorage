import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
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
import model.Message;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Stack;

public class AuthPageController implements Initializable{
    @FXML
    public Button AuthButton;
    @FXML
    public Button goToRegistrationPageButton;
    @FXML
    public PasswordField password;
    @FXML
    public TextField login;
    public static Label warning;
    private NettyNetwork network;
    private ObjectDecoderInputStream odis;
    private ObjectEncoderOutputStream oeos;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            Thread serviceThread = new Thread(() -> {
                try {
                    while (true) {

                        Requests request = (Requests) odis.readObject();
                    }
                } catch (Exception e) {

                }
            });
            serviceThread.setDaemon(true);
            serviceThread.start();
        } catch (Exception e) {

        }
    }
    @FXML
    public void Enter(ActionEvent actionEvent) throws IOException {
        if (!login.getText().isEmpty() && !password.getText().isEmpty()) {
            SerialHandlerWithCallBack.authRequest(login.getText(), password.getText());
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
