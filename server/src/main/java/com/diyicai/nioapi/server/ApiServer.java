package com.diyicai.nioapi.server;


import com.diyicai.nioapi.common.util.LogUtil;
import com.diyicai.nioapi.net.NIOAcceptor;
import com.diyicai.nioapi.net.NIOProcessor;
import com.diyicai.nioapi.net.factory.FrontendConnectionFactory;
import com.diyicai.nioapi.net.util.ExecutorUtil;
import com.diyicai.nioapi.net.util.NameableExecutor;
import com.diyicai.nioapi.net.util.TimeUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: zhangyong
 * Date: 12-12-19
 * Time: 下午7:44
 * To change this template use File | Settings | File Templates.
 */
public class ApiServer {

    private static final Logger LOGGER = Logger.getLogger(ApiServer.class);

    private static final long LOG_WATCH_DELAY = 60000L;                          //LOG4J 定时查看配置是否更新

    private static final long TIME_UPDATE_PERIOD = 20L;                          //定时更新时间

    private static final long DEFAULT_PROCESSOR_CHECK_PERIOD = 15 * 1000L;     //定时检查处理线程

    public static final String NAME = "EnterpriseApi";

    private NIOAcceptor server;                                                      //前端接收器

    private final Timer timer;                                                      //定时器 timer

    private final long startupTime;

    private final NameableExecutor timerExecutor;

    private NIOProcessor[] processors;

    private static final ApiServer INSTANCE = new ApiServer();

    private ApiServer() {
        this.timer = new Timer(NAME + "Timer", true);
        this.startupTime = TimeUtil.currentTimeMillis();
        timerExecutor = ExecutorUtil.create("TimerExecutor", 4);
    }

    public static final ApiServer getInstance() {
        return INSTANCE;
    }

    public void beforeStart(String dateFormat) {
        String home = System.getProperty("cobar.home");
        LOGGER.info("home==" + home);
        if (home == null) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            LogLog.warn(sdf.format(new Date()) + " [cobar.home] is not set.");
        } else {
            //这里需要注意的是可以log4j自动来观测log4j的变化
            LogUtil.configureAndWatch(home + "/conf/log4j.xml", LOG_WATCH_DELAY);
        }
    }

    public void startup() throws IOException {
        // server startup
        LOGGER.info("===============================================");
        LOGGER.info(NAME + " is ready to startup ...");

        //定时修改当前时间(弱精度)
        timer.schedule(updateTime(), 0L, TIME_UPDATE_PERIOD);

        processors = new NIOProcessor[4];
        for (int i = 0; i < processors.length; i++) {
            processors[i] = new NIOProcessor("Processor" + i, 4, 4);
            processors[i].startup();
        }
        //定时检查处理线程(并且处理空闲时间过程的连接)
        timer.schedule(processorCheck(), 0L, DEFAULT_PROCESSOR_CHECK_PERIOD);

        FrontendConnectionFactory factory = new FrontendConnectionFactory();
        server = new NIOAcceptor(NAME + "Server", 8066, factory);
        server.setProcessors(processors);
        server.start();

        LOGGER.info("===============================================");
    }

    // 系统时间定时更新任务
    private TimerTask updateTime() {
        return new TimerTask() {
            @Override
            public void run() {
                TimeUtil.update();
            }
        };
    }

    // 处理器定时检查任务
    private TimerTask processorCheck() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (NIOProcessor p : processors) {
                            p.check();
                        }
                    }
                });
            }
        };
    }

}


