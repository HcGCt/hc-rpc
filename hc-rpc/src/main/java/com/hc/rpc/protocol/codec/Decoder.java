package com.hc.rpc.protocol.codec;

import com.hc.rpc.common.RpcRequest;
import com.hc.rpc.common.RpcResponse;
import com.hc.rpc.common.constants.MsgType;
import com.hc.rpc.protocol.MsgHeader;
import com.hc.rpc.protocol.RpcMessage;
import com.hc.rpc.protocol.serialize.SerializationFactory;
import com.hc.rpc.protocol.serialize.ISerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.hc.rpc.common.constants.Constants.HEADER_TOTAL_LEN;
import static com.hc.rpc.common.constants.Constants.MAGIC;

/**
 * 解码器
 * TCP流式传输,消息无边界,会出现半包粘包问题
 * @Author hc
 */
public class Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 不合法或还为结束完整消息包
        if (in.readableBytes() < HEADER_TOTAL_LEN) {
            return;
        }
        // 标记当前读取位置，便于后面回退
        in.markReaderIndex();
        // 验证魔数
        short magic = in.readShort();
        if (magic != MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        byte version = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();
        // 序列化方式
        final int len = in.readInt();
        if (in.readableBytes() < len){
            in.resetReaderIndex();  // 回退标记位置,不解析"半包"
            return;
        }
        final byte[] bytes = new byte[len];
        in.readBytes(bytes);
        final String serialization = new String(bytes);

        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();  // 回退标记位置,不解析"半包"
            return;
        }
        // 读取数据
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 消息类型
        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }

        // 消息头
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setSerializations(bytes);
        header.setSerializationLen(len);
        header.setMsgLen(dataLength);

        // 序列化器
        ISerializer serializer = SerializationFactory.get(serialization);
        switch (msgTypeEnum) {
            case REQUEST:
                // 请求
                RpcRequest request = serializer.deserialize(data, RpcRequest.class);
                if (request != null) {
                    RpcMessage<RpcRequest> rpcMessage = new RpcMessage<>();
                    rpcMessage.setHeader(header);
                    rpcMessage.setBody(request);
                    out.add(rpcMessage);
                }
                break;
            case RESPONSE:
                // 响应
                RpcResponse response = serializer.deserialize(data, RpcResponse.class);
                if (response != null) {
                    RpcMessage<RpcResponse> rpcMessage = new RpcMessage<>();
                    rpcMessage.setHeader(header);
                    rpcMessage.setBody(response);
                    out.add(rpcMessage);
                }
                break;
            default:
                break;
        }
    }
}
