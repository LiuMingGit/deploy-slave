package com.bsoft.deploy.netty.client;

import com.bsoft.deploy.command.CmdLine;
import com.bsoft.deploy.command.CmdLineFactory;
import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Order;
import com.bsoft.deploy.dao.entity.SlaveApp;
import com.bsoft.deploy.exception.CommandExecuteException;
import com.bsoft.deploy.utils.FileUtils;
import com.bsoft.deploy.utils.StringUtils;

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
                reloadCache(order.getReqData());
                break;
            case Constant.CMD_THREAD_DUMP:
                threadDump(order.getReqData(), respData);
                break;
            case Constant.CMD_APP_BACKUP:
                appBackup(order.getReqData(), respData);
                break;
        }
        // 去除请求数据,减少传输量
        order.setReqData(null);
        return respData;
    }

    /**
     * 全量备份
     *
     * @param reqData
     * @param respData
     * @throws CommandExecuteException
     */
    private static void appBackup(Map<String, Object> reqData, Map<String, Object> respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        SlaveApp slaveApp = Global.getSlaveStore().getSlaveApp(slaveAppId);
        String backup_path = slaveApp.getAppBackupPath();
        String tomcat_home = slaveApp.getAppTomcatHome();
        String target_path = slaveApp.getAppTargetPath();
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        CmdLine cmdLine = CmdLineFactory.getInstance();
        boolean isRun = cmdLine.isTomcatRunning(port);
        if (isRun) {
            cmdLine.shutdownTomcat(tomcat_home);
        }
        int reTry = 0;
        while (isRun) {
            if (reTry > 20) {
                throw new CommandExecuteException("tomcat无法正常关闭!");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // slient
            }
            reTry++;
            isRun = CmdLineFactory.getInstance().isTomcatRunning(port);
        }
        // 判断是否存在有效的应用
        if (FileUtils.exists(target_path)) {
            String appBackupName = "bak_" + slaveApp.getId() + "_" + slaveApp.getPkgId();
            cmdLine.backupAndRemoveApp(target_path, backup_path + appBackupName);
        }
    }

    private static void isTomcatRun(Map<String, Object> reqData, Map<String, Object> respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        boolean isRun = CmdLineFactory.getInstance().isTomcatRunning(port);
        respData.put("isRun", isRun);
    }

    private static void startTomcat(Map reqData, Map<String, Object> respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        boolean isRun = CmdLineFactory.getInstance().isTomcatRunning(port);
        if (isRun) {
            respData.put("isRun", true);
            return;
        }
        String tomcatHome = Global.getSlaveStore().getSlaveApp(slaveAppId).getAppTomcatHome();
        CmdLineFactory.getInstance().startupTomcat(tomcatHome);
    }

    private static void stopTomcat(Map reqData, Map<String, Object> respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        boolean isRun = CmdLineFactory.getInstance().isTomcatRunning(port);
        if (!isRun) {
            respData.put("isRun", false);
            return;
        }
        String tomcatHome = Global.getSlaveStore().getSlaveApp(slaveAppId).getAppTomcatHome();
        CmdLineFactory.getInstance().shutdownTomcat(tomcatHome);
        respData.put("isRun", false);
    }

    private static void reloadCache(Map reqData) {
        if (reqData != null) {
            String target = (String) reqData.get("target");
            int id = (int) reqData.get("id");
            if (StringUtils.isEq("ALL", target)) {
                Global.getAppStore().reloadAll();
                Global.getSlaveStore().reloadAll();
            } else if (StringUtils.isEq("app", target)) {
                Global.getAppStore().reload(id);
            } else if (StringUtils.isEq("appFile", target)) {
                Global.getAppStore().reloadFiles(id);
            } else if (StringUtils.isEq("slave", target)) {
                Global.getSlaveStore().reloadSlave(id);
            } else if (StringUtils.isEq("slaveApp", target)) {
                Global.getSlaveStore().reloadSlaveApp(id);
            }
        }

    }

    private static void threadDump(Map reqData, Map<String, Object> respData) throws CommandExecuteException {
        int slaveAppId = (int) reqData.get("slaveAppId");
        String port = Global.getSlaveStore().getSlaveAppPort(slaveAppId);
        String pid = CmdLineFactory.getInstance().getTomcatPid(port);
        byte[] dump = CmdLineFactory.getInstance().threadDump(pid);
        respData.put("threads", dump);
    }

}
