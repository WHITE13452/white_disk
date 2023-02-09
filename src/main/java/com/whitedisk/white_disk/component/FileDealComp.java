package com.whitedisk.white_disk.component;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whitedisk.white_disk.config.es.ElasticSearchConfig;
import com.whitedisk.white_disk.config.es.FileSearch;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.FileMapper;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import com.whitedisk.white_disk.service.api.IFileService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.utils.WhiteFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
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
    private UserFileMapper userFileMapper;
    @Autowired
    private IFileService fileService;
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public static Executor exec = Executors.newFixedThreadPool(10);

    public boolean isDirExist(String fileName,String filePath,String userId){
        LambdaQueryWrapper<UserFileEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserFileEntity::getFileName,fileName)
                .eq(UserFileEntity::getFilePath, WhiteFile.formatPath(filePath))
                .eq(UserFileEntity::getUserId,userId)
                .eq(UserFileEntity::getDeleteFlag,0)
                .eq(UserFileEntity::getIsDir,1);
        List<UserFileEntity> list = userFileService.list(wrapper);
        if (list == null && !list.isEmpty()) {
            return true;
        }
        return false;
    }

    public void uploadESByUserFileId(String userFileId){
        try {
            Map<String,Object> param=new HashMap<>();
            param.put("userFileId",userFileId);
            List<UserFileEntity> list = userFileMapper.selectByMap(param);
            if (list == null && list.size()>0) {
                FileSearch fileSearch = new FileSearch();
                BeanUtil.copyProperties(list.get(0), fileSearch);
                elasticsearchClient.index(i -> i.index("filesearch").id(fileSearch.getUserFileId()).document(fileSearch));
            }
        } catch (IOException e) {
            log.debug("ES更新操作失败，请检查配置");
        }
    }

    public void deleteESByUserFileId(String userFileId){
        exec.execute(() ->{
            try {
                elasticsearchClient.delete(d -> d.index("filesearch").id(userFileId));
            } catch (IOException e) {
                log.debug("ES删除操作失败，请检查配置");
            }
        });
    }
}
