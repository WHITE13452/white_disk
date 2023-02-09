package com.whitedisk.white_disk.controller;

import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.BatchDeleteFileDTO;
import com.whitedisk.white_disk.dto.file.CreateFileDTO;
import com.whitedisk.white_disk.dto.file.RenameFileDTO;
import com.whitedisk.white_disk.dto.file.SearchFileDTO;
import com.whitedisk.white_disk.service.api.IFileService;
import com.whitedisk.white_disk.vo.file.SearchFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author white
 * 文件基本操作
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private IFileService fileService;

    @Autowired
    private FileDealComp fileDealComp;

    private static final String CURRENT_MODULE="文件接口";

    @Operation(summary = "创建文件", description = "目录(文件夹)的创建", tags = {"file"})
    @MyLog(operation = "创建文件", module = CURRENT_MODULE)
    @PostMapping("/createfile")
    @ResponseBody
    public RestResult<String> createFile(@Valid @RequestBody CreateFileDTO createFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        Boolean flag = fileService.createFile(createFileDTO, sessionUser);
        if (!flag){
            return RestResult.fail().message("同名文件已存在");
        }
        return RestResult.success();
    }

    @Operation(summary = "文件搜索", description = "文件搜索", tags = {"file"})
    @MyLog(operation = "搜索文件", module = CURRENT_MODULE)
    @GetMapping("/search")
    @ResponseBody
    public RestResult<List<SearchFileVO>> searchFile(@RequestBody SearchFileDTO searchFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        List<SearchFileVO> searchFileVOList= fileService.searchFile(searchFileDTO,sessionUser);
        return RestResult.success().data(searchFileVOList);
    }
    @Operation(summary = "重命名文件", description = "重命名文件", tags = {"file"})
    @MyLog(operation = "重命名文件", module = CURRENT_MODULE)
    @PostMapping("/renamefile")
    @ResponseBody
    public RestResult<String> renameFile(@RequestBody RenameFileDTO renameFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        Boolean flag=fileService.renameFile(renameFileDTO,sessionUser);
        if(!flag){
            return RestResult.fail().message("同名文件已存在");
        }
        return RestResult.success();
    }
    @Operation(summary = "文件列表", description = "用于展示用户所拥有的文件", tags = {"file"})
    @MyLog(operation = "文件列表", module = CURRENT_MODULE)
    @GetMapping("/getfilelist")
    @ResponseBody
    public RestResult getFileList( @Parameter(description = "文件路径", required = true) String filePath,
                            @Parameter(description = "当前页", required = true) long currentPage,
                            @Parameter(description = "页面数量", required = true) long pageCount){
        JwtUser sessionUser = SessionUtil.getSession();
        return null;
    }

    @Operation(summary = "批量删除文件", description = "批量删除文件", tags = {"file"})
    @MyLog(operation = "批量删除文件", module = CURRENT_MODULE)
    @PostMapping("/batchdeletefile")
    @ResponseBody
    public RestResult<String> deleteBatchOfFile(@RequestBody BatchDeleteFileDTO batchDeleteFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        return null;
    }

    @Operation(summary = "批量文件", description = "批量文件", tags = {"file"})
    @MyLog(operation = "批量文件", module = CURRENT_MODULE)
    @PostMapping("/deletefile")
    @ResponseBody
    public RestResult<String> deleteFile(@RequestBody BatchDeleteFileDTO batchDeleteFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        return null;
    }

}
