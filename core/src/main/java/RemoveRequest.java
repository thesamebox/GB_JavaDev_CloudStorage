import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.LinkedList;
@Slf4j
public class RemoveRequest extends Requests {

    private LinkedList<File> toRemove;

    public RemoveRequest(String login, LinkedList<File> toRemove) {
        super.setLogin(login);
        this.toRemove = toRemove;
    }

    public LinkedList<File> getToRemove() {
        return toRemove;
    }

}
