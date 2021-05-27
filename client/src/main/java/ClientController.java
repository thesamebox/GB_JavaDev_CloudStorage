import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ClientController implements Initializable {

    @FXML
    private Button logout;
    @FXML
    private ListView<StorageFiles> localStorageList;
    @FXML
    private ListView<StorageFiles> cloudStorageList;

    private HashMap<Integer, LinkedList<File>> cloudStorageFolder;
    private String serverResponse;
    private String localDirectory = "TestFiles\\LocalSource";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ConnectServer.setConnection();
        initLocalFilesList();
        Thread serverListener = new Thread(() -> {
            while (ConnectServer.isActive()) {
                Object request = null;
                try {
                    request = ConnectServer.getResponse();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (request instanceof UpdateRequest) {
                    UpdateRequest requestResponse = (UpdateRequest) request;
                    cloudStorageFolder = new HashMap<>();
                    cloudStorageFolder.putAll(requestResponse.getCloudStorageContents());
                    Platform.runLater(() -> initCloudFilesList(cloudStorageFolder));
                }
                if (request instanceof RequestResponse) {
                    RequestResponse requestResponse = (RequestResponse) request;
                    serverResponse = requestResponse.getServerMessage();
                    if (request.equals(CommandList.DELETED)) {
                        Platform.runLater(() -> {
                            initCloudFilesList(cloudStorageFolder);
                        });
                    }
                }
            }
        });
        serverListener.setDaemon(true);
        serverListener.start();
    }

    private void initLocalFilesList() {
        ObservableList<StorageFiles> localList = FXCollections.observableArrayList();
        File localListPath = new File(localDirectory);
        File[] localListFiles = localListPath.listFiles();
        if (localListFiles.length == 0) {
            localStorageList.setOpacity(0.9);
            localStorageList.setItems(localList);
            localStorageList.setCellFactory(param -> new StorageFilesList());
        } else if (localListFiles.length > 0) {
            for (File llf : localListFiles) {
                long sizeStorageItem;
                String nameStorageItem = llf.getName();
                if (llf.isDirectory()) {
                    sizeStorageItem = getSizeStorageDirectory(llf);
                } else {
                    sizeStorageItem = llf.length();
                }
                String lastChanges = new SimpleDateFormat("dd/MM/yyyy")
                        .format(new Date(llf.lastModified()));
                File pathToFile = new File(llf.getAbsolutePath());
                localList.addAll(new StorageFiles(
                        nameStorageItem,
                        sizeStorageItem,
                        false,
                        lastChanges,
                        pathToFile));
            }
            localStorageList.setItems(localList);
            localStorageList.setCellFactory(param -> new StorageFilesList());
        } else {
            localStorageList.setItems(localList);
            localStorageList.setCellFactory(param -> new StorageFilesList());
        }
    }

    private void initCloudFilesList(HashMap<Integer, LinkedList<File>> cloudListFiles) {
        ObservableList<StorageFiles> cloudList = FXCollections.observableArrayList();
        if (!cloudListFiles.isEmpty()) {
            for (int i = 0; i < cloudListFiles.get(0).size(); i++) {
                long sizeStorageItem;
                String nameStorageItem = cloudListFiles.get(0).get(i).getName();
                if (cloudListFiles.get(0).get(i).isDirectory()) {
                    sizeStorageItem = getSizeStorageDirectory(cloudListFiles.get(0).get(i));
                } else {
                    sizeStorageItem = cloudListFiles.get(0).get(i).length();
                }
                String lastChanges = new SimpleDateFormat("dd/MM/yyyy")
                        .format(new Date(cloudListFiles.get(0).get(i).lastModified()));
                File pathToFile = new File(cloudListFiles.get(0).get(i).getAbsolutePath());
                cloudList.addAll(new StorageFiles(
                        nameStorageItem,
                        sizeStorageItem,
                        false,
                        lastChanges,
                        pathToFile));
            }
            cloudStorageList.setItems(cloudList);
            cloudStorageList.setCellFactory(param -> new StorageFilesList());
        } else {
            cloudStorageList.setItems(cloudList);
            cloudStorageList.setCellFactory(param -> new StorageFilesList());
        }
    }

    private long getSizeStorageDirectory(File file) {
        long folderSize = 0;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.isFile()) {
                    folderSize += f.length();
                } else if (f.isDirectory()) {
                    folderSize +=getSizeStorageDirectory(f);
                }
            }
        }
        return folderSize;
    }

    @FXML
    public void logout(ActionEvent actionEvent) throws IOException {
        log.debug("Client logout");
        Stage stage = (Stage) logout.getScene().getWindow();
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("AuthPage.fxml")));
        stage.setScene(new Scene(parent, 400, 500));
        stage.setResizable(false);
        stage.setTitle("CloudStorage");
        stage.show();
    }
}
