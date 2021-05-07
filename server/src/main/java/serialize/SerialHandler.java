package serialize;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.Message;
@Slf4j
public class SerialHandler extends SimpleChannelInboundHandler<Message> {

    /**Сейчас сделано - сообщения в один клиент.
     * Общая рассылка - чтото складыыать в коллекцию.
     * На сервере завести коллекцию
     * Тут создавать экземляр класса сервер
     * в нем сделать метод "добавить контекст в рассылочный список
     * Таким образом можно будет итерироваться по всем контекстами и делать широкое вещание
     * */

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                Message message) throws Exception {
        ctx.writeAndFlush(message);
        log.debug("received: {}", message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client accepted");
    }
}
