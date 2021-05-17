import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
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
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
@Slf4j
public class RegistrationPageController implements Initializable {
    public Button cancelRegistrationButton;
    public TextField login;
    public PasswordField password;
    public PasswordField confirmPassword;
    public Button makeRegistrationButton;
    public Label warning;
    private NettyNetwork network;
    private ObjectDecoderInputStream odis;
    private ObjectEncoderOutputStream oeos;

    public void makeRegistration(ActionEvent actionEvent) throws IOException {
        warning.setText("");
        if (!login.getText().isEmpty() && !password.getText().isEmpty() && !confirmPassword.getText().isEmpty()) {
            if (password.getText().equals(confirmPassword.getText())) {
                SerialHandlerWithCallBack.regRequest(login.getText(), password.getText());
            } else {
                warning.setText("Passwords does not match");
                password.clear();
                confirmPassword.clear();
            }

        }
    }

    public void cancelRegistration(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) cancelRegistrationButton.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("AuthPage.fxml")));
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("CloudStorage");
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            Thread registerPageListener = new Thread(() -> {
               try {
                   while (true) {
                       //Где то тут растет херня
                       String serverMessage = String.valueOf(odis.readObject());
                       log.debug(serverMessage);
                       if (serverMessage.equals(CommandList.LOGIN_IS_TAKEN)) {
                           warning.setText("The login is taken already");
                           login.clear();
                           password.clear();
                           confirmPassword.clear();
                       }
                       if (serverMessage.equals(CommandList.REG_OK)) {
                           warning.setText("Registration finished successfully");
                           try {
                               Thread.sleep(1000);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }
                           Stage stage = (Stage) makeRegistrationButton.getScene().getWindow();
                           Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("AuthPage.fxml")));
                           Scene scene = new Scene(root, 800, 600);
                           stage.setScene(scene);
                           stage.setResizable(false);
                           stage.setTitle("CloudStorage");
                           stage.show();
                       }
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }

            });
            registerPageListener.setDaemon(true);
            registerPageListener.start();
        } catch (Exception e) {

        }

    }
}
