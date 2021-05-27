import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

@Slf4j
public class ConnectServer {

    private static boolean active = true;
    private static Socket socket;
    private static final String HOST = "localhost";
    private static final int PORT = 8189;
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

    public static boolean updateRequest(String login){
        try {
            oeos.writeObject(new UpdateRequest(login));
            oeos.flush();
            return true;
        } catch (IOException e) {
            log.error("Exception - ", e);
        }
        return false;
    }

    public static boolean uploadRequest(String login, LinkedList<File> copyListToCloud) {
        try {
            if (!copyListToCloud.isEmpty()) {
                for (int i = 0; i < copyListToCloud.size(); i++) {
                    Path path = Paths.get(copyListToCloud.get(i).getAbsolutePath());
                    oeos.writeObject(new CopyRequest(login, path));
                    oeos.flush();
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Exception - ", e);
        }
        return false;
    }

    public static boolean deleteRequest(String login, LinkedList<File> filesToDelete) {
        try {
            if (!filesToDelete.isEmpty()) {
                oeos.writeObject(new RemoveRequest(login, filesToDelete));
                oeos.flush();
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            log.error("Exception - ", e);
        }
        return false;
    }

    public static boolean uploadFiles(String login, LinkedList<File> filesToSendToCloud) {
        try {
            if (!filesToSendToCloud.isEmpty()) {
                for (int i = 0; i < filesToSendToCloud.size(); i++) {
                    Path path = Paths.get(filesToSendToCloud.get(i).getAbsolutePath());
                    oeos.writeObject(new CopyRequest(login, path));
                    oeos.flush();
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Exception - ", e);
        }
        return false;
    }

    public static boolean downloadRequest(LinkedList pathsOfSelectedFilesInCloudStorage) {
        try {
            if (!pathsOfSelectedFilesInCloudStorage.isEmpty()){
                oeos.writeObject(new FileRequest(pathsOfSelectedFilesInCloudStorage));
                oeos.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }
}

