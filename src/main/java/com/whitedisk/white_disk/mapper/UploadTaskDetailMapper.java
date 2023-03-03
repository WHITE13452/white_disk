package com.whitedisk.white_disk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whitedisk.white_disk.entity.UploadTaskDetail;

import java.util.List;

public interface UploadTaskDetailMapper extends BaseMapper<UploadTaskDetail> {
    List<Integer> selectUploadedChunkNumList(String identifier);
}
