package com.netty.show;

import com.netty.show.assist.CommonUtils;
import com.netty.show.assist.SelectionKeys;
import com.netty.show.assist.SocketChannelArr;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by guzy on 17/5/23.
 */
public class CommonServer extends CommonWorker {

    Logger logger = Logger.getLogger(CommonServer.class);

    /**
     * 主机-昵称对应map
     */
    ConcurrentHashMap<String, Queue<Object>> hostNickMap = new ConcurrentHashMap<String, Queue<Object>>();

    /**
     * 要通过SelectionKey 写的内容
     */
    ConcurrentHashMap<String, Queue<Object>> toWriteMap = new ConcurrentHashMap<String, Queue<Object>>();


    public CommonServer(int port, String name) throws IOException {
        super(name);
        logger.debug(String.format("Server name:%s", name));

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        channel = serverSocketChannel;
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.debug(String.format("server start:%d", port));
    }

    void handleConnect(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

        selectionKey.attach(ByteBuffer.allocate(1024));
        logger.debug(String.format("client:%s", CommonUtils.getSocketName(client)));

        Queue<String> list = new ArrayBlockingQueue<String>(1);
        list.add("服务器连接反馈！");
        writeContent(null, client, list);
    }


    private void addObjToMap(String key, Object value, ConcurrentHashMap<String, Queue<Object>> map) {
        Queue<Object> queue = map.get(key);
        if (queue == null) {
            queue = new ArrayBlockingQueue<Object>(100);

            Queue<Object> resultQueue = map.putIfAbsent(key, queue);
            if (resultQueue != null) {
                queue = resultQueue;
            }
        }
        queue.add(value);
    }

    synchronized void handleNotWritten(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        Queue<Object> list = toWriteMap.get(CommonUtils.getSocketName(channel));
        if (list != null && list.size() > 0) {
            writeContent(key, channel, list);
            list.clear();
        }
    }

    @Override
    void handleClose(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        logger.debug(String.format("%s close", CommonUtils.getSocketName(channel)));
    }

    @Override
    void handleKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            try {
                handleConnect(key);
            } catch (IOException e) {
                logger.error(e);
                e.printStackTrace();
            }
            return;
        }

        SocketChannel channel = (SocketChannel) key.channel();
        if (key.isReadable()) {
            handleReadable(key, channel);
        } else if (key.isWritable()) {
            handleNotWritten(key);
        }
    }

    @Override
    void shutDown() {
        start();
    }

    public void shutDownReally() throws IOException {
        logger.debug("shut down really");
        running = false;
        selector.close();
        channel.close();
    }

    public void write(String name, Object value) {
        addObjToMap(name, value, toWriteMap);
    }
}
