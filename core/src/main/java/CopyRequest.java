import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
@Slf4j
public class CopyRequest extends Requests {
    private String fileName;
    private byte[] data;
    private boolean isDirectory;
    private boolean isEmpty;

    public CopyRequest(Path path) throws IOException {
        this.fileName = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
        this.isDirectory = false;
        this.isEmpty = false;
    }

    public CopyRequest(String fileName, boolean isDirectory, boolean isEmpty) {
        this.fileName = fileName;
        this.isDirectory = isDirectory;
        this.isEmpty = isEmpty;
    }
    public CopyRequest(String login, Path path) throws IOException {
        this.fileName = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
        super.setLogin(login);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
