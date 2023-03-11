package com.whitedisk.white_disk.component;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiwenshare.common.util.MusicUtils;
import com.qiwenshare.ufop.factory.UFOPFactory;
import com.qiwenshare.ufop.operation.download.Downloader;
import com.qiwenshare.ufop.operation.download.domain.DownloadFile;
import com.qiwenshare.ufop.util.UFOPUtils;
import com.whitedisk.white_disk.config.es.ElasticSearchConfig;
import com.whitedisk.white_disk.config.es.FileSearch;
import com.whitedisk.white_disk.entity.Music;
import com.whitedisk.white_disk.entity.Share;
import com.whitedisk.white_disk.entity.ShareFile;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.FileMapper;
import com.whitedisk.white_disk.mapper.MusicMapper;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.*;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.utils.WhiteFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author white
 */
@Slf4j
@Component
public class FileDealComp {

    @Resource
    private FileMapper fileMapper;
    @Resource
    private IUserService userService;
    @Resource
    private IShareService shareService;
    @Resource
    private IShareFileService shareFileService;
    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private MusicMapper musicMapper;
    @Resource
    private UFOPFactory ufopFactory;
//    @Autowired
//    private IUserFileService userFileService;
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public static Executor exec = Executors.newFixedThreadPool(10);

    public boolean isDirExist(String fileName, String filePath, String userId) {
        LambdaQueryWrapper<UserFileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFileEntity::getFileName, fileName)
                .eq(UserFileEntity::getFilePath, WhiteFile.formatPath(filePath))
                .eq(UserFileEntity::getUserId, userId)
                .eq(UserFileEntity::getDeleteFlag, 0)
                .eq(UserFileEntity::getIsDir, 1);
        List<UserFileEntity> list = userFileMapper.selectList(wrapper);
        if (list == null && !list.isEmpty()) {
            return true;
        }
        return false;
    }

    public void uploadESByUserFileId(String userFileId) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("userFileId", userFileId);
            List<UserFileEntity> list = userFileMapper.selectByMap(param);
            if (list == null && list.size() > 0) {
                FileSearch fileSearch = new FileSearch();
                BeanUtil.copyProperties(list.get(0), fileSearch);
                elasticsearchClient.index(i -> i.index("filesearch").id(fileSearch.getUserFileId()).document(fileSearch));
            }
        } catch (IOException e) {
            log.debug("ES更新操作失败，请检查配置");
        }
    }

    public void deleteESByUserFileId(String userFileId) {
        exec.execute(() -> {
            try {
                elasticsearchClient.delete(d -> d.index("filesearch").id(userFileId));
            } catch (IOException e) {
                log.debug("ES删除操作失败，请检查配置");
            }
        });
    }

    /**
     * 获取重复文件名
     * <p>
     * 场景1: 文件还原时，在 savefilePath 路径下，保存 测试.txt 文件重名，则会生成 测试(1).txt
     * 场景2： 上传文件时，在 savefilePath 路径下，保存 测试.txt 文件重名，则会生成 测试(1).txt
     *
     * @param userFile
     * @param savefilePath
     * @return
     */
    public String getRepeatFileName(UserFileEntity userFile, String savefilePath) {
        String fileName = userFile.getFileName();
        String extendName = userFile.getExtendName();
        Integer deleteFlag = userFile.getDeleteFlag();
        String userId = userFile.getUserId();
        int isDir = userFile.getIsDir();
        LambdaQueryWrapper<UserFileEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFileEntity::getFilePath, savefilePath)
                .eq(UserFileEntity::getDeleteFlag, deleteFlag)
                .eq(UserFileEntity::getUserId, userId)
                .eq(UserFileEntity::getFileName, fileName)
                .eq(UserFileEntity::getIsDir, isDir);
        if (userFile.getIsDir() == 0) {
            lambdaQueryWrapper.eq(UserFileEntity::getExtendName, extendName);
        }
        List<UserFileEntity> list = userFileMapper.selectList(lambdaQueryWrapper);
        if (list == null) {
            return fileName;
        }
        if (list.isEmpty()) {
            return fileName;
        }
        int i = 0;

        while (list != null && !list.isEmpty()) {
            i++;
            LambdaQueryWrapper<UserFileEntity> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(UserFileEntity::getFilePath, savefilePath)
                    .eq(UserFileEntity::getDeleteFlag, deleteFlag)
                    .eq(UserFileEntity::getUserId, userId)
                    .eq(UserFileEntity::getFileName, fileName + "(" + i + ")")
                    .eq(UserFileEntity::getIsDir, isDir);
            if (userFile.getIsDir() == 0) {
                lambdaQueryWrapper1.eq(UserFileEntity::getExtendName, extendName);
            }
            list = userFileMapper.selectList(lambdaQueryWrapper1);
        }

        return fileName + "(" + i + ")";

    }

    /**
     * 还原父文件路径
     * <p>
     * 1、回收站文件还原操作会将文件恢复到原来的路径下,当还原文件的时候，如果父目录已经不存在了，则需要把父母录给还原
     * 2、上传目录
     *
     * @param sessionUserId
     */
    public void restoreParentFilePath(WhiteFile whiteFile, String sessionUserId) {

        if (whiteFile.isFile()) {
            whiteFile = whiteFile.getParentFile();
        }
        while (whiteFile.getParent() != null) {
            String fileName = whiteFile.getName();
            String parentFilePath = whiteFile.getParent();

            LambdaQueryWrapper<UserFileEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserFileEntity::getFilePath, parentFilePath)
                    .eq(UserFileEntity::getFileName, fileName)
                    .eq(UserFileEntity::getDeleteFlag, 0)
                    .eq(UserFileEntity::getIsDir, 1)
                    .eq(UserFileEntity::getUserId, sessionUserId);
            List<UserFileEntity> userFileList = userFileMapper.selectList(lambdaQueryWrapper);
            if (userFileList.size() == 0) {
                UserFileEntity userFile = WhiteFileUtil.getWhiteDir(sessionUserId, parentFilePath, fileName);
                try {
                    userFileMapper.insert(userFile);
                } catch (Exception e) {
                    if (e.getMessage().contains("Duplicate entry")) {
                        //ignore
                    } else {
                        log.error(e.getMessage());
                    }
                }
            }
            whiteFile = new WhiteFile(parentFilePath, true);
        }
    }

    /**
     * 删除重复的子目录文件
     * <p>
     * 当还原目录的时候，如果其子目录在文件系统中已存在，则还原之后进行去重操作
     *
     * @param filePath
     * @param sessionUserId
     */
    public void deleteRepeatSubDirFile(String filePath, String sessionUserId) {
        LambdaQueryWrapper<UserFileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(UserFileEntity::getFileName, UserFileEntity::getFilePath)
                .likeRight(UserFileEntity::getFilePath, WhiteFileUtil.formatLikePath(filePath))
                .eq(UserFileEntity::getIsDir, 1)
                .eq(UserFileEntity::getDeleteFlag, 0)
                .eq(UserFileEntity::getUserId, sessionUserId)
                .groupBy(UserFileEntity::getFilePath, UserFileEntity::getFileName)
                .having("count(fileName) >= 2");

        List<UserFileEntity> repeatList = userFileMapper.selectList(wrapper);

        for (UserFileEntity userFile : repeatList) {
            LambdaQueryWrapper<UserFileEntity> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(UserFileEntity::getFilePath, userFile.getFilePath())
                    .eq(UserFileEntity::getFileName, userFile.getFileName())
                    .eq(UserFileEntity::getDeleteFlag, "0");
            List<UserFileEntity> userFileEntities = userFileMapper.selectList(wrapper1);
            for (int i = 0; i < userFileEntities.size() - 1; i++) {
                userFileMapper.deleteById(userFileEntities.get(i).getUserFileId());
            }
        }
    }

    public void parseMusicFile(String extendName, int storageType, String fileUrl, String fileId) {
        File outFile = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            if ("mp3".equalsIgnoreCase(extendName) || "flac".equalsIgnoreCase(extendName)) {
                Downloader downloader = ufopFactory.getDownloader(storageType);
                DownloadFile downloadFile = new DownloadFile();
                downloadFile.setFileUrl(fileUrl);
                inputStream = downloader.getInputStream(downloadFile);
                outFile = UFOPUtils.getTempFile(fileUrl);
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                fileOutputStream = new FileOutputStream(outFile);
                IOUtils.copy(inputStream, fileOutputStream);
                Music music = new Music();
                music.setMusicId(IdUtil.getSnowflakeNextIdStr());
                music.setFileId(fileId);

                Tag tag = null;
                AudioHeader audioHeader = null;
                if ("mp3".equalsIgnoreCase(extendName)) {
                    MP3File f = (MP3File) AudioFileIO.read(outFile);
                    tag = f.getTag();
                    audioHeader = f.getAudioHeader();
                    MP3File mp3file = new MP3File(outFile);
                    if (mp3file.hasID3v2Tag()) {
                        AbstractID3v2Tag id3v2Tag = mp3file.getID3v2TagAsv24();
                        AbstractID3v2Frame frame = (AbstractID3v2Frame) id3v2Tag.getFrame("APIC");
                        FrameBodyAPIC body;
                        if (frame != null && !frame.isEmpty()) {
                            body = (FrameBodyAPIC) frame.getBody();
                            byte[] imageData = body.getImageData();
                            music.setAlbumImage(Base64.getEncoder().encodeToString(imageData));
                        }
                        if (tag != null) {
                            music.setArtist(tag.getFirst(FieldKey.ARTIST));
                            music.setTitle(tag.getFirst(FieldKey.TITLE));
                            music.setAlbum(tag.getFirst(FieldKey.ALBUM));
                            music.setYear(tag.getFirst(FieldKey.YEAR));
                            try {
                                music.setTrack(tag.getFirst(FieldKey.TRACK));
                            } catch (Exception e) {
                                // ignore
                            }

                            music.setGenre(tag.getFirst(FieldKey.GENRE));
                            music.setComment(tag.getFirst(FieldKey.COMMENT));
                            music.setLyrics(tag.getFirst(FieldKey.LYRICS));
                            music.setComposer(tag.getFirst(FieldKey.COMPOSER));
                            music.setAlbumArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
                            music.setEncoder(tag.getFirst(FieldKey.ENCODER));
                        }
                    }
                } else if ("flac".equalsIgnoreCase(extendName)) {
                    AudioFile f = new FlacFileReader().read(outFile);
                    tag = f.getTag();
                    audioHeader = f.getAudioHeader();
                    if (tag != null) {
                        music.setArtist(StringUtils.join(tag.getFields(FieldKey.ARTIST), ","));
                        music.setTitle(StringUtils.join(tag.getFields(FieldKey.TITLE), ","));
                        music.setAlbum(StringUtils.join(tag.getFields(FieldKey.ALBUM), ","));
                        music.setYear(StringUtils.join(tag.getFields(FieldKey.YEAR), ","));
                        music.setTrack(StringUtils.join(tag.getFields(FieldKey.TRACK), ","));
                        music.setGenre(StringUtils.join(tag.getFields(FieldKey.GENRE), ","));
                        music.setComment(StringUtils.join(tag.getFields(FieldKey.COMMENT), ","));
                        music.setLyrics(StringUtils.join(tag.getFields(FieldKey.LYRICS), ","));
                        music.setComposer(StringUtils.join(tag.getFields(FieldKey.COMPOSER), ","));
                        music.setAlbumArtist(StringUtils.join(tag.getFields(FieldKey.ALBUM_ARTIST), ","));
                        music.setEncoder(StringUtils.join(tag.getFields(FieldKey.ENCODER), ","));
                        List<Artwork> artworkList = tag.getArtworkList();
                        if (artworkList != null && !artworkList.isEmpty()) {
                            Artwork artwork = artworkList.get(0);
                            byte[] binaryData = artwork.getBinaryData();
                            music.setAlbumImage(Base64.getEncoder().encodeToString(binaryData));
                        }
                    }

                }

                if (audioHeader != null) {
                    music.setTrackLength(Float.parseFloat(audioHeader.getTrackLength() + ""));
                }

                if (StringUtils.isEmpty(music.getLyrics())) {
                    try {

                        String lyc = MusicUtils.getLyc(music.getArtist(), music.getTitle(), music.getAlbum());
                        music.setLyrics(lyc);
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                }
                musicMapper.insert(music);
            }
        } catch (Exception e) {
            log.error("解析音乐信息失败！", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
            if (outFile != null) {
                if (outFile.exists()) {
                    outFile.delete();
                }
            }
        }
    }

    public boolean checkAuthDownloadAndPreview(String shareBatchNum,
                                               String extractionCode,
                                               String token,
                                               String userFileId,
                                               Integer platform) {
        log.debug("权限检查开始：shareBatchNum:{}, extractionCode:{}, token:{}, userFileId{}" , shareBatchNum, extractionCode, token, userFileId);
        if (platform != null && platform == 2) {
            return true;
        }
        UserFileEntity userFile = userFileMapper.selectById(userFileId);
        log.debug(JSON.toJSONString(userFile));
        if ("undefined".equals(shareBatchNum)  || StringUtils.isEmpty(shareBatchNum)) {

            String userId = userService.getUserIdByToken(token);
            log.debug(JSON.toJSONString("当前登录session用户id：" + userId));
            if (userId == null) {
                return false;
            }
            log.debug("文件所属用户id：" + userFile.getUserId());
            log.debug("登录用户id:" + userId);
            if (!userFile.getUserId().equals(userId)) {
                log.info("用户id不一致，权限校验失败");
                return false;
            }
        } else {
            Map<String, Object> param = new HashMap<>();
            param.put("shareBatchNum", shareBatchNum);
            List<Share> shareList = shareService.listByMap(param);
            //判断批次号
            if (shareList.size() <= 0) {
                log.info("分享批次号不存在，权限校验失败");
                return false;
            }
            Integer shareType = shareList.get(0).getShareType();
            if (1 == shareType) {
                //判断提取码
                if (!shareList.get(0).getExtractionCode().equals(extractionCode)) {
                    log.info("提取码错误，权限校验失败");
                    return false;
                }
            }
            param.put("userFileId", userFileId);
            List<ShareFile> shareFileList = shareFileService.listByMap(param);
            if (shareFileList.size() <= 0) {
                log.info("用户id和分享批次号不匹配，权限校验失败");
                return false;
            }

        }
        return true;
    }


}
