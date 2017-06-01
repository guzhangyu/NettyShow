package com.netty.show.handler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * 传递内容处理器
 * Created by guzy on 17/5/23.
 */
public interface ContentHandler {

    Object write(ByteBuffer attach,SocketChannel channel,Object o,List<Object> outs);

    Object read(SocketChannel channel,Object o,List<Object> outs);
}
