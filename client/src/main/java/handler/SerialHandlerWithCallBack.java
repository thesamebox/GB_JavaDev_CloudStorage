package handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Message;

@Slf4j
@AllArgsConstructor
public class SerialHandlerWithCallBack extends SimpleChannelInboundHandler<Message> {

    private final CallBack callBack;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                Message message) throws Exception {
        callBack.call(message);
    }
}
