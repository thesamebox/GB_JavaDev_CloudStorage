import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import jdk.nashorn.internal.runtime.ECMAException;
import lombok.SneakyThrows;
import model.Message;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public ListView<String>listView;
    public TextField inputArea;
    private NettyNetwork network;
    private ObjectDecoderInputStream odis;
    private ObjectEncoderOutputStream oeos;

    public void send(ActionEvent actionEvent) throws IOException {
        oeos.writeObject(Message.builder()
                .created(LocalDateTime.now())
                .text(inputArea.getText())
                .author("user")
                .build());
        inputArea.clear();
    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");
            Thread serviceThread = new Thread(() -> {
                try {
                    while (true) {

                        Message message = (Message) odis.readObject();
                        String msg = String.format("[%s] %s: %s",
                                message.getCreated().format(dateFormat),
                                message.getAuthor(),
                                message.getText());
                        Platform.runLater(() ->
                                listView.getItems().add(msg));
                    }
                } catch (Exception e) {

                }

            });
            serviceThread.setDaemon(true);
            serviceThread.start();
        } catch (Exception e) {

        }

    }
}

