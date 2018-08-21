package com.bsoft.deploy.command;

import com.bsoft.deploy.command.impl.LinuxCmdLineImpl;
import com.bsoft.deploy.command.impl.WinCmdLineImpl;

/**
 * desc
 * Created on 2018/8/18.
 *
 * @author yangl
 */
public class CmdLineFactory {
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static LinuxCmdLineImpl linuxCmdLine = null;
    private static WinCmdLineImpl winCmdLine = null;

    public static CmdLine getInstance() {
        if (isLinux()) {
            if (linuxCmdLine == null) {
                linuxCmdLine = new LinuxCmdLineImpl();
            }
            return linuxCmdLine;
        }
        if (isWindows()) {
            if (winCmdLine == null) {
                winCmdLine = new WinCmdLineImpl();
            }
            return winCmdLine;
        }
        return new WinCmdLineImpl();
    }

    public static boolean isLinux() {
        return OS.contains("linux");
    }

    public static boolean isWindows() {
        return OS.contains("windows");
    }

}
