package com.netty.show;

import com.netty.show.handler.CompleteHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by guzy on 17/5/24.
 */
public class CommonClient extends CommonWorker {

    Logger logger=Logger.getLogger(CommonClient.class);

    //客户端channel
    private SocketChannel socketChannel;

    //要写的内容
    private Queue<Object> toWrites=new ArrayBlockingQueue<Object>(100);

    /**
     * 连接完成的处理器
     */
    CompleteHandler completeHandler;

    public CommonClient(String host,int port,String name) throws IOException {
        super(name);
        socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);

        selector= Selector.open();
        socketChannel.register(selector,SelectionKey.OP_CONNECT);//注册服务器连接兴趣
        socketChannel.connect(new InetSocketAddress(host,port));//要连接的服务器

        this.channel=socketChannel;
    }

    public CommonClient setCompleteHandler(CompleteHandler completeHandler) {
        this.completeHandler = completeHandler;
        return this;
    }


    synchronized void handleNotWritten(SelectionKey key) {
        SocketChannel channel=(SocketChannel)key.channel();
        if(toWrites!=null && toWrites.size()>0){
            writeContent(null, channel, toWrites);
        }
    }

    @Override
    void handleKey(SelectionKey key) throws IOException {
        SocketChannel channel=(SocketChannel)key.channel();

        if(key.isConnectable()){
            if(channel.isConnectionPending()){
                channel.finishConnect();
                logger.debug(String.format("%s 完成连接",name));
                if(completeHandler!=null){
                    completeHandler.handle(channel);
                }
            }
            channel.register(selector,SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            return;
        }

        if(key.isReadable()){
            handleReadable(key,channel);
        }else if(key.isWritable()){
            handleNotWritten(key);
        }
    }

    public void write(Object o){
        toWrites.add(o);
    }

    public void handleClose(SelectionKey selectionKey) {
        logger.debug("server closed!");
        shutDown();
    }

    @Override
    void shutDown() {
        try {
            selector.close();
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
