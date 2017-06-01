package com.netty.show.handlers;

import com.netty.show.handler.ContentHandler;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by guzy on 17/5/29.
 */
public class NormalContentHandler implements ContentHandler {
    Logger logger=Logger.getLogger(NormalContentHandler.class);

    public Object write(ByteBuffer attach, SocketChannel channel, Object o, List<Object> outs) {
        byte[] result=((String)o).getBytes();

        ByteBuffer buffer=attach==null?ByteBuffer.allocate(result.length):attach;
        buffer.put(result);
        logger.debug(String.format("before write,outs'size:%d ",outs.size()));
        outs.add(buffer);
        logger.debug(String.format("after write,outs'size:%d ",outs.size()));

        return result;
    }

    public Object read(SocketChannel channel, Object o, List<Object> outs) {
        ByteBuffer buffer=(ByteBuffer)o;
        logger.debug(String.format("before read,outs'size:%d ",outs.size()));

        int len=buffer.limit();
        byte[]arr=new byte[len];
        buffer.get(arr,0,len);

        outs.add(arr);

        //多出来的数据会不会有问题?
        logger.debug(String.format("after read,outs'size:%d ",outs.size()));
        return null;
    }
}
