package com.bsoft.deploy.command.impl;

import com.bsoft.deploy.command.CmdLine;
import com.bsoft.deploy.exception.CommandExecuteException;
import com.bsoft.deploy.exception.FileOperationException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

/**
 * window命令行实现
 * Created on 2018/8/18.
 *
 * @author yangl
 */
public class WinCmdLineImpl implements CmdLine {
    private static final Logger logger = LoggerFactory.getLogger(WinCmdLineImpl.class);

    @Override
    public boolean startupTomcat(final String tomcat_home) {
        File bat = new File(tomcat_home + "bin/startup.bat");
        if (!bat.exists()) {
            String message = MessageFormatter
                    .format("指定的tomcat_home[{}]中没有找到有效的启动文件!", tomcat_home)
                    .getMessage();
            throw new FileOperationException(message);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String batDir = tomcat_home + "bin";
                CommandLine cmd = CommandLine.parse("cmd /c startup.bat");
                DefaultExecutor executor = new DefaultExecutor();
                executor.setWorkingDirectory(new File(batDir));
                try {
                    executor.execute(cmd);
                } catch (IOException e) {
                    logger.error("启动tomcat失败!", e);
                }
            }
        }).start();
        return true;
    }

    @Override
    public boolean shutdownTomcat(String tomcat_home) {
        String batDir = tomcat_home + "bin";
        CommandLine cmd = CommandLine.parse("cmd /c shutdown.bat");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(batDir));
        try {
            executor.execute(cmd);
        } catch (IOException e) {
            logger.error("关闭tomcat失败!", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean isTomcatRunning(String port) throws CommandExecuteException {
        try {
            String cmd = "cmd /c netstat -nao | findstr " + port;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(cmd);
            int exit = process.waitFor();
            if (exit == 0) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = br.readLine()) != null) {
                    // 判断 line 是否有效
                    if (line.contains("LISTENING")) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        } catch (IOException | InterruptedException e) {
            throw new CommandExecuteException("获取tomcat端口号执行失败!", e);
        }
    }

    @Override
    public String getTomcatPid(String port) throws CommandExecuteException {
        String pid = null;
        try {
            String netStat = "cmd /c netstat -nao | findstr " + port;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(netStat);
            int exit = process.waitFor();
            if (exit == 0) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = br.readLine()) != null) {
                    // 判断 line 是否有效
                    if (line.contains("LISTENING")) {
                        String[] info = line.split(" ");
                        pid = info[info.length - 1];
                        break;
                    }
                }
            }
            String taskList = "cmd /c tasklist /NH /FI \"pid eq " + pid + "\"";
            process = runtime.exec(taskList);
            exit = process.waitFor();
            if (exit == 0) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = br.readLine()) != null) {
                    // 确认是java进程
                    if (line.startsWith("java.exe")) {
                        return pid;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new CommandExecuteException("获取tomcat的pid命令执行失败!", e);
        }
        return null;
    }

    @Override
    public byte[] threadDump(String pid) throws CommandExecuteException {
        try {
            String cmd = "jstack " + pid + "";
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(cmd);
            InputStream inputStream = process.getInputStream();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024]; //buff用于存放循环读取的临时数据
            int rc;
            while ((rc = inputStream.read(buff, 0, 1024)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            byte[] thread = swapStream.toByteArray();
            int exit = process.exitValue();
            if (exit == 0) {
                return thread;
            }
        } catch (IOException e) {
            throw new CommandExecuteException("获取tomcat端口号执行失败!", e);
        }
        return null;
    }

    @Override
    public boolean backupApp(String srcPath, String backupPath) throws CommandExecuteException {
        StringBuilder cmd = new StringBuilder();
        try {
            if (srcPath.endsWith("/") || srcPath.endsWith("\\")) {
                srcPath = srcPath.substring(0, srcPath.length() - 1);
            }
            cmd.append("cmd /c xcopy /e /i /q /y ");
            ClassPathResource exclude_file = new ClassPathResource("exclude.txt");
            if (exclude_file.exists()) {
                cmd.append("/exclude:");
                cmd.append(exclude_file.getFile().getAbsolutePath());
            }
            // 文件路径加双引号
            cmd.append(" \"");
            cmd.append(srcPath);
            cmd.append("\" \"");
            cmd.append(backupPath);
            cmd.append("\"");
            return executeCommand(cmd.toString());
        } catch (Exception e) {
            logger.error("命令[{}]执行失败!", cmd.toString());
            throw new CommandExecuteException("应用备份失败!原因:" + e.getMessage());
        }
    }

    @Override
    public boolean removeApp(String path) throws CommandExecuteException {
        try {
            return executeCommand("cmd /c rmdir /s /q \"" + path + "\"");
        } catch (IOException | InterruptedException e) {
            throw new CommandExecuteException("应用删除失败!", e);
        }
    }


    /**
     * 采用先copy,后delete方式
     * ps:move 不支持跨分区,如c盘移动到d盘,且不能过滤
     *
     * @param srcPath
     * @param backupPath
     * @return
     * @throws CommandExecuteException
     */
    @Override
    public boolean backupAndRemoveApp(String srcPath, String backupPath) throws CommandExecuteException {
        return backupApp(srcPath, backupPath) && removeApp(srcPath);
    }

    private boolean executeCommand(String cmd) throws IOException, InterruptedException, CommandExecuteException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(cmd);
        int exit = process.waitFor();
        if (exit == 0) {
            return true;
        } else {
            StringBuilder error = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
            while ((line = br.readLine()) != null) {
                error.append(line);
            }
            logger.error("命令[{}]执行失败!", cmd);
            throw new CommandExecuteException(error.toString());
        }
    }

}
