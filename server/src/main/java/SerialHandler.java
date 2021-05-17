import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

/**Сейчас сделано - сообщения в один клиент.
 * Общая рассылка - чтото складыыать в коллекцию.
 * На сервере завести коллекцию
 * Тут создавать экземляр класса сервер
 * в нем сделать метод "добавить контекст в рассылочный список
 * Таким образом можно будет итерироваться по всем контекстами и делать широкое вещание
 * */

@Slf4j
public class SerialHandler extends SimpleChannelInboundHandler<Requests> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client accepted");
    }

    @Override
    public void channelRead0(ChannelHandlerContext chc, Requests request) throws Exception {
        if (request == null) {
            return;
        }
        if (request instanceof RegistrationRequest) {
            RegistrationRequest registrationRequest = (RegistrationRequest) request;
            String login = registrationRequest.getLogin();
            String pass = registrationRequest.getPassword();
            DBConnectionHandler dbch = new DBConnectionHandler();
            if (dbch.registeredLogin(login)) {
                chc.writeAndFlush(CommandList.LOGIN_IS_TAKEN);
                log.debug(String.format("The user with login %s registered already", login));
            } else {
                dbch.registration(login, pass);
                File newUserDirectory = new File("TestFiles\\ServerStorage\\" + login);
                newUserDirectory.mkdir();
                chc.writeAndFlush("Registration is successful");
                log.debug(String.format("The user %s finished the registration successfully", login));
            }
            dbch.closeDB();
            return;
        }
        if (request instanceof AuthRequest) {
            AuthRequest authRequest = (AuthRequest) request;
            String login = authRequest.getLogin();
            String pass = authRequest.getPassword();
            DBConnectionHandler dbch = new DBConnectionHandler();
            if (dbch.registeredLogin(login)) {
                if (dbch.registeredPassword(pass)) {
                    chc.writeAndFlush(String.format("User %s Accepted", login));
                    log.debug(String.format("User %s was authorized to the server", login));
                } else {
                    chc.writeAndFlush("Wrong password");
                    log.debug(String.format("Failure attempt to access the login %s with password", login));
                }
            } else {
                chc.writeAndFlush(String.format("A user with login %s has not been registered", login));
            }
            dbch.closeDB();
            return;
        }
        if (request instanceof UpdateRequest) {
            UpdateRequest updateRequest = (UpdateRequest) request;
            String login = updateRequest.getLogin();
            chc.writeAndFlush(new UpdateRequest(getContentOfTheStorage(login)));
            return;
        }
        if (request instanceof RemoveRequest) {
            RemoveRequest removeRequest = (RemoveRequest) request;
            String login = removeRequest.getLogin();
            for (int i = 0; i < removeRequest.getToRemove().size(); i++) {
                File toRemove = new File(removeRequest.getToRemove().get(i).getAbsolutePath());
                if (toRemove.isDirectory()) {
                    removeRecursively(toRemove);
                } else {
                    toRemove.delete();
                }
            }
            removeRequest.getToRemove().clear();
            if (removeRequest.getToRemove().isEmpty()) {
                chc.writeAndFlush(new UpdateRequest(getContentOfTheStorage(login)));
            }
            return;
        }
        if (request instanceof CopyRequest) {
            CopyRequest copyRequest = (CopyRequest) request;
            String login = copyRequest.getLogin();
            Path pathToStorage = Paths.get(
                    "TestFiles\\ServerStorage\\" +
                            copyRequest.getLogin() +
                            "\\" +
                            copyRequest.getFileName()
            );
            if (copyRequest.isDirectory() && copyRequest.isEmpty()) {
                if (Files.exists(pathToStorage)) {
                    log.debug("The file is already exists");
                } else {
                    Files.createDirectory(pathToStorage);
                }
            } else {
                if (Files.exists(pathToStorage)) {
                    log.debug("The file is already exists");
                } else {
                    Files.write(Paths.get(
                            "TestFiles\\ServerStorage\\" +
                                    copyRequest.getLogin() +
                                    "\\" +
                                    copyRequest.getFileName()
                            ),
                            copyRequest.getData());
                }
            }
            chc.writeAndFlush(new UpdateRequest(getContentOfTheStorage(login)));
            return;
        }


    }
    private void removeRecursively(File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                removeRecursively(f);
                log.debug("File " + f.getName() + " has been removed from directory");
            }
        }
        if (!file.delete()) {
            log.error("The file" + file.getName() + " has been deleted already");
        }
    }

    public HashMap<Integer, LinkedList<File>> getContentOfTheStorage(String login) {
        HashMap<Integer, LinkedList<File>> cloudStorageContent = new HashMap<>();
        LinkedList<File> filesList = new LinkedList<>();
        File path = new File("TestFiles\\ServerStorage\\" + login);
        File[] files = path.listFiles();
        if (files.length == 0) {
            cloudStorageContent.clear();
        } else {
            filesList.clear();
            for (File file : files) {
                filesList.add(file);
            }
            cloudStorageContent.clear();
            cloudStorageContent.put(0, filesList);
        }
        return cloudStorageContent;
    }
}
