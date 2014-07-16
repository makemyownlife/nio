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
package com.diyicai.nioapi.net;

import com.diyicai.nioapi.net.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author xianmao.hexm
 */
public class FrontendConnection extends AbstractConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontendConnection.class);
    protected long id;
    protected String host;
    protected int port;
    protected int localPort;
    protected long idleTimeout;
    protected boolean isAccepted;

    public FrontendConnection(SocketChannel channel) {
        super(channel);
        Socket socket = channel.socket();
        this.host = socket.getInetAddress().getHostAddress();
        this.port = socket.getPort();
        this.localPort = socket.getLocalPort();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public void setProcessor(NIOProcessor processor) {
        this.processor = processor;
        this.readBuffer = processor.getBufferPool().allocate();
        processor.addFrontend(this);
    }

    private final static byte[] encodeString(String src, String charset) {
        if (src == null) {
            return null;
        }
        if (charset == null) {
            return src.getBytes();
        }
        try {
            return src.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return src.getBytes();
        }
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[thread=")
                .append(Thread.currentThread().getName())
                .append(",class=")
                .append(getClass().getSimpleName())
                .append(",host=")
                .append(host)
                .append(",port=")
                .append(port).toString();
    }


    public boolean isIdleTimeout() {
        return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime) + idleTimeout;
    }

    @Override
    protected void idleCheck() {
        if (isIdleTimeout()) {
            LOGGER.warn(toString() + " idle timeout");
            close();
        }
    }

    @Override
    public void register(Selector selector) throws IOException {
        LOGGER.info("register");
        super.register(selector);
    }


    @Override
    //处理数据 重载方法
    public void handleData(byte[] data) {
        processor.getHandler().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    write(("你好呀" + System.currentTimeMillis()).getBytes("utf-8"));
                } catch (Throwable e) {
                    handleError(ErrorCode.ERR_HANDLE_DATA, e);
                }
            }
        });
    }

    @Override
    public void handleError(int errCode, Throwable t) {
        //根据异常类型和信息，选择日志输出级别
        if (t instanceof EOFException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(toString(), t);
            }
        } else if (isConnectionReset(t)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(toString(), t);
            }
        } else {
            LOGGER.warn(toString(), t);
        }

        // 异常返回码处理
        switch (errCode) {
            case ErrorCode.ERR_HANDLE_DATA:
                String msg = t.getMessage();
                break;
            default:
                close();
        }

    }

    protected boolean isConnectionReset(Throwable t) {
        if (t instanceof IOException) {
            String msg = t.getMessage();
            return (msg != null && msg.contains("Connection reset by peer"));
        }
        return false;
    }

    @Override
    public boolean close() {
        return super.close();
    }

}
