import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class SerialHandlerWithCallBack extends SimpleChannelInboundHandler<Requests> {

    private final CallBack callBack;

    @Override
    protected void channelRead0(ChannelHandlerContext chc,
                                Requests request) throws Exception {
        callBack.call(request);
    }
}
