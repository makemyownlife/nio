package com.diyicai.nioapi.net;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: zhangyong
 * Date: 13-1-1
 * Time: 下午5:05
 * To change this template use File | Settings | File Templates.
 */
public class For {

    public static void main(String[] args) {
        for (; ; ) {
            System.out.println(System.currentTimeMillis() + 123 + new Random().nextInt());
            BlockingQueue<Map> writeQueue = new LinkedBlockingQueue<Map>();
            Map c = null;
            try {
                System.out.println("123");
                if ((c = writeQueue.take()) != null) {
                    System.out.println("writeQueue==" + writeQueue);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
