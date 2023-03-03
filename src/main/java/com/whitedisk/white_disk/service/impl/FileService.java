package com.whitedisk.white_disk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlighterEncoder;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.j2objc.annotations.ReflectionSupport;
import com.qiwenshare.common.exception.QiwenException;
import com.qiwenshare.common.operation.FileOperation;
import com.qiwenshare.common.util.DateUtil;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.qiwenshare.ufop.factory.UFOPFactory;
import com.qiwenshare.ufop.operation.copy.Copier;
import com.qiwenshare.ufop.operation.copy.domain.CopyFile;
import com.qiwenshare.ufop.operation.download.Downloader;
import com.qiwenshare.ufop.operation.download.domain.DownloadFile;
import com.qiwenshare.ufop.util.UFOPUtils;
import com.whitedisk.white_disk.component.AsyncTaskComp;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.config.es.FileSearch;
import com.whitedisk.white_disk.dto.file.*;
import com.whitedisk.white_disk.entity.FileEntity;
import com.whitedisk.white_disk.entity.Image;
import com.whitedisk.white_disk.entity.Music;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.FileMapper;
import com.whitedisk.white_disk.mapper.ImageMapper;
import com.whitedisk.white_disk.mapper.MusicMapper;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.utils.WhiteFileUtil;
import com.whitedisk.white_disk.vo.file.FileDetailVO;
import com.whitedisk.white_disk.vo.file.SearchFileVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.UTF8;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author white
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class FileService extends ServiceImpl<FileMapper, FileEntity> implements IFileService {

    @Resource
    private FileDealComp fileDealComp;
    @Resource
    private UserFileMapper userFileMapper;
    @Autowired
    private UserFileService userFileService;
    @Resource
    private FileMapper fileMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private AsyncTaskComp asyncTaskComp;
    @Resource
    UFOPFactory ufopFactory;
    @Resource
    MusicMapper musicMapper;
    @Resource
    ImageMapper imageMapper;
    @Value("${ufop.storage-type}")
    private Integer storageType;

    @Override
    public String createFile(CreateFileDTO createFileDTO, JwtUser user) {
        try{
            String fileName = createFileDTO.getFileName();
            String filePath = createFileDTO.getFilePath();
            String extendName = createFileDTO.getExtendName();
            String userId = user.getUserId();

            List<UserFileEntity> userFileEntityList=userFileService.selectSameUserFile(fileName,filePath,extendName,userId);
            if (userFileEntityList != null && !userFileEntityList.isEmpty()) {
                return new String("同名文件已存在");
            }

            String uuid= UUID.randomUUID().toString().replaceAll("-", "");

            String templateFilePath = "";
            if ("docx".equals(extendName)) {
                templateFilePath = "template/Word.docx";
            } else if ("xlsx".equals(extendName)) {
                templateFilePath = "template/Excel.xlsx";
            } else if ("pptx".equals(extendName)) {
                templateFilePath = "template/PowerPoint.pptx";
            } else if ("txt".equals(extendName)) {
                templateFilePath = "template/Text.txt";
            } else if ("drawio".equals(extendName)) {
                templateFilePath = "template/Drawio.drawio";
            }

            String url2= ClassUtils.getDefaultClassLoader().getResource("static/" + templateFilePath).getPath();
            url2 = URLDecoder.decode(url2, "UTF-8");
            FileInputStream inputStream = new FileInputStream(url2);
            Copier copier = ufopFactory.getCopier();
            CopyFile copyFile = new CopyFile();
            copyFile.setExtendName(extendName);
            String fileUrl = copier.copy(inputStream, copyFile);

            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileId(IdUtil.getSnowflakeNextIdStr());
            fileEntity.setFileSize(0L);
            fileEntity.setFileUrl(fileUrl);
            fileEntity.setStorageType(storageType);
            fileEntity.setIdentifier(uuid);
            fileEntity.setCreateTime(DateUtil.getCurrentTime());
            fileEntity.setCreateUserId(SessionUtil.getSession().getUserId());
            fileEntity.setFileStatus(1);

            boolean saveFlag = this.save(fileEntity);

            UserFileEntity userFile = new UserFileEntity();
            if (saveFlag){
                userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
                userFile.setUserId(userId);
                userFile.setFileName(fileName);
                userFile.setFilePath(filePath);
                userFile.setDeleteFlag(0);
                userFile.setIsDir(0);
                userFile.setExtendName(extendName);
                userFile.setUploadTime(DateUtil.getCurrentTime());
                userFile.setFileId(fileEntity.getFileId());

                userFileService.save(userFile);
            }
            return new String("文件创建成功");
        }catch (Exception e){
            log.debug(e.getMessage());
            return new String(e.getMessage());
        }
    }

    @Override
    public Boolean createFold(CreateFoldDTO createFileDTO, JwtUser user) {
        boolean dirExist = fileDealComp.isDirExist(createFileDTO.getFileName(), createFileDTO.getFilePath(), user.getUserId());
        if(dirExist){
            return false;
        }
        UserFileEntity userFile = WhiteFileUtil.getWhiteDir(user.getUserId(), createFileDTO.getFilePath(), createFileDTO.getFileName());
        userFileService.save(userFile);
        fileDealComp.uploadESByUserFileId(userFile.getUserFileId());
        return true;
    }

    @Override
    public List<SearchFileVO> searchFile(SearchFileDTO searchFileDTO, JwtUser user) {
        int currentPage = (int) searchFileDTO.getCurrentPage()-1;
        int pageCount = (int) (searchFileDTO.getPageCount() == 0 ? 10 : searchFileDTO.getPageCount());
        SearchResponse<FileSearch> searchResponse =null;
        try{
            searchResponse = elasticsearchClient.search(s -> s
                            .index("filesearch")
                            .query(_1 -> _1
                                    .bool(_2 -> _2
                                            .must(_3 -> _3
                                                    .bool(_4 -> _4
                                                            .should(_5 -> _5
                                                                    .match(_6 -> _6
                                                                            .field("fileName")
                                                                            .query(searchFileDTO.getFileName())))
                                                            .should(_5 -> _5
                                                                    .wildcard(_6 -> _6
                                                                            .field("fileName")
                                                                            .wildcard("*" + searchFileDTO.getFileName() + "*")))
                                                            .should(_5 -> _5
                                                                    .match(_6 -> _6
                                                                            .field("content")
                                                                            .query(searchFileDTO.getFileName())))
                                                            .should(_5 -> _5
                                                                    .wildcard(_6 -> _6
                                                                            .field("content")
                                                                            .wildcard("*" + searchFileDTO.getFileName() + "*")))
                                                    ))
                                            .must(_3 -> _3
                                                    .term(_4 -> _4
                                                            .field("userId")
                                                            .value(user.getUserId())))
                                    ))
                            .from(currentPage)
                            .size(pageCount)
                            .highlight(h -> h
                                    .fields("fileName", f -> f.type("plain")
                                            .preTags("<span class='keyword'>").postTags("</span>"))
                                    .encoder(HighlighterEncoder.Html))
                    ,
                    FileSearch.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        List<SearchFileVO> list= new ArrayList<>();
        for (Hit<FileSearch> hit : searchResponse.hits().hits()) {
            SearchFileVO searchFileVO=new SearchFileVO();
            BeanUtil.copyProperties(hit,searchFileVO);
            searchFileVO.setHighLight(hit.highlight());
            list.add(searchFileVO);
            asyncTaskComp.checkESUserFileId(searchFileVO.getUserFileId());
        }
        return list;
    }

    @Override
    public Boolean renameFile(RenameFileDTO renameFileDto, JwtUser user) {
        UserFileEntity userFile=userFileService.getById(renameFileDto.getUserFileId());
        List<UserFileEntity> userFileEntities=userFileService.selectUserFileByNameAndPath(renameFileDto.getFileName(), userFile.getFilePath(),user.getUserId());
        if (userFileEntities != null && !userFileEntities.isEmpty()) {
            return false;
        }
        LambdaUpdateWrapper<UserFileEntity> wrapper=new LambdaUpdateWrapper<>();
        wrapper.set(UserFileEntity::getFileName,renameFileDto.getFileName())
                .set(UserFileEntity::getUploadTime, DateUtil.getCurrentTime())
                .eq(UserFileEntity::getUserFileId,renameFileDto.getUserFileId());
        userFileService.update(wrapper);

        if(1 == userFile.getIsDir()){
            List<UserFileEntity> list=userFileService.selectUserFileByLikeRightFilePath(new WhiteFile(userFile.getFilePath(),userFile.getFileName(),true).getPath(),user.getUserId());
            for (UserFileEntity newUserFileEntity : list) {
                newUserFileEntity.setFilePath(newUserFileEntity.getFilePath()
                        .replaceFirst(new WhiteFile(userFile.getFilePath(), userFile.getFileName(), userFile.getIsDir() == 1).getParent()
                                     ,new WhiteFile(userFile.getFilePath(), renameFileDto.getFileName(), userFile.getIsDir() == 1).getParent()));
                userFileService.updateById(newUserFileEntity);
            }
        }
        fileDealComp.uploadESByUserFileId(renameFileDto.getUserFileId());
        return true;
    }

    @Override
    public void unzipFile(String userFileId, int unzipMode, String filePath) {
        UserFileEntity userFile = userFileMapper.selectById(userFileId);
        FileEntity fileEntity = fileMapper.selectById(userFile.getFileId());
        File destFile = new File(UFOPUtils.getStaticPath() + "temp" + File.separator + fileEntity.getFileUrl());

        Downloader downloader = ufopFactory.getDownloader(fileEntity.getStorageType());
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setFileUrl(fileEntity.getFileUrl());
        InputStream inputStream = downloader.getInputStream(downloadFile);

        try {
            FileUtils.copyInputStreamToFile(inputStream,destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String extendName = userFile.getExtendName();
        String unzipUrl = UFOPUtils.getTempFile(fileEntity.getFileUrl()).getAbsolutePath().replaceAll("." + extendName, "");

        List<String> fileEntryNameList=new ArrayList<>();

        try {
            fileEntryNameList = FileOperation.unzip(destFile, unzipUrl);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("解压失败" + e);
            throw new QiwenException(500001, "解压异常：" + e.getMessage());
        }

        if(destFile.exists()){
            destFile.delete();
        }

        if(!fileEntryNameList.isEmpty() && unzipMode==1){
            UserFileEntity whiteDir = WhiteFileUtil.getWhiteDir(userFile.getUserId(), userFile.getFilePath(), userFile.getFileName());
            userFileMapper.insert(whiteDir);
        }

        for (int i = 0; i < fileEntryNameList.size(); i++) {
            String entryName = fileEntryNameList.get(i);
            asyncTaskComp.saveUnzipFile(userFile,fileEntity,unzipMode,entryName,filePath);
        }

    }

    @Override
    public Boolean copyFile(CopyFileDTO copyFileDTO, JwtUser user) {
        String userId = user.getUserId();
        String userFileIds = copyFileDTO.getUserFileIds();
        String filePath = copyFileDTO.getFilePath();

        String[] userFileIdList = userFileIds.split(",");

        for (String userFileId : userFileIdList) {
            UserFileEntity userFile = userFileMapper.selectById(userFileId);
            String oldFilePath = userFile.getFilePath();
            String fileName = userFile.getFileName();
            if(userFile.isDirectory()){
                WhiteFile whiteFile = new WhiteFile(oldFilePath, fileName, true);
                if(filePath.startsWith(whiteFile.getPath() + WhiteFile.separator) || filePath.equals(whiteFile.getPath())){
                    return false;
                }
            }
            userFileService.userFileCopy(userFileId,filePath,userId);
            fileDealComp.deleteRepeatSubDirFile(filePath, userId);
        }
        return true;
    }

    @Override
    public FileDetailVO getFileDetail(String userFileId) {
        UserFileEntity userFile = userFileMapper.selectById(userFileId);
        FileEntity fileEntity = fileMapper.selectById(userFileId);
        Music music = musicMapper.selectOne(new QueryWrapper<Music>().eq("fileId", userFile.getFileId()));
        Image image = imageMapper.selectOne(new QueryWrapper<Image>().eq("fileId", userFile.getFileId()));

        if ("mp3".equalsIgnoreCase(userFile.getExtendName()) || "flac".equalsIgnoreCase(userFile.getExtendName())) {
            if (music == null) {
                fileDealComp.parseMusicFile(userFile.getExtendName(), fileEntity.getStorageType(), fileEntity.getFileUrl(), fileEntity.getFileId());
                music = musicMapper.selectOne(new QueryWrapper<Music>().eq("fileId", userFile.getFileId()));
            }
        }
        FileDetailVO fileDetailVO = new FileDetailVO();
        BeanUtil.copyProperties(userFile, fileDetailVO);
        BeanUtil.copyProperties(fileEntity, fileDetailVO);
        fileDetailVO.setMusic(music);
        fileDetailVO.setImage(image);
        return fileDetailVO;
    }
}
