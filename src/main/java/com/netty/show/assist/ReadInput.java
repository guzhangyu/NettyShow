package com.netty.show.assist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by guzy on 17/5/24.
 */
public class ReadInput {

    public void read(HandlerStr handlerStr) throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        String line=br.readLine();
        while(line!=null){
            if(line.equals("EOF")){
                br.close();
                break;
            }
            handlerStr.handle(line);
            line=br.readLine();
        }
    }
}
