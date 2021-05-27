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
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Slf4j
public class ClientController implements Initializable {

    public Button previousCloudFolderButton;
    public Button previousLocalFolderButton;
    @FXML
    private Button logout;
    @FXML
    private ListView<StorageFiles> localStorageList;
    @FXML
    private ListView<StorageFiles> cloudStorageList;

    private HashMap<Integer, LinkedList<File>> cloudStorageFolder;
    private String serverResponse;
    private String currentDirectoryName = "";
    private String localDirectory = "TestFiles\\LocalSource";
    private int localStorageFolderLevelCounter = 0;
    private int cloudStorageFolderLevelCounter = 0;
    private LinkedList<File> pathToCloudStorageList;

    //common methods
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
                    log.error("Exception - ", e);
                }
                if (request instanceof UpdateRequest) {
                    UpdateRequest requestResponse = (UpdateRequest) request;
                    cloudStorageFolder = new HashMap<>();
                    cloudStorageFolder.putAll(requestResponse.getCloudStorageContents());
                    Platform.runLater(() -> initCloudFilesList(cloudStorageFolder));
                }
                if (request instanceof CopyRequest) {
                    CopyRequest copyRequest = (CopyRequest) request;
                    if (copyRequest.isDirectory() && copyRequest.isEmpty()) {
                        Path toLocalDirectory = Paths.get(localDirectory + "\\" + copyRequest.getFileName());
                        if (Files.exists(toLocalDirectory)) {
                            log.debug("the " + CurrentLogin.getCurrentLogin() + "'s directory exists already");
                        } else {
                            Platform.runLater(() -> {
                                try {
                                    Files.createDirectory(toLocalDirectory);
                                } catch (IOException e) {
                                    log.error("Exception - ", e);
                                }
                            });
                        }
                    } else {
                        try {
                            Files.write(Paths.get(localDirectory + "\\" + copyRequest.getFileName()),
                                    copyRequest.getData(),
                                    StandardOpenOption.CREATE);
                        } catch (NullPointerException | IOException e) {
                            log.error("Exception - ", e);
                        }
                    }
                    Platform.runLater(this::initLocalFilesList);
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
        dragToDownload();
        dragToUpload();
        serverListener.setDaemon(true);
        serverListener.start();
        deleteChosenFilesOnKeyDeletePressed();
        updateCloudStoragePanel();
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
    public void DeleteRequest(){
        ConnectServer.deleteRequest(CurrentLogin.getCurrentLogin(), getPathsOfSelectedFilesInCloudStorage());
    }
    public void deleteChosenFilesOnKeyDeletePressed() {
        localStorageList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                deleteChosenFilesFromLocalStorage();
            }
        });
        cloudStorageList.setOnKeyPressed(event -> {
            localStorageList.getSelectionModel().clearSelection();
            if (event.getCode() == KeyCode.DELETE){
                DeleteRequest();
            }
        });
    }
    public static void deleteContentsOfFolderRecursively(File file) throws Exception {
        try {
            if (file.isDirectory()) {
                for (File c : file.listFiles()) {
                    deleteContentsOfFolderRecursively(c);
                }
            }
            if (!file.delete()) {
                throw new Exception("Delete command returned false for file: " + file);
            }
        } catch (Exception e) {
            throw new Exception("Failed to delete the folder: " + file, e);
        }
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

    // local methods
    @FXML
    private void initLocalFilesList() {
        ObservableList<StorageFiles> localList = FXCollections.observableArrayList();
        File localListPath = new File(localDirectory);
        File[] localListFiles = localListPath.listFiles();
        if (localListFiles.length == 0 && localStorageFolderLevelCounter == 0) {
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
    private void dragToUpload() {
        localStorageList.setOnDragDetected(event -> {
            Dragboard db = localStorageList.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            List<File> localFiles = new LinkedList<File>(getPathsOfSelectedFilesInLocalStorage());
            content.putFiles(localFiles);
            localStorageList.setStyle("-fx-opacity: 1;");
            db.setContent(content);
        });
        localStorageList.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            localStorageList.setStyle("-fx-opacity: 1;");
        });
        cloudStorageList.setOnDragEntered(event -> {
            if (event.getGestureSource() != localStorageList){
                event.acceptTransferModes(TransferMode.NONE);
            }
            cloudStorageList.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
        });
        cloudStorageList.setOnDragOver(event -> {
            if (event.getGestureSource() != localStorageList){
                event.acceptTransferModes(TransferMode.NONE);
            }else {
                event.acceptTransferModes(TransferMode.COPY);
                cloudStorageList.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
                localStorageList.setStyle("-fx-opacity: 1;");
            }
        });
        cloudStorageList.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            cloudStorageList.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            localStorageList.setStyle("-fx-opacity: 1;");
        });
        cloudStorageList.setOnDragDropped(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            cloudStorageList.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            localStorageList.setStyle("-fx-opacity: 1;");
            uploadRequest();
        });
    }
    public LinkedList<File> getPathsOfSelectedFilesInLocalStorage() {
        try {
            localStorageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            LinkedList<File> listOfSelectedElementsInLocalStorage = new LinkedList<File>();
            if (localStorageList.getSelectionModel().getSelectedItems().size() != 0) {
                System.out.println(localStorageList.getSelectionModel().getSelectedItems().size());
                for (int i = 0; i < localStorageList.getSelectionModel().getSelectedItems().size(); i++) {
                    listOfSelectedElementsInLocalStorage.add(localStorageList.getSelectionModel().getSelectedItems().get(i).getPathToFile());
                }
                return listOfSelectedElementsInLocalStorage;
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }
    public void uploadRequest() {
        ConnectServer.uploadFiles(CurrentLogin.getCurrentLogin(),getPathsOfSelectedFilesInLocalStorage());
    }
    public void goToNextDirectoryInLocalStorageOnDoubleClickOrOpenFile(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            Platform.runLater(() -> {
                localStorageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            });
        } else if (mouseEvent.getClickCount() == 2) {
            localStorageList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            if (localStorageList.getSelectionModel().getSelectedItems().size() == 1) {
                File pathToClickedFile;
                pathToClickedFile = localStorageList.getSelectionModel().getSelectedItem().getPathToFile();
                if (pathToClickedFile.isDirectory()) {
                    File[] nextDirectory = pathToClickedFile.listFiles();
                    if (nextDirectory.length == 0) {

                    }else if (nextDirectory.length != 0) {
                        localStorageFolderLevelCounter++;
                        if (localStorageFolderLevelCounter > 0) {
                            previousLocalFolderButton.setVisible(true);
                        }
                        if (localStorageFolderLevelCounter > 0 && nextDirectory.length != 0) {
                            localDirectory += "\\" + pathToClickedFile.getName();
                            currentDirectoryName = pathToClickedFile.getName();
                        } else {
                            currentDirectoryName = "TestFiles\\LocalSource";
                        }
                        ObservableList<StorageFiles> listOfLocalItems = FXCollections.observableArrayList();
                        for (int i = 0; i < nextDirectory.length; i++) {
                            String nameOfLocalFileOrDirectory = nextDirectory[i].getName();
                            long initialSizeOfLocalFileOrDirectory = 0;
                            try {
                                if (nextDirectory[i].isDirectory()){
                                    initialSizeOfLocalFileOrDirectory = getSizeStorageDirectory(nextDirectory[i]);
                                }else{
                                    initialSizeOfLocalFileOrDirectory = nextDirectory[i].length();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                    .format(new Date(nextDirectory[i].lastModified()));
                            File pathOfFileInLocalStorage = new File(nextDirectory[i].getAbsolutePath());
                            listOfLocalItems.addAll(new StorageFiles(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification,pathOfFileInLocalStorage));
                            localStorageList.setItems(listOfLocalItems);
                            localStorageList.setCellFactory(param -> new StorageFilesList());
                        }

                    }
                }else {
                    Desktop desktop = null;
                    if (desktop.isDesktopSupported()){
                        desktop = desktop.getDesktop();
                        try {
                            desktop.open(pathToClickedFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public void goToPreviousDirectoryInLocalStorage() {
        ObservableList<StorageFiles> listOfLocalItems = FXCollections.observableArrayList();
        LinkedList<File> files = new LinkedList<>();
        File file = new File(localDirectory);
        File previousDirectory = new File(file.getParent());
        File[] contentsOfPreviousDirectory = previousDirectory.listFiles();
        for (int i = 0; i < contentsOfPreviousDirectory.length; i++) {
            files.add((contentsOfPreviousDirectory[i]));
        }
        for (int i = 0; i < files.size(); i++) {
            String nameOfLocalFileOrDirectory = files.get(i).getName();
            long initialSizeOfLocalFileOrDirectory = 0;
            try {
                if (files.get(i).isDirectory()){
                    initialSizeOfLocalFileOrDirectory = getSizeStorageDirectory(files.get(i));
                }else {
                    initialSizeOfLocalFileOrDirectory = files.get(i).length();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date(files.get(i).lastModified()));
            File pathOfFileInLocalStorage = files.get(i).getAbsoluteFile();
            listOfLocalItems.addAll(new StorageFiles(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathOfFileInLocalStorage
            ));
        }
        localStorageList.setItems(listOfLocalItems);
        localStorageList.setCellFactory(param -> new StorageFilesList());
        localStorageFolderLevelCounter--;
        if (localStorageFolderLevelCounter <= 0) {
            previousLocalFolderButton.setVisible(false);
            localDirectory = "TestFiles\\LocalSource";
            currentDirectoryName = "LocalSource";
        }else {
            localDirectory = previousDirectory.toString();
            currentDirectoryName = previousDirectory.getName();
        }
    }
    public void deleteChosenFilesFromLocalStorage(){
        localStorageList.getSelectionModel().clearSelection();
        for (int i = 0; i < getPathsOfSelectedFilesInLocalStorage().size(); i++) {
            String absolutePath = getPathsOfSelectedFilesInLocalStorage().get(i).toString();
            Path path = Paths.get(absolutePath);
            File file = new File(getPathsOfSelectedFilesInLocalStorage().get(i).toString());
            try {
                if (file.isDirectory()) {
                    deleteContentsOfFolderRecursively(file);
                } else {
                    Files.delete(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initLocalFilesList();
    }
    public void selectAllFilesFromLocalStorage() {
        if (localStorageList.getItems().size() == localStorageList.getSelectionModel().getSelectedItems().size()) {
            localStorageList.getSelectionModel().clearSelection();
        } else {
            localStorageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            localStorageList.getSelectionModel().selectAll();
        }
    }

    //cloud methods
    private void initCloudFilesList(HashMap<Integer, LinkedList<File>> cloudListFiles) {
        if (cloudStorageFolderLevelCounter > 0){
            cloudStorageFolderLevelCounter = 0;
            previousCloudFolderButton.setVisible(false);
        }
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
                File pathToFile = new File(cloudListFiles.get(0).get(i).getAbsolutePath() + "\\" + CurrentLogin.getCurrentLogin());
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
    private void dragToDownload() {
        localStorageList.setOnDragOver(event -> {
            localStorageList.setStyle("-fx-background-color: gray; -fx-opacity: 0.5;");
            event.acceptTransferModes(TransferMode.MOVE);
        });
        localStorageList.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            ObservableList<StorageFiles> listOfFiles = FXCollections.observableArrayList();
            String pathToCorrectDirectory = localDirectory + "\\";
            if (db.hasFiles()) {
                for (int i = 0; i < db.getFiles().size(); i++) {
                    long initialSize = 0;
                    String name = db.getFiles().get(i).getName();
                    if (db.getFiles().get(i).isDirectory()){
                        try {
                            initialSize = getSizeStorageDirectory(db.getFiles().get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        initialSize = db.getFiles().get(i).length();
                    }
                    String dateOfLastModification = new java.text.SimpleDateFormat("dd/MM/yyy")
                            .format(new Date(db.getFiles().get(i).lastModified()));
                    File pathOfDroppedFileInLocalStorage = db.getFiles().get(i).getAbsoluteFile();
                    Path sourcePath = Paths.get(db.getFiles().get(i).getAbsolutePath());
                    File destinationPath = new File(pathToCorrectDirectory + db.getFiles().get(i).getName());
                    try {
                        Files.move(sourcePath, Paths.get(destinationPath.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    listOfFiles.add(new StorageFiles(name, initialSize, false, dateOfLastModification,pathOfDroppedFileInLocalStorage));
                    localStorageList.setItems(listOfFiles);
                    localStorageList.setCellFactory(param -> new StorageFilesList());
                }
                initLocalFilesList();
                localStorageList.setStyle("-fx-background-color: white;");
            }
            success = true;
            event.setDropCompleted(success);
            event.consume();
        });
        localStorageList.setOnDragExited(event -> {
            localStorageList.setStyle("-fx-background-color: gray; -fx-opacity: 1;");
            event.acceptTransferModes(TransferMode.NONE);
        });
    }
    public LinkedList<File> getPathsOfSelectedFilesInCloudStorage(){
        cloudStorageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        LinkedList<File> listOfSelectedElementsInCloudStorage = new LinkedList<File>();
        if (cloudStorageList.getSelectionModel().getSelectedItems().size() != 0){
            for (int i = 0; i < cloudStorageList.getSelectionModel().getSelectedItems().size(); i++) {
                listOfSelectedElementsInCloudStorage.add(cloudStorageList.getSelectionModel().getSelectedItems().get(i).getPathToFile());
            }
        }
        return listOfSelectedElementsInCloudStorage;
    }
    public void goToNextDirectoryInCloudStorageOnDoubleClick(MouseEvent mouseEvent) {
        pathToCloudStorageList = new LinkedList<>();
        if (mouseEvent.getClickCount() == 1) {
            cloudStorageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else if (mouseEvent.getClickCount() == 2) {
            cloudStorageList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            if (cloudStorageList.getSelectionModel().getSelectedItems().size() == 1) {
                File pathToClickedFile = new File("");
                for (int i = 0; i < cloudStorageFolder.get(cloudStorageFolderLevelCounter).size(); i++) {
                    File file = cloudStorageFolder.get(cloudStorageFolderLevelCounter).get(i);
                    if (cloudStorageList.getSelectionModel().getSelectedItem().getName().equals(file.getName())) {
                        pathToClickedFile = cloudStorageFolder.get(cloudStorageFolderLevelCounter).get(i);
                    }
                }
                if (pathToClickedFile.isDirectory()) {
                    File[] nextDirectory = pathToClickedFile.listFiles();
                    if (nextDirectory.length == 0) {
                        System.out.println("empty");
                    }
                    if (nextDirectory.length != 0) {
                        for (int i = 0; i < nextDirectory.length; i++) {
                            try {
                                pathToCloudStorageList.add(nextDirectory[i]);
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                        cloudStorageFolderLevelCounter++;
                        cloudStorageFolder.put(cloudStorageFolderLevelCounter, pathToCloudStorageList);
                        ObservableList<StorageFiles> listOfCloudItems = FXCollections.observableArrayList();
                        for (int i = 0; i < nextDirectory.length; i++) {
                            String nameOfCloudStorageFileOrDirectory = nextDirectory[i].getName();
                            long initialSizeOfLocalStorageFileOrDirectory = nextDirectory[i].length();
                            String dateOfLastModification = new java.text.SimpleDateFormat("dd/MM/yyyy")
                                    .format(new Date(nextDirectory[i].lastModified()));
                            File pathToFileInLocalStorage = new File(nextDirectory[i].getAbsolutePath());
                            listOfCloudItems.addAll(new StorageFiles(nameOfCloudStorageFileOrDirectory,
                                    initialSizeOfLocalStorageFileOrDirectory,
                                    false,
                                    dateOfLastModification,
                                    pathToFileInLocalStorage));
                            cloudStorageList.setItems(listOfCloudItems);
                            cloudStorageList.setCellFactory(param -> new StorageFilesList());
                        }
                    }
                    if (cloudStorageFolderLevelCounter > 0) {
                        previousCloudFolderButton.setVisible(true);
                    }
                }else {
                    log.debug("it is not a directory");                }
            }
        }
    }
    public void goToPreviousDirectoryInCloudStorage(ActionEvent event) {
        ObservableList<StorageFiles> listOfCloudItems = FXCollections.observableArrayList();
        LinkedList<File> files = new LinkedList<>();
        for (int i = 0; i < cloudStorageFolder.get(cloudStorageFolderLevelCounter - 1).size(); i++) {
            files.add((cloudStorageFolder.get(cloudStorageFolderLevelCounter - 1).get(i)));
        }
        for (int i = 0; i < files.size(); i++) {
            String nameOfLocalFileOrDirectory = files.get(i).getName();
            long initialSizeOfLocalFileOrDirectory = files.get(i).length();
            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date(files.get(i).lastModified()));
            File pathToFileInCloudStorage = new File(files.get(i).getAbsolutePath());
            listOfCloudItems.addAll(new StorageFiles(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathToFileInCloudStorage));
        }
        cloudStorageList.setItems(listOfCloudItems);
        cloudStorageList.setCellFactory(param -> new StorageFilesList());
        cloudStorageFolder.remove(cloudStorageFolderLevelCounter);
        cloudStorageFolderLevelCounter--;
        if (cloudStorageFolderLevelCounter <= 0) {
            previousCloudFolderButton.setVisible(false);
        }

    }
    public void selectAllFilesFromCloudStorage() {
        if (cloudStorageList.getItems().size() == cloudStorageList.getSelectionModel().getSelectedItems().size()) {
            cloudStorageList.getSelectionModel().clearSelection();
        } else {
            cloudStorageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            cloudStorageList.getSelectionModel().selectAll();
        }
    }
    public void updateCloudStoragePanel() {
        ConnectServer.updateRequest(CurrentLogin.getCurrentLogin());
    }
    public void downloadFiles() {
        ConnectServer.downloadRequest(getPathsOfSelectedFilesInCloudStorage());
    }
}
