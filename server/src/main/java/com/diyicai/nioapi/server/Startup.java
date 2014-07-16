package com.diyicai.nioapi.server;

import org.apache.log4j.helpers.LogLog;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 服务启动入口
 * User: zhangyong
 * Date: 12-12-18
 * Time: 下午7:48
 * To change this template use File | Settings | File Templates.
 */
public class Startup {

    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    public static void main(String[] args) {
        try {
            // init
            ApiServer server = ApiServer.getInstance();
            server.beforeStart(dateFormat);

            // startup
            server.startup();
        } catch (Throwable e) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            LogLog.error(sdf.format(new Date()) + " startup error", e);
            System.exit(-1);
        }
    }

}
