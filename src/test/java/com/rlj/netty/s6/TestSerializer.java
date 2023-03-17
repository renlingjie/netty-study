package com.rlj.netty.s6;

import com.rlj.config.Config;
import com.rlj.message.LoginRequestMessage;
import com.rlj.message.Message;
import com.rlj.protocol.MessageCodec;
import com.rlj.protocol.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-20
 */
public class TestSerializer {
    public static void main(String[] args)  {
        MessageCodec CODEC = new MessageCodec();
        LoggingHandler LOGGING = new LoggingHandler();
        EmbeddedChannel channel = new EmbeddedChannel(LOGGING, CODEC, LOGGING);

        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        // 序列化
        channel.writeOutbound(message);
        // 反序列化
        //ByteBuf buf = messageToByteBuf(message);
        //channel.writeInbound(buf);
    }

    public static ByteBuf messageToByteBuf(Message msg) {
        int algorithm = Config.getSerializerAlgorithm().ordinal();
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(new byte[]{1, 2, 3, 4});
        out.writeByte(1);
        out.writeByte(algorithm);
        out.writeByte(msg.getMessageType());
        out.writeInt(msg.getSequenceId());
        out.writeByte(0xff);
        byte[] bytes = Serializer.Algorithm.values()[algorithm].serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        return out;
    }
}
