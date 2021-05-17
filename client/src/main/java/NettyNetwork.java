import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyNetwork {

    private SocketChannel clientChannel;
    private final CallBack callBack;
    private static NettyNetwork INSTANCE;

    public static NettyNetwork getInstance(CallBack callBack) {
        if (INSTANCE == null) {
            INSTANCE = new NettyNetwork(callBack);
        }
        return INSTANCE;
    }

    private NettyNetwork(CallBack callBack) {

        this.callBack = callBack;
        new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                clientChannel = socketChannel;
                                socketChannel.pipeline().addLast(
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
                                        new SerialHandlerWithCallBack(callBack)
                                );
                            }
                        });
                ChannelFuture future = bootstrap.connect("localhost", 8189).sync();
                log.debug("client network started");
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("Exception - ", e);
            } finally {
                group.shutdownGracefully();
            }
        }).start();


    }

    public void write(Requests request) {
        clientChannel.writeAndFlush(request);
    }
}
