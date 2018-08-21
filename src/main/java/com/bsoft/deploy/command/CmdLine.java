package com.bsoft.deploy.command;

import com.bsoft.deploy.exception.CommandExecuteException;

/**
 * windows bat命令或者 linux shell命令调用
 * Created on 2018/8/18.
 *
 * @author yangl
 */
public interface CmdLine {

    /**
     * 运行tomcat
     *
     * @return
     * @Param tomcat home
     */
    boolean startupTomcat(String tomcat_home) throws CommandExecuteException;

    /**
     * 关闭tomcat
     *
     * @param tomcat_home
     * @return
     */
    boolean shutdownTomcat(String tomcat_home) throws CommandExecuteException;

    /**
     * 判断指定端口的tomcat是否运行中
     *
     * @param port
     * @return
     * @throws CommandExecuteException
     */
    boolean isTomcatRunning(String port) throws CommandExecuteException;

    /**
     * 获取指定port的tomcat的pid
     * @param port
     * @return
     * @throws CommandExecuteException
     */
    String getTomcatPid(String port) throws CommandExecuteException;

    byte[] threadDump(String pid) throws CommandExecuteException;
}
