package com.rlj.protocol;

import com.rlj.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-18
 */
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                // 避免黏包、半包问题，使用预设长度解码器(最后一个不需要去掉，因为我们的解码器自己会解析，所以全都拿)
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(),
                new MessageCodec());
        // 出站发送，就要编码，因此来一个对象
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        // 之后在出站时channel的MessageCodec()的出站编码就会起作用，对上面的对象进行编码
        channel.writeOutbound(message);


        // 入站接收，就要解码，需要一个字节数组，就调用上面的"出站发送"编码拿到的字节数组
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,buf); // message编码拿到的字节数组会输出到前面的buf中
        // 这个时候buf就有数据了，进行解码
        channel.writeInbound(buf);
    }
}
