package pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class StringToByteHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx,
                      Object msg,
                      ChannelPromise promise) throws Exception {
        String message = (String) msg;
        log.debug("received: {}", message);
        ByteBuf buffer = ctx.alloc().directBuffer();
        buffer.writeCharSequence(message, StandardCharsets.UTF_8);
        buffer.retain();

        ctx.writeAndFlush(buffer);
    }
}
