import java.io.File;

public class StorageFiles {
    private String name;
    private long size;
    private boolean isChosen;
    private String lastChangeDate;
    private File pathToFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public String getLastChangeDate() {
        return lastChangeDate;
    }

    public void setLastChangeDate(String lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }

    public File getPathToFile() {
        return pathToFile;
    }

    public StorageFiles(String name, long size, boolean isChosen, String lastChangeDate, File pathToFile) {
        this.name = name;
        this.size = size;
        this.isChosen = isChosen;
        this.lastChangeDate = lastChangeDate;
        this.pathToFile = pathToFile;
    }
}
