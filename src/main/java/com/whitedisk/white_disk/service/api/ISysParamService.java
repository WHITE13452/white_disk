package com.whitedisk.white_disk.service.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whitedisk.white_disk.entity.SysParam;

/**
 * @author white
 */
public interface ISysParamService extends IService<SysParam> {
    /**
     * 根据key值拿到系统参数
     * @param key
     * @return
     */
    String getValue(String key);
}
