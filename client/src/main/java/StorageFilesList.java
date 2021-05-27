import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;


import java.util.Objects;

@Slf4j
public class StorageFilesList extends ListCell<StorageFiles> {
    @FXML
    FXMLLoader storageCellLoader;
    @FXML
    private CheckBox checkBox;
    @FXML
    private Label itemName;
    @FXML
    private Label itemSize;
    @FXML
    private Label itemLastChanged;
    @FXML
    private VBox storageCell;

    @Override
    public void updateSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    @Override
    protected void updateItem(StorageFiles item, boolean empty) {
        try {
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (storageCellLoader == null) {
                    storageCellLoader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("CellPage.fxml")));
                    storageCellLoader.setController(this);
                    storageCellLoader.load();
                }
                itemName.setText(item.getName());
                if (item.getSize() / 1073741824 > 0) {
                    itemSize.setText(item.getSize() / 1073741824 + " GB");
                } else if (item.getSize() / 1048576 > 0) {
                    itemSize.setText((item.getSize() / 1048576) + " MB");
                } else if (item.getSize() / 1024 > 0) {
                    itemSize.setText(item.getSize() / 1024 + " KB");
                } else if (item.getSize() / 1024 <= 0) {
                    itemSize.setText(item.getSize() + " bytes");
                }
                checkBox.setSelected(item.isChosen());
                itemLastChanged.setText(item.getLastChangeDate());
                }
            setText(null);
            setGraphic(storageCell);
        } catch (Exception e) {
            log.error("Exception - ", e);
        }
        }
    }

