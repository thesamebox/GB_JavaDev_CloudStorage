import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ConnectServer {

    private static boolean active = true;
    private static Socket socket;
    private static String HOST = "localhost";
    private static int PORT = 8189;
    private static ObjectEncoderOutputStream oeos;
    private static ObjectDecoderInputStream odis;

    public static void setConnection() {
        try {
            socket = new Socket(HOST, PORT);
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            log.debug("client network started");
        } catch (IOException e) {
            log.error("Exception - ", e);
        }
    }

    public static Object getResponse() throws IOException, ClassNotFoundException {
        return odis.readObject();
    }

    public static boolean isActive() {
        return active;
    }

    public static void disconnect() {
        active = false;
        try {
            oeos.close();
            odis.close();
            socket.close();
            Platform.exit();
        } catch (IOException e) {
            log.error("Exception - ", e);
        }
    }

    public static void AuthRequest(String login, String password) {
        try {
            oeos.writeObject(new AuthRequest(login,password));
            oeos.flush();
        } catch (IOException e) {
            log.error("Exception - ", e);
        }
    }

    public static void RegRequest(String login, String password) {
        try {
            oeos.writeObject(new RegistrationRequest(login, password));
            oeos.flush();
        } catch (IOException e) {
            log.error("Exception - ", e);
        }
    }


}
