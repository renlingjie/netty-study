package com.rlj.netty.s5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @author Renlingjie
 * @name
 * @date 2023-02-03
 */
@Slf4j
public class EventLoopServer {
    @Data
    @AllArgsConstructor
    static class Student{
        private String name;
    }
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 连接建立后执行
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 1、通过Channel拿到Pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        // 2、添加处理器Handler，其内部结构实际为：headHandler --> inHandler1 --> inHandler2 -->
                        // inHandler3 --> outHandler4 --> outHandler5 --> outHandler6 --> tailHandler
                        pipeline.addLast("inHandler1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 工序1：将接收到的客户端Object类型，实际为ByteBuf(原材料)的数据，进行第一道加工，变为String
                                log.debug("inHandler1");
                                ByteBuf buf = (ByteBuf) msg;
                                String name = buf.toString(Charset.defaultCharset());
                                super.channelRead(ctx,name); // 将该工序加工的数据给下一道工序
                            }
                        });
                        pipeline.addLast("inHandler2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object name) throws Exception {
                                // 工序2：将接收到的上一道工序Object类型，实际为String的数据，进行第二道加工，构造出Student对象
                                log.debug("inHandler2");
                                Student student = new Student(name.toString());
                                super.channelRead(ctx,student); // 将该工序加工的数据给下一道工序
                            }
                        });
                        pipeline.addLast("inHandler3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object student) throws Exception {
                                // 工序3：将接收到的上一道工序Object类型，实际为Student的数据，进行第三道加工，打印Student对象
                                log.debug("inHandler3，结果:{},class:{}",student,student.getClass());
                                // 不用再执行super.channelRead(ctx,student)了，因为下一道工序不再是ChannelInboundHandlerAdapter
                                // 但是只有在入站Handler中有使用channel.writeAndFlush()，下面出站的Handler才能正常执行，所以给下面的
                                // 出站Handler发送一条数据，这段代码意思是创建一个ByteBuf，内容是"server..."的字节数组，将此ByteBuf
                                // 发送给下面的出站Handler
                                ch.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
                            }
                        });
                        pipeline.addLast("outHandler4",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                // 工序4
                                log.debug("outHandler4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("outHandler5",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                // 工序5
                                log.debug("outHandler5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("outHandler6",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                // 工序6
                                log.debug("outHandler6");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
