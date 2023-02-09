package com.whitedisk.white_disk.component;

import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Future;

/**
 * @author white
 */
@Slf4j
@Component
@Async("asyncTaskExecutor")
public class AsyncTaskComp {

    @Resource
    private UserFileMapper userFileMapper;
    @Autowired
    private FileDealComp fileDealComp;



    public Future<String> checkESUserFileId(String userFileId){
        UserFileEntity userFile=userFileMapper.selectById(userFileId);
        if (userFile == null) {
            fileDealComp.deleteESByUserFileId(userFileId);
        }
        return new AsyncResult<String>("checkUserFileId");
    }
}
