package com.whitedisk.white_disk.component;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiwenshare.ufop.factory.UFOPFactory;
import com.qiwenshare.ufop.operation.copy.domain.CopyFile;
import com.qiwenshare.ufop.util.UFOPUtils;
import com.whitedisk.white_disk.entity.FileEntity;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.FileMapper;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IFileTransferService;
import com.whitedisk.white_disk.service.api.IRecoveryFileService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateRevokedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author white
 */
@Slf4j
@Component
@Async("asyncTaskExecutor")
public class AsyncTaskComp {

    @Resource
    IFileTransferService fileTransferService;
    @Resource
    IRecoveryFileService recoveryFileService;
    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private FileDealComp fileDealComp;
    @Resource
    UFOPFactory ufopFactory;

    @Value("${ufop.storage-type}")
    private Integer storageType;

    public Long getFilePointCount(String fileId) {
        LambdaQueryWrapper<UserFileEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFileEntity::getFileId, fileId);
        return userFileMapper.selectCount(lambdaQueryWrapper);
    }

    public Future<String> deleteUserFile(String userFileId) {
        UserFileEntity userFile = userFileMapper.selectById(userFileId);
        if (userFile.getIsDir() == 1) {
            LambdaQueryWrapper<UserFileEntity> userFileLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userFileLambdaQueryWrapper.eq(UserFileEntity::getDeleteBatchNum, userFile.getDeleteBatchNum());
            List<UserFileEntity> userFileEntityList = userFileMapper.selectList(userFileLambdaQueryWrapper);
            for (UserFileEntity userFileEntity : userFileEntityList) {
                Long filePointCount = getFilePointCount(userFileEntity.getFileId());
                if (filePointCount != null && filePointCount == 0 && userFileEntity.getIsDir() == 0) {
                    FileEntity fileEntity = fileMapper.selectById(userFileEntity.getFileId());
                    try {
                        fileTransferService.deleteFile(fileEntity);
                        fileMapper.deleteById(fileEntity.getFileId());
                    } catch (Exception e) {
                        log.error("删除本地文件失败：" + JSON.toJSONString(fileEntity));
                    }
                }
            }
        } else {
            recoveryFileService.deleteUserFileByDeleteBatchNum(userFile.getDeleteBatchNum());
            Long filePointCount = getFilePointCount(userFile.getFileId());

            if (filePointCount != null && filePointCount == 0 && userFile.getIsDir() == 0) {
                FileEntity fileEntity = fileMapper.selectById(userFile.getFileId());
                try {
                    fileTransferService.deleteFile(fileEntity);
                    fileMapper.deleteById(fileEntity.getFileId());
                } catch(Exception e) {
                    log.error("删除本地文件失败：" + JSON.toJSONString(fileEntity));
                }
            }
        }
        return new AsyncResult<String>("deleteUserFile");
    }


    public Future<String> checkESUserFileId(String userFileId){
        UserFileEntity userFile=userFileMapper.selectById(userFileId);
        if (userFile == null) {
            fileDealComp.deleteESByUserFileId(userFileId);
        }
        return new AsyncResult<String>("checkUserFileId");
    }

    public Future<String> saveUnzipFile(UserFileEntity userFile, FileEntity fileBean, int unzipMode, String entryName, String filePath) {
        String unzipUrl = UFOPUtils.getTempFile(fileBean.getFileUrl()).getAbsolutePath().replace("." + userFile.getExtendName(), "");
        String totalFileUrl = unzipUrl + entryName;
        File currentFile = new File(totalFileUrl);

        String fileId = null;
        if (!currentFile.isDirectory()) {

            FileInputStream fis = null;
            String md5Str = UUID.randomUUID().toString();
            try {
                fis = new FileInputStream(currentFile);
                md5Str = DigestUtils.md5Hex(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(fis);
            }

            FileInputStream fileInputStream = null;
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("identifier", md5Str);
                List<FileEntity> list = fileMapper.selectByMap(param);

                if (list != null && !list.isEmpty()) { //文件已存在
                    fileId = list.get(0).getFileId();
                } else { //文件不存在
                    fileInputStream = new FileInputStream(currentFile);
                    CopyFile createFile = new CopyFile();
                    createFile.setExtendName(FilenameUtils.getExtension(totalFileUrl));
                    String saveFileUrl = ufopFactory.getCopier().copy(fileInputStream, createFile);

                    FileEntity tempFileBean = new FileEntity(saveFileUrl, currentFile.length(), storageType, md5Str, userFile.getUserId());
                    ;
                    fileMapper.insert(tempFileBean);
                    fileId = tempFileBean.getFileId();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(fileInputStream);
                System.gc();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentFile.delete();
            }


        }


        WhiteFile whiteFile = null;
        if (unzipMode == 0) {
            whiteFile = new WhiteFile(userFile.getFilePath(), entryName, currentFile.isDirectory());
        } else if (unzipMode == 1) {
            whiteFile = new WhiteFile(userFile.getFilePath() + "/" + userFile.getFileName(), entryName, currentFile.isDirectory());
        } else if (unzipMode == 2) {
            whiteFile = new WhiteFile(filePath, entryName, currentFile.isDirectory());
        }

        UserFileEntity saveUserFile = new UserFileEntity(whiteFile, userFile.getUserId(), fileId);
        String fileName = fileDealComp.getRepeatFileName(saveUserFile, saveUserFile.getFilePath());

        if (saveUserFile.getIsDir() == 1 && !fileName.equals(saveUserFile.getFileName())) {
            //如果是目录，而且重复，什么也不做
        } else {
            saveUserFile.setFileName(fileName);
            userFileMapper.insert(saveUserFile);
        }
        fileDealComp.restoreParentFilePath(whiteFile, userFile.getUserId());

        return new AsyncResult<String>("saveUnzipFile");
    }
}
