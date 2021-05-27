import java.io.File;
import java.util.LinkedList;

public class FileRequest extends Requests {
    private LinkedList<File> requestedFiles;
    public FileRequest(LinkedList<File> files) {
        this.requestedFiles = files;
    }

    public LinkedList<File> getRequestedFiles() {
        return requestedFiles;
    }
}
