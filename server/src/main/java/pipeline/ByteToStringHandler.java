package pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ByteToStringHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buffer = (ByteBuf) msg;
        StringBuilder sBuilder = new StringBuilder();

        while (buffer.isReadable()) {
            sBuilder.append((char)buffer.readByte());
        }

        ctx.fireChannelRead(sBuilder.toString());
    }
}
