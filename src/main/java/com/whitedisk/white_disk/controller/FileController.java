package com.whitedisk.white_disk.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.exception.QiwenException;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.*;
import com.whitedisk.white_disk.service.api.IFileService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.vo.file.FileDetailVO;
import com.whitedisk.white_disk.vo.file.FileListVO;
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
    private IUserFileService userFileService;

    @Autowired
    private FileDealComp fileDealComp;

    private static final String CURRENT_MODULE="文件接口";

    @Operation(summary = "创建文件", description = "文件的创建", tags = {"file"})
    @MyLog(operation = "创建文件", module = CURRENT_MODULE)
    @PostMapping("/createFile")
    @ResponseBody
    public RestResult<Object> createFile(@Valid @RequestBody CreateFileDTO createFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        String message =fileService.createFile(createFileDTO,sessionUser);
        return RestResult.success().message(message);
    }

    @Operation(summary = "创建文件夹", description = "文件夹（目录）的创建", tags = {"file"})
    @MyLog(operation = "创建文件夹", module = CURRENT_MODULE)
    @PostMapping("/createFold")
    @ResponseBody
    public RestResult<String> createFold(@Valid @RequestBody CreateFoldDTO createFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        Boolean flag = fileService.createFold(createFileDTO, sessionUser);
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
    public RestResult<FileListVO> getFileList(
            @Parameter(description = "文件类型", required = true) String fileType,
            @Parameter(description = "文件路径", required = true) String filePath,
            @Parameter(description = "当前页", required = true) long currentPage,
            @Parameter(description = "页面数量", required = true) long pageCount){
        JwtUser sessionUser = SessionUtil.getSession();
        if("0".equals(fileType)){
            IPage<FileListVO> fileList = userFileService.userFileList(null,filePath,currentPage,currentPage);
            return RestResult.success().dataList(fileList.getRecords(), fileList.getTotal());
        }else {
            IPage<FileListVO> fileList = userFileService.getFileByFileType(Integer.valueOf(fileType), currentPage, pageCount, sessionUser.getUserId());
            return RestResult.success().dataList(fileList.getRecords(), fileList.getTotal());
        }
    }

    @Operation(summary = "批量删除文件", description = "批量删除文件", tags = {"file"})
    @MyLog(operation = "批量删除文件", module = CURRENT_MODULE)
    @PostMapping("/batchdeletefile")
    @ResponseBody
    public RestResult<String> deleteBatchOfFile(@RequestBody BatchDeleteFileDTO batchDeleteFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        String file = batchDeleteFileDTO.getFiles();
        String[] files = file.split(",");
        for (String userFileId : files) {
            userFileService.deleteUserFile(userFileId,sessionUser);
            fileDealComp.deleteESByUserFileId(userFileId);
        }
        return RestResult.success().message("删除成功");
    }

    @Operation(summary = "删除文件", description = "删除文件", tags = {"file"})
    @MyLog(operation = "删除文件", module = CURRENT_MODULE)
    @PostMapping("/deletefile")
    @ResponseBody
    public RestResult<String> deleteFile(@RequestBody DeleteFileDTO deleteFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        userFileService.deleteUserFile(deleteFileDTO.getUserFileId(), sessionUser);
        fileDealComp.deleteESByUserFileId(deleteFileDTO.getUserFileId());
        return RestResult.success();
    }


    @Operation(summary = "解压文件", description = "解压文件", tags = {"file"})
    @MyLog(operation = "解压文件", module = CURRENT_MODULE)
    @PostMapping("/unzipfile")
    @ResponseBody
    public RestResult<String> unzipFile(@RequestBody UnzipFileDTO unzipFileDTO){
        try{
            fileService.unzipFile(unzipFileDTO.getUserFileId(), unzipFileDTO.getUnzipMode(), unzipFileDTO.getFilePath());
        }catch (QiwenException e){
            return RestResult.fail().message(e.getMessage());
        }
        return RestResult.success();
    }

    @Operation(summary = "文件复制", description = "复制文件或者目录", tags = {"file"})
    @MyLog(operation = "文件复制", module = CURRENT_MODULE)
    @PostMapping("/copyfile")
    @ResponseBody
    public RestResult<String> copyFile(CopyFileDTO copyFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        if(!fileService.copyFile(copyFileDTO,sessionUser)){
            return RestResult.fail().message("原路径与目标路径冲突，不能复制");
        }
        return RestResult.success();
    }

    @Operation(summary = "查询文件详情", description = "查询文件详情", tags = {"file"})
    @GetMapping(value = "/detail")
    @ResponseBody
    public RestResult<FileDetailVO> queryFileDetail(
            @Parameter(description = "用户文件Id", required = true) String userFileId){
        FileDetailVO vo = fileService.getFileDetail(userFileId);
        return RestResult.success().data(vo);
    }
}
