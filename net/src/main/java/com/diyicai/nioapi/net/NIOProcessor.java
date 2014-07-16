package com.diyicai.nioapi.net;

import com.diyicai.nioapi.net.buffer.BufferPool;
import com.diyicai.nioapi.net.util.ExecutorUtil;
import com.diyicai.nioapi.net.util.NameableExecutor;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * NIO 通过线程池来处理
 * User: zhangyong
 * Date: 12-12-20
 * Time: 下午7:47
 * To change this template use File | Settings | File Templates.
 */
public class NIOProcessor {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024 * 16;
    private static final int DEFAULT_BUFFER_CHUNK_SIZE = 4096;
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final String name;

    private final ConcurrentMap<Long, FrontendConnection> frontends;

    private final NameableExecutor executor;

    private final NameableExecutor handler;

    private final NIOReactor reactor;

    private final BufferPool bufferPool;

    private long netOutBytes;

    public NIOProcessor(String name) throws IOException {
        this(name, DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_CHUNK_SIZE, AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS);
    }

    public NIOProcessor(String name, int handler, int executor) throws IOException {
        this(name, DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_CHUNK_SIZE, handler, executor);
    }

    public NIOProcessor(String name, int buffer, int chunk, int handler, int executor) throws IOException {
        this.name = name;
        this.reactor = new NIOReactor(name);
        this.bufferPool = new BufferPool(buffer, chunk);
        this.handler = (handler > 0) ? ExecutorUtil.create(name + "-H", handler) : null;
        this.executor = (executor > 0) ? ExecutorUtil.create(name + "-E", executor) : null;
        this.frontends = new ConcurrentHashMap<Long, FrontendConnection>();
    }

    /**
     * 添加前端映射
     */
    public void addFrontend(FrontendConnection c) {
        frontends.put(c.getId(), c);
    }

    public String getName() {
        return name;
    }

    public BufferPool getBufferPool() {
        return bufferPool;
    }

    public NameableExecutor getHandler() {
        return handler;
    }

    public NameableExecutor getExecutor() {
        return executor;
    }

    //start processor
    public void startup() {
        reactor.startup();
    }

    public void postRegister(NIOConnection c) {
        reactor.postRegister(c);
    }

    public long getNetOutBytes() {
        return netOutBytes;
    }

    public void addNetOutBytes(long bytes) {
        netOutBytes += bytes;
    }

    /**
     * 定时执行该方法，回收部分资源。
     */
    public void check() {
        frontendCheck();
    }

    // 前端连接检查
    private void frontendCheck() {
        Iterator<Map.Entry<Long, FrontendConnection>> it = frontends.entrySet().iterator();
        while (it.hasNext()) {
            FrontendConnection c = it.next().getValue();

            // 删除空连接
            if (c == null) {
                it.remove();
                continue;
            }

            // 清理已关闭连接，否则空闲检查。
            if (c.isClosed()) {
                it.remove();
                c.cleanup();
            } else {
                c.idleCheck();
            }
        }
    }

    public void postWrite(NIOConnection c) {
        reactor.postWrite(c);
    }

}
