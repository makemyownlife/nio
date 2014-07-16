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

import com.diyicai.nioapi.net.factory.FrontendConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * 前端接收线程(单线程)
 *
 * @author xianmao.hexm
 */
public final class NIOAcceptor extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(NIOAcceptor.class);

    private static final AcceptIdGenerator ID_GENERATOR = new AcceptIdGenerator();

    private final int port;

    private final Selector selector;

    private final ServerSocketChannel serverChannel;

    private FrontendConnectionFactory factory;

    private NIOProcessor[] processors;

    private int nextProcessor;

    public NIOAcceptor(String name, int port, FrontendConnectionFactory factory) throws IOException {
        super.setName(name);
        this.port = port;
        this.selector = Selector.open();                                    // 创建Selector对象
        this.serverChannel = ServerSocketChannel.open();                   // 创建一个ServerSocketChannel对象
        this.serverChannel.socket().bind(new InetSocketAddress(port));    //  可以顺利绑定相同的端口
        this.serverChannel.configureBlocking(false);                      //  工作于非阻塞模式
        this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.factory = factory;
    }

    private long acceptCount;

    public long getAcceptCount() {
        return acceptCount;
    }

    public void setProcessors(NIOProcessor[] processors) {
        this.processors = processors;
    }

    @Override
    public void run() {
        final Selector selector = this.selector;
        for (; ; ) {
            ++acceptCount;
            try {
                selector.select(1000L);
                Set<SelectionKey> keys = selector.selectedKeys();
                try {
                    for (SelectionKey key : keys) {
                        if (key.isValid() && key.isAcceptable()) {
                            accept();
                        } else {
                            LOGGER.info("cancel");
                            key.cancel();
                        }
                    }
                } finally {
                    keys.clear();
                }
            } catch (Throwable e) {
                LOGGER.warn(getName(), e);
            }
        }
    }

    private void accept() {
        SocketChannel channel = null;
        try {
            channel = serverChannel.accept();
            channel.configureBlocking(false);
            FrontendConnection c = factory.make(channel);
            c.setAccepted(true);
            c.setId(ID_GENERATOR.getId());
            NIOProcessor processor = nextProcessor();
            c.setProcessor(processor);
            processor.postRegister(c);
        } catch (Throwable e) {
            closeChannel(channel);
            LOGGER.warn(getName(), e);
        }
    }

    private NIOProcessor nextProcessor() {
        if (++nextProcessor == processors.length) {
            nextProcessor = 0;
        }
        return processors[nextProcessor];
    }

    private static void closeChannel(SocketChannel channel) {
        if (channel == null) {
            return;
        }
        Socket socket = channel.socket();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        try {
            channel.close();
        } catch (IOException e) {
        }
    }


    /**
     * 前端连接ID生成器
     *
     * @author xianmao.hexm
     */
    private static class AcceptIdGenerator {

        private static final long MAX_VALUE = 0xffffffffL;

        private long acceptId = 0L;
        private final Object lock = new Object();

        private long getId() {
            synchronized (lock) {
                if (acceptId >= MAX_VALUE) {
                    acceptId = 0L;
                }
                return ++acceptId;
            }
        }
    }

}
