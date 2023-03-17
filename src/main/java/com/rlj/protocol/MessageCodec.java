package com.rlj.protocol;

import com.rlj.config.Config;
import com.rlj.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-17
 */
// 消息的编解码器
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    // 消息发出前将自定义的Message编码为ByteBuf
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        // 下面只需要将传入的消息本体、以及自定义协议的其他要素，都写入传入的ByteBuf中，发送的时候就会将发送的消息按照下面要素组成的自定义协议发送
        // 1、指定魔数(暗号)为1234，占4字节
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        // 2、指定版本为1，占1字节
        byteBuf.writeByte(1);
        // 3、指定序列化算法(方式)，因为算法有很多种，最好来个枚举类：JDK-0 JSON-1 等，然后在配置文件application.properties中配置算法，
        // 然后在配置类Config的方法getSerializerAlgorithm()中读取。返回的是枚举对象，如何拿到0、1等int类型？ordinal()即可，返回枚举对
        // 象在枚举类中的顺序，所以为了保证JDK-0 JSON-1，在枚举类中JDK的要写在第一个，JSON的要写在第二个
        byteBuf.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 4、指定指令类型，占1字节
        byteBuf.writeByte(message.getMessageType());
        // 5、消息序列号(暂时不考虑双工通信)，占4字节
        byteBuf.writeInt(message.getSequenceId());
        // 附1：因为 魔数4+版本1+序列化方式1+指令类型1+消息序列号4+正文长度4=15，一般字节数都应是2的整数倍，所以这里补一个无意义的字节
        byteBuf.writeByte(0xff);
        // 附2：获取内容的字节数组(Java对象 ---> 字节数组)，供下面的6、7使用
        // 底层实际会将message写入对象输出流ObjectOutputStream，对象输出流又会写入字节数组输出流ByteArrayOutputStream，最终输出字节数组
        byte[] bytes = Config.getSerializerAlgorithm().serialize(message);
        // 6、正文长度
        byteBuf.writeInt(bytes.length);
        // 7、消息正文
        byteBuf.writeBytes(bytes);
    }

    // 将接收到的ByteBuf解码为自定义的Message
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 接收的byteBuf和上面的格式一致，按照上面的来即可(用readXXX，getXXX读取后指针不会移动的)
        // 1、魔数4字节，直接readInt()
        int magicNum = byteBuf.readInt();
        // 2、版本1字节，直接readByte()
        byte version = byteBuf.readByte();
        // 3、序列化算法(方式)1字节，直接readInt()
        byte serializerType = byteBuf.readByte();
        // 4、指定指令类型1字节，直接readByte()
        byte messageType = byteBuf.readByte();
        // 5、消息序列号4字节，直接readInt()
        int sequenceId = byteBuf.readInt();
        // 附：无意义的1字节，直接readByte()让指针往后移
        byteBuf.readByte();
        // 6、正文长度4字节，直接readInt()拿到正文长度
        int length = byteBuf.readInt();
        // 7、消息正文length字节，直接readBytes()拿到字节数组，然后用上面serializerType规定的是JDK、Java等算法来反序列化为Java对象
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes,0,length);
        // 找到用的是哪个算法进行反序列化。传过来为0就用第一个，传过来为1就用第二个，所以枚举类中枚举对象顺序一定要对
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerType];
        // 确定具体消息类型
        Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        Object message = algorithm.deserialize(messageClass,bytes);
        // log.debug("{},{},{},{},{},{}",magicNum,version,serializerType,messageType,sequenceId,length);
        // log.debug("{}",message);
        list.add(message); // 加入到参数中的List<Object> list，以便传递给下一个参数使用
    }
}
