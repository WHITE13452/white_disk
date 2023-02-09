package com.whitedisk.white_disk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whitedisk.white_disk.entity.SysParam;
import com.whitedisk.white_disk.mapper.SysParamMapper;
import com.whitedisk.white_disk.service.api.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author white
 */
@Service
@Slf4j
public class SysParamServiceImpl extends ServiceImpl<SysParamMapper,SysParam> implements ISysParamService {

    @Resource
    private SysParamMapper sysParamMapper;

    @Override
    public String getValue(String key) {
        SysParam sysParam=new SysParam();
        sysParam.setSysParamKey(key);
        List<SysParam> list=sysParamMapper.selectList(new QueryWrapper<>(sysParam));
        if (list != null && !list.isEmpty()) {
            return list.get(0).getSysParamValue();
        }
        return null;
    }
}
