package com.netty.show.assist;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务端的SelectionKey列表，与客户端socket一一对应
 * Created by guzy on 17/5/23.
 */
public class SelectionKeys {

    private Map<String,SelectionKey> selectionKeyMap=new HashMap<String, SelectionKey>();

    public void add(String name,SelectionKey selectionKey){
        selectionKeyMap.put(name,selectionKey);
    }

    public Boolean containsValue(SelectionKey key){
        return selectionKeyMap.containsValue(key);
    }

    public Boolean containsKey(String key){
        return selectionKeyMap.containsKey(key);
    }


    public void add(SelectionKey selectionKey){
        SocketChannel channel=(SocketChannel)selectionKey.channel();
        add(channel.socket().getInetAddress().getHostName(),selectionKey);
    }

    public Map<String,SelectionKey> getMap(){
        return selectionKeyMap;
    }

    public void remove(String key){
        selectionKeyMap.remove(key);
    }

    public SelectionKey get(String key){
        return selectionKeyMap.get(key);
    }
}
