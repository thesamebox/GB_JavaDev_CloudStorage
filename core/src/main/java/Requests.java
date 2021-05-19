import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
@Slf4j
public abstract class Requests implements Serializable {
    private String login;
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
