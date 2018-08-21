package com.bsoft.deploy;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * desc
 * Created on 2018/8/19.
 *
 * @author yangl
 */
public class CommandLineTest {

    public static void main(String[] args) throws Exception {

        netStat();
        //executor.setStreamHandler(streamHandler);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                tomcatStart();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //tomcatStop();
            }
        }).start();*/



    }

    private static void tomcatStop() {
        try {
            Thread.sleep(10000);
            CommandLine cmd = CommandLine.parse("cmd /c shutdown.bat");
            DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(new File("D:/develop/apache-tomcat7_8889/bin"));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
            ExecuteWatchdog watchdog = new ExecuteWatchdog(50000);
            // executor.setExitValue(1);
            executor.setWatchdog(watchdog);
            // executor.setStreamHandler(streamHandler);
            int exitValue = executor.execute(cmd);
            System.out.println(exitValue);
            System.out.println(outputStream.toString("gbk"));
            System.out.println(errorStream.toString("gbk"));
        } catch (Exception e) {
            e.printStackTrace();
        };
    }

    private static CommandLine javaVersion() {
        CommandLine cmd = new CommandLine("java");
        cmd.addArgument("-version");
        return cmd;
    }

    private static CommandLine netStat()  throws  Exception{
        CommandLine cmd = CommandLine.parse("cmd /c (netstat -nao | findstr 8889)");
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        executor.setStreamHandler(streamHandler);
        executor.setExitValue(0);
        int exitValue = executor.execute(cmd);
        System.out.println(exitValue);
        System.out.println(outputStream.toString());
        System.out.println(errorStream.toString());
        return cmd;
    }

    private static void tomcatStart() {
        try {
            CommandLine cmd = CommandLine.parse("cmd /c startup.bat");
            DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(new File("D:/develop/apache-tomcat7_8889/bin"));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);

            int exitValue = executor.execute(cmd);
            System.out.println(exitValue);
            System.out.println(outputStream.toString("gbk"));
            System.out.println(errorStream.toString("gbk"));
        } catch (IOException e) {
            e.printStackTrace();
        };
    }
}
