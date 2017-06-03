package com.netty.show;

import com.netty.show.handler.ContentHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * server client 的基类
 * Created by guzy on 17/5/23.
 */
public abstract class CommonWorker {

    Logger logger=Logger.getLogger(CommonWorker.class);

    protected Selector selector;

    protected volatile Boolean running=true;

    //内容处理器链
    private List<ContentHandler> contentHandlerList=new ArrayList<ContentHandler>();

    String name;

    /**
     * 处理数据内容的线程池
     */
    private ExecutorService executorService;

    /**
     * 主 channel
     */
    SelectableChannel channel;


    public CommonWorker(String name) {
        this.name = name;
        this.executorService= Executors.newFixedThreadPool(10);
    }

    public void addContentHandler(ContentHandler contentHandler){
        contentHandlerList.add(contentHandler);
    }

    /**
     * 处理关闭事件
     * @param selectionKey
     */
    abstract void handleClose(SelectionKey selectionKey);

    public void start(){
        try{
            while(running){
                int count=selector.select();
                if(count>0){
                    Set<SelectionKey> selectionKeys=selector.selectedKeys();
                    for(SelectionKey key:selectionKeys){
                        handleKey(key);
                    }
                    selectionKeys.clear();
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            shutDown();
        }
    }

    abstract void handleKey(SelectionKey key) throws IOException;

    abstract void shutDown();

    void handleReadable(final SelectionKey key, final SocketChannel channel) throws IOException {
        //TODO:扩容，并发
        final ByteBuffer receiveBuffer=key.attachment()==null?ByteBuffer.allocate(1024):(ByteBuffer)key.attachment();

        //读取数据
        int count=channel.read(receiveBuffer);
        if(count>0){
            executorService.execute(new Runnable() {
                public void run() {
                    receiveBuffer.flip();

                    List<Object> results=new ArrayList<Object>();
                    results.add(receiveBuffer);
                    for(ContentHandler handler:contentHandlerList){
                        List<Object> outs=new ArrayList<Object>();
                        Iterator itr=results.iterator();
                        while(itr.hasNext()){
                            handler.read(channel,itr.next(),outs);
                        }
                        results=outs;
                    }

                    for(Object result:results){
                        logger.debug(String.format("接收数据:%s",new String((byte[])result)));
                    }

                }
            });

            channel.register(selector,SelectionKey.OP_WRITE | SelectionKey.OP_READ);////TODO:可能要改成在这里注册写事件
        }else if(count<0){
            //对端链路关闭
            key.cancel();
            channel.close();
            handleClose(key);
        }else{
            //读到0字节
        }
    }

    void writeContent(final SelectionKey selectionKey, final SocketChannel channel, Queue contents){
        final ByteBuffer attach=getAttachment(selectionKey);
        final List list=new ArrayList(contents);
        contents.clear();
        executorService.submit(new Runnable() {

            public void run(){
                try {
                     for(Object content:list){
                         writeContentInner(content);
                     }
//                    if(CommonWorker.this instanceof CommonClient){
//                        channel.register(selector, SelectionKey.OP_WRITE );
//                    }else{
                        channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    //}

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            List<Object> writeContentInner(Object content) throws IOException {
                List<Object> results=new ArrayList<Object>();
                results.add(content);
                logger.debug(String.format("%s 发送 %s",name,content));

                for(ContentHandler handler:contentHandlerList){
                    List<Object> outs=new ArrayList<Object>();
                    for(Object o:results){
                        handler.write(attach, channel, o, outs);
                    }
                    results=outs;
                }

                if(attach!=null){//此时都已经写入到attach中了
                    writeContent(channel,attach);
                }else{
                    for(Object result:results){
                        writeContent(channel,(ByteBuffer)result);
                    }
                }
                return results;
            }
        });
    }

    /**
     * 获取附件
     * @param selectionKey
     * @return
     */
    private ByteBuffer getAttachment(SelectionKey selectionKey){
        if(selectionKey==null){
            return null;
        }
        return (ByteBuffer)selectionKey.attachment();
    }

    /**
     * 底层写方法
     * @param channel
     * @param buffer
     * @throws IOException
     */
    void writeContent(SocketChannel channel, ByteBuffer buffer) throws IOException {
        buffer.flip();
        while(buffer.hasRemaining()){
            channel.write(buffer);
        }
    }

}
