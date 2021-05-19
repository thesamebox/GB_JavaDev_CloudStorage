import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public ListView<File> LocalS;
    public String currentDirectory = "TestFiles\\LocalSource";
    public TextField inputArea;
    @FXML
    public ListView<File> localStorageList;


    public void send(ActionEvent actionEvent) throws IOException {

    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initializeFilesList() {
        File pathToLocalStorage = new File(currentDirectory);
        File[] localStorageContentList = pathToLocalStorage.listFiles();
//        if (localStorageContentList > 0) {

        }
    }

