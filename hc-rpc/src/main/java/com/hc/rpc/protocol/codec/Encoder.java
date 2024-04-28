package com.hc.rpc.protocol.codec;

import com.hc.rpc.protocol.MsgHeader;
import com.hc.rpc.protocol.RpcMessage;
import com.hc.rpc.protocol.serialize.SerializationFactory;
import com.hc.rpc.protocol.serialize.ISerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 * @Author hc
 */
public class Encoder extends MessageToByteEncoder<RpcMessage<Object>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage<Object> msg, ByteBuf byteBuf) throws Exception {
        MsgHeader header = msg.getHeader();             // 消息头
        byteBuf.writeShort(header.getMagic());          // 魔数
        byteBuf.writeByte(header.getVersion());         // 版本
        byteBuf.writeByte(header.getMsgType());         // 消息类型
        byteBuf.writeByte(header.getStatus());          // 状态
        byteBuf.writeLong(header.getRequestId());       // 可用于回调
        byteBuf.writeInt(header.getSerializationLen());
        // 序列化方式
        final byte[] ser = header.getSerializations();
        final String serialization = new String(ser);
        byteBuf.writeBytes(ser);
        ISerializer serializer = SerializationFactory.get(serialization);
        byte[] data = serializer.serialize(msg.getBody());
        byteBuf.writeInt(data.length);                  // 数据长度
        byteBuf.writeBytes(data);
    }
}
