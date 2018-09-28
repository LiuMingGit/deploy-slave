package com.bsoft.deploy.file;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.AppFile;
import com.bsoft.deploy.dao.entity.FileLog;
import com.bsoft.deploy.dao.entity.SlaveApp;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.exception.FileOperationException;
import com.bsoft.deploy.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 接收master传输的文件
 * Created on 2018/8/10.
 *
 * @author yangl
 */
public class FileWorker {
    private final static Logger logger = LoggerFactory.getLogger(FileWorker.class);


    // private String appPath = "D:/workspace_ideal/deploy/master_target/";
    private String backup_dir = "D:/workspace_ideal/deploy/master_backup/";

    public static boolean receive(AppFile file) throws Exception {
        try {
            SlaveApp slaveApp = Global.getSlaveStore().getSlaveApp(file.getSlaveAppId());
            String appPath = slaveApp.getAppTargetPath();
            File baseDir = new File(appPath);
            if (!baseDir.exists()) {
                if (!baseDir.mkdirs()) {
                    throw new FileOperationException("文件操作失败:应用基础路径不存在且创建失败!");
                }
            }

            File f = new File(appPath + file.getRelative());
            String backup_path = slaveApp.getAppBackupPath();
            String appBackupName = "bak_" + slaveApp.getId() + "_" + slaveApp.getPkgId();
            String backup_file_path = backup_path + appBackupName;
            File backupDir = new File(backup_file_path);
            boolean isUpdateIgnore = Global.getAppStore().getAppFiles(slaveApp.getAppId()).contains(file.getRelative());
            // 文件存在先备份
            if (f.exists()) {
                // 更新忽略
                if (isUpdateIgnore) {
                    updateSlaveLog(1, "", file.getLogId());
                    return true;
                }

                if (!backupDir.exists()) {
                    if (!backupDir.mkdirs()) {
                        return true;
                    }
                }
                // 保存备份文件 重命名备份文件
                // int hashcode = (file.getName() + file.getMark()).hashCode();
                File backup_file = new File(backup_file_path + File.separator + file.getRelative());
                String fileDir = FileUtils.getFilePath(backup_file.getAbsolutePath());
                File dir = new File(fileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileUtils.copyFile(f, backup_file);

                // 文件是否允许操作
                if (!f.canWrite()) {
                    throw new FileOperationException("文件操作失败:文件[" + file.getName() + "]不允许写入!");
                }
            } else {
                String dir = FileUtils.getFilePath(f.getAbsolutePath());
                new File(dir).mkdirs();
                // 全量更新需要重新覆盖更新忽略的文件
                if (isUpdateIgnore && backupDir.exists()) {
                    logger.debug("文件[{}]覆盖完成!", file.getName());
                    File backupFile = new File(backupDir + File.separator + file.getRelative());
                    if(backupFile.exists()) {
                        FileUtils.copyFile(backupFile, f);
                        updateSlaveLog(1, "", file.getLogId());
                        return true;
                    }
                }
            }
            logger.debug("文件[{}]备份完成!", file.getName());
            // 更新文件
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                fos.write(file.getContent());
                fos.flush();
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
            // 同步成功
            // 回写同步成功标志
            updateSlaveLog(1, "", file.getLogId());
            return true;
        } catch (Exception e) {
            // 同步失败
            updateSlaveLog(-1, e.getMessage(), file.getLogId());
            logger.error("文件[{}]同步失败!", file.getName(), e);
            return false;
        }
    }

    private static void updateSlaveLog(int status, String message, int logId) {
        FileLog fileLog = new FileLog();
        fileLog.setStatus(status);
        fileLog.setMessage(message);
        fileLog.setId(logId);
        Global.getAppContext().getBean(AppFileMapper.class).updateFileTransferLog(fileLog);
    }
}
