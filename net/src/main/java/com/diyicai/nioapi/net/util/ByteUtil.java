/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diyicai.nioapi.net.util;


/**
 * 字节处理的工具类
 */
public class ByteUtil {

    public static byte[] intTo2Bytes(int i) {
        byte[] buf = new byte[2];
        buf[0] = (byte) ((i >>> 8));
        buf[1] = (byte) (i);
        return buf;
    }

    /**
     * 也可以写成   byte[] buf = intTo2Bytes(400);
             int length = buf[1] & 0xff;
             length |= (buf[0] & 0xff) << 8;
             System.out.println(length);
     参考的是 cobar的写法
     * **/
    public static int twoBytesToInt(byte[] buf) {
        return ((buf[0] & 0xff) << 8) + (buf[1] & 0xff);
    }

    public static byte[] addBytes(byte[] initBytes, byte[] addBytes) {
        byte[] b = new byte[initBytes.length + addBytes.length];
        try {
            System.arraycopy(initBytes, 0, b, 0, initBytes.length);
            for (int i = 0; i < addBytes.length; i++) {
                b[i + initBytes.length] = addBytes[i];
            }
        } catch (Exception e) {

        }
        return b;
    }


    public static void main(String[] args) {
        byte[] buf = intTo2Bytes(400);
        int length = buf[1] & 0xff;
        length |= (buf[0] & 0xff) << 8;
        System.out.println(length);
    }

}
