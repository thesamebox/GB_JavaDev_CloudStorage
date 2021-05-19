import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
@Slf4j
public class UpdateRequest extends Requests {
    private HashMap<Integer, LinkedList<File>> cloudStorageContents;


    public UpdateRequest(HashMap<Integer, LinkedList<File>> cloudStorageContents) {
        this.cloudStorageContents = cloudStorageContents;
    }
    public UpdateRequest(String login){
        super.setLogin(login);
    }

    public HashMap<Integer, LinkedList<File>> getCloudStorageContents() {
        return cloudStorageContents;
    }
}