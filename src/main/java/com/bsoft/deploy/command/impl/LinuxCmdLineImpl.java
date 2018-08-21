package com.bsoft.deploy.command.impl;

import com.bsoft.deploy.command.CmdLine;
import com.bsoft.deploy.exception.CommandExecuteException;

/**
 * linux实现
 * Created on 2018/8/18.
 *
 * @author yangl
 */
public class LinuxCmdLineImpl implements CmdLine {
    @Override
    public boolean startupTomcat(String tomcat_home) {
        return false;
    }

    @Override
    public boolean shutdownTomcat(String tomcat_home) {
        return false;
    }

    @Override
    public boolean isTomcatRunning(String port) {
        return false;
    }

    @Override
    public String getTomcatPid(String port) throws CommandExecuteException {
        return null;
    }

    @Override
    public byte[] threadDump(String pid) throws CommandExecuteException {
        return null;
    }


}
