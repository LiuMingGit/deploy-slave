package com.bsoft.deploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc
 * Created on 2018/8/19.
 *
 * @author yangl
 */
public class Test {
    public static void main(String[] args) throws Exception {

        ConcurrentHashMap<Integer,String> map = new ConcurrentHashMap<>();
        Integer key = new Integer(1001);
        Integer key2 = new Integer(1001);
        map.put(1001,"101");
        System.out.println(map.get(key2));
        System.out.println(map.containsKey(1001));
        System.out.println(map.containsKey(key2));

        /*String serverXml = "D:\\develop\\apache-tomcat7_8889\\conf\\server.xml";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(serverXml));
        Element root = document.getRootElement();
        String port = root.element("Service").element("Connector").attributeValue("port");
        System.out.println(port);*/
        //String send = "E:\\apache-tomcat-7.0.76\\bin\\startup.bat";//启动tomcat命令
        //String send = "E:\\apache-tomcat-7.0.76\\bin\\tomcat7w.exe";//启动Tomcat命令，仅限windows版本，无弹框
        String send = "cmd /c tasklist /NH /FI \"pid eq 20152\"";//关闭tomcat命令
        Test callTomcat = new Test();
        try {
            callTomcat.callCommand(send);
        } catch (Exception e) {
            System.out.println("执行命令时出错：" + e.getMessage());
        }

    }

    /**
     * 执行命令
     *
     * @throws IOException
     */
    private void callCommand(String command) throws Exception {

        Runtime runtime = Runtime.getRuntime();//返回与当前的Java应用相关的运行时对象
        //指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例
        Process process = runtime.exec(command);
        process.waitFor();
        System.out.println(process.exitValue());
        String line = null;
        String content = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while((line = br.readLine()) != null) {
            content += line + "\r\n";
        }
        System.out.println(content);

    }
}
