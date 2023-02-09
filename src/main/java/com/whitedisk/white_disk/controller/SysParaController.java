package com.whitedisk.white_disk.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiwenshare.common.result.RestResult;
import com.whitedisk.white_disk.dto.param.QueryGroupParamDTO;
import com.whitedisk.white_disk.entity.SysParam;
import com.whitedisk.white_disk.service.api.ISysParamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author white
 * 系统参数管理
 */
@RestController
@RequestMapping("/sysParam")
@Slf4j
public class SysParaController {

    @Autowired
    private ISysParamService sysParamService;

    @Operation(summary = "查询系统参数组", tags = {"系统参数管理"})
    @GetMapping ("/grouplist")
    @ResponseBody
    public RestResult<Map> groupList(@RequestParam QueryGroupParamDTO groupParamDTO){
        Map<String,Object> result=new HashMap<>();
        QueryWrapper<SysParam> wrapper=new QueryWrapper<>();
        wrapper.lambda().eq(SysParam::getGroupName,groupParamDTO.getGroupName());
        List<SysParam> list=sysParamService.list(wrapper);
        for (SysParam sysParam : list) {
            result.put(sysParam.getSysParamKey(), sysParam.getSysParamValue());
        }
        return RestResult.success().data(result);
    }
}
