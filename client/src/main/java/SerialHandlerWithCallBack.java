import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

@Slf4j
public class SerialHandlerWithCallBack extends SimpleChannelInboundHandler<Requests> {
    private static Socket socket;
    private final CallBack callBack;
    private static ObjectDecoderInputStream odis;
    private static ObjectEncoderOutputStream oeos;

    static {
        try {
            socket = new Socket("localhost", 8189);
            odis = new ObjectDecoderInputStream(Objects.requireNonNull(socket).getInputStream());
            oeos = new ObjectEncoderOutputStream(Objects.requireNonNull(socket).getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SerialHandlerWithCallBack(CallBack callBack) throws IOException {
        this.callBack = callBack;
    }

    public static void authRequest(String login, String password) throws IOException {
        oeos.writeObject(new AuthRequest(login, password));
        oeos.flush();
    }

    public static void regRequest(String login, String password) throws IOException {
        oeos.writeObject(new RegistrationRequest(login, password));
        oeos.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                Requests request) throws Exception {
        callBack.call(request);
    }
}
