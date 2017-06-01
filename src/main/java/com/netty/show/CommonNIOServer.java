package com.netty.show;

import com.netty.show.assist.HandlerStr;
import com.netty.show.assist.ReadInput;
import com.netty.show.handlers.HalfContentHandler;
import com.netty.show.handlers.NormalContentHandler;

import java.io.IOException;

/**
 * Created by guzy on 17/5/24.
 */
public class CommonNIOServer extends CommonServer {
    public CommonNIOServer(int port, String name) throws IOException {
        super(port, name);
    }

    public static void main(String[] args) throws IOException {
        int port=8888;
        final CommonNIOServer server=new CommonNIOServer(8888,"server");

        //server.addContentHandler(new NormalContentHandler());
        server.addContentHandler(new HalfContentHandler());

        new Thread(new Runnable() {
            public void run() {
                try {
                    handleInput();
                } catch (IOException e) {
                    e.printStackTrace();
                    run();
                }
            }

            void handleInput() throws IOException {
                new ReadInput().read(new HandlerStr() {
                    public void handle(String str) {
                        String[] strs=str.split(" ");
                        server.write(strs[0],strs[1]);
                    }
                });
            }
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    server.shutDownReally();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        server.start();
    }
}
