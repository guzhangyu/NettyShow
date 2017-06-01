package com.netty.show;

import com.netty.show.assist.HandlerStr;
import com.netty.show.assist.ReadInput;
import com.netty.show.handler.CompleteHandler;
import com.netty.show.handlers.HalfContentHandler;
import com.netty.show.handlers.NormalContentHandler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by guzy on 17/5/24.
 */
public class CommonNIOClient extends CommonClient {
    public CommonNIOClient(String host, int port, String name) throws IOException {
        super(host, port, name);
    }

    public static void main(String[] args) throws IOException {
        final CommonNIOClient client=new CommonNIOClient("127.0.0.1",8888,"client");

        client.setCompleteHandler(new CompleteHandler() {
            public void handle(SocketChannel socketChannel) throws IOException {
                client.write("hello server");
            }
        })//.addContentHandler(new NormalContentHandler());
        .addContentHandler(new HalfContentHandler());//增加内容过滤器

        new Thread(new Runnable() {
            public void run() {
                client.start();
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    new ReadInput().read(new HandlerStr() {
                        public void handle(String str) {
                            client.write(str);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
