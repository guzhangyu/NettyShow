package com.netty.show.assist;

import java.nio.channels.SocketChannel;

/**
 * Created by guzy on 17/5/23.
 */
public class CommonUtils {

    public static String getSocketName(SocketChannel socketChannel){
        return socketChannel.socket().getInetAddress().getHostName();
    }
}
