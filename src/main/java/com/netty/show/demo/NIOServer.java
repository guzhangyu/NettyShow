package com.netty.show.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by guzy on 17/5/26.
 */
public class NIOServer {

    private int flag=0;

    private int BLOCK=4096;

    //发送缓冲
    private ByteBuffer sendBuffer = ByteBuffer.allocate(BLOCK);

    //接收缓冲
    private ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);

    private Selector selector;

    public NIOServer(int port) throws IOException {
        ServerSocketChannel channel=ServerSocketChannel.open();
        channel.configureBlocking(false);

        ServerSocket socket=channel.socket();
        socket.bind(new InetSocketAddress(port));

        selector=Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println(String.format("server start -- :%d ",port));
    }

    private void listen() throws IOException {
        while (true){
            selector.select();

            Set<SelectionKey> keys=selector.selectedKeys();
            Iterator<SelectionKey> iterator=keys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey=iterator.next();
                iterator.remove();
                handleKey(selectionKey);
            }
        }
    }

    private void handleKey(SelectionKey selectionKey) throws IOException {
        SocketChannel client=null;
        if(selectionKey.isAcceptable()){
            ServerSocketChannel server=(ServerSocketChannel)selectionKey.channel();

            client=server.accept();
            client.configureBlocking(false);
            client.register(selector,SelectionKey.OP_READ);
        }else if(selectionKey.isReadable()){
            client=(SocketChannel)selectionKey.channel();

            receiveBuffer.clear();
            int count=client.read(receiveBuffer);
            if(count>0){
                String receiveText=new String(receiveBuffer.array(),0,count);
                System.out.println(String.format("服务端接收数据:%s",receiveText));

                client.register(selector,SelectionKey.OP_WRITE);//TODO:拿对方的注册？
            }
        }else if(selectionKey.isWritable()){
            client=(SocketChannel)selectionKey.channel();

            write(client);

            write(client);
        }
    }

    private void write(SocketChannel client) throws IOException {
        String text="send msg to client"+flag++;
        sendBuffer.clear();
        sendBuffer.put(text.getBytes());
        sendBuffer.flip();
        client.write(sendBuffer);
        System.out.println(String.format("服务端向客户端发送数据:%s",text));

        client.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws IOException {
        new NIOServer(8888).listen();
    }
}
