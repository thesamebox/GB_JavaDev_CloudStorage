import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public ListView<String>listView;
    public TextField inputArea;


    public void send(ActionEvent actionEvent) throws IOException {

    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

