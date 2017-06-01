package com.netty.show.handlers;

import com.netty.show.handler.ContentHandler;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * 半包、粘包处理器
 * Created by guzy on 17/5/24.
 */
public class HalfContentHandler implements ContentHandler {

    Logger logger=Logger.getLogger(HalfContentHandler.class);

    public Object write(ByteBuffer attach, SocketChannel channel, Object o, List<Object> outs) {
        byte[] result=((String)o).getBytes();

        ByteBuffer buffer=attach==null?ByteBuffer.allocate(result.length+4):attach;
        buffer.putInt(result.length);
        buffer.put(result);
        logger.debug(String.format("before write,outs'size:%d ",outs.size()));
        outs.add(buffer);
        logger.debug(String.format("after write,outs'size:%d ",outs.size()));

        return result;
    }

    public Object read(SocketChannel channel, Object o, List<Object> outs) {
        ByteBuffer buffer=(ByteBuffer)o;
        int len=0,curLen=0;
        logger.debug(String.format("before read,outs'size:%d ",outs.size()));

        do{
            curLen+=4;
            len=buffer.getInt();

            byte[]arr=new byte[len];
            buffer.position(curLen);
            buffer.get(arr,0,len);

            outs.add(arr);
            curLen+=len;
        }while (curLen<buffer.limit());

        //多出来的数据会不会有问题?
        logger.debug(String.format("after read,outs'size:%d ",outs.size()));
        return null;
    }
}
