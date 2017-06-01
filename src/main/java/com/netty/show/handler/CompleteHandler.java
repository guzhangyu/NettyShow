package com.netty.show.handler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by guzy on 17/5/24.
 */
public interface CompleteHandler {

    void handle(SocketChannel socketChannel) throws IOException;
}
