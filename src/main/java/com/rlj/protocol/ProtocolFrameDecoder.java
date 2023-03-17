package com.rlj.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    // 协议定好后参数一般不会变，所以来一个协议级别的，将参数固定
    // 参数1：规定消息最大长度为1024
    // 参数2：约定长度部分的起始位置为12，因为魔数4+版本1+序列化方式1+指令类型1+排序号4+无意义填充1=12，之后才是约定长度部分
    // 参数3：约定长度部分本身占4个字节(协议里我们设置为4)
    // 参数4：约定长度部分结束后还有0个字节后才开始是消息内容(协议里约定长度部分和消息内容是相连的)
    // 参数5：最终截取一段消息后，要把前面几个字节去掉：不去掉，我们会在解码器中进行截取、解析，所以不用在这里截取
    public ProtocolFrameDecoder() {
        this(10240, 12, 4, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
