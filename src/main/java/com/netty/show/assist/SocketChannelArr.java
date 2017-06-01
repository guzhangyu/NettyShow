package com.netty.show.assist;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by guzy on 17/5/23.
 */
public class SocketChannelArr {

    Map<String,Queue<SocketChannel>> map=new HashMap<String, Queue<SocketChannel>>();

    public void add(SocketChannel channel){
        String name=CommonUtils.getSocketName(channel);
        synchronized (map){
            Queue<SocketChannel> list=map.get(name);
            if(list==null){
                list=new ArrayBlockingQueue<SocketChannel>(100);
                map.put(name,list);
            }
            if(list.contains(channel)){
                return;
            }

            list.add(channel);
        }
    }

    public int size(){
        return map.size();
    }

    public Map<String, Queue<SocketChannel>> getMap() {
        return map;
    }

    public void remove(SocketChannel socketChannel){
        String name=CommonUtils.getSocketName(socketChannel);

        synchronized (map){
            Queue<SocketChannel> queue=map.get(name);
            if(queue==null){
                return;
            }
            if(queue.contains(socketChannel)){
                queue.remove(socketChannel);
            }
            if(queue.isEmpty()){
                map.remove(name);
            }
        }
    }

    public Collection<SocketChannel> get(String name){
        return map.get(name);
    }
}
