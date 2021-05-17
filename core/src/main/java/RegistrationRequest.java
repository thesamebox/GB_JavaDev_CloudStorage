import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistrationRequest extends Requests {
    private String password;

    public RegistrationRequest(String login, String password) {
        super.setLogin(login);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
