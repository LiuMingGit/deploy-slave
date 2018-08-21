package com.bsoft.deploy.netty.client;

import com.bsoft.deploy.command.CmdLineFactory;
import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Order;
import com.bsoft.deploy.exception.CommandExecuteException;

import java.util.HashMap;
import java.util.Map;

/**
 * 指令执行者
 * Created on 2018/8/20.
 *
 * @author yangl
 */
public class OrderRunner {

    public static Map<String, Object> execute(Order order) throws CommandExecuteException {
        Map<String, Object> respData = new HashMap<>();
        respData.put("code", 200);
        String type = order.getType();
        switch (type) {
            case Constant.CMD_IS_TOMCAT_RUN:
                isTomcatRun(order.getReqData(), respData);
                break;
            case Constant.CMD_TOMCAT_START:
                startTomcat(order.getReqData(), respData);
                break;
            case Constant.CMD_TOMCAT_STOP:
                stopTomcat(order.getReqData(), respData);
                break;
            case Constant.CMD_RELOAD_CACHE:
                reloadCache();
                break;
            case Constant.CMD_THREAD_DUMP:
                threadDump(order.getReqData(), respData);
                break;
        }
        // 去除请求数据,减少传输量
        order.setReqData(null);
        return respData;
    }

    private static void isTomcatRun(Map reqData, Map respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        boolean isRun = CmdLineFactory.getInstance().isTomcatRunning(port);
        respData.put("isRun", isRun);
    }

    private static void startTomcat(Map reqData, Map respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        boolean isRun = CmdLineFactory.getInstance().isTomcatRunning(port);
        if (isRun) {
            respData.put("isRun", isRun);
            return;
        }
        String tomcatHome = Global.getSlaveStore().getSlaveApp(slaveAppId).getAppTomcatHome();
        CmdLineFactory.getInstance().startupTomcat(tomcatHome);

    }

    private static void stopTomcat(Map reqData, Map respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        boolean isRun = CmdLineFactory.getInstance().isTomcatRunning(port);
        if (!isRun) {
            respData.put("isRun", isRun);
            return;
        }
        String tomcatHome = Global.getSlaveStore().getSlaveApp(slaveAppId).getAppTomcatHome();
        CmdLineFactory.getInstance().shutdownTomcat(tomcatHome);
    }

    private static void reloadCache() {
        Global.getSlaveStore().reloadAll();
    }

    private static void threadDump(Map reqData, Map respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        String pid = CmdLineFactory.getInstance().getTomcatPid(port);
        byte[] dump = CmdLineFactory.getInstance().threadDump(pid);
        respData.put("threads", dump);
    }

}
